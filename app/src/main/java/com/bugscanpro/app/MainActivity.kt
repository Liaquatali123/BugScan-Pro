package com.bugscanpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Bg = Color(0xFF080D12)
private val Panel = Color(0xFF111B24)
private val Green = Color(0xFF52F2A5)
private val Cyan = Color(0xFF53D9F5)
private val Muted = Color(0xFF91A4B3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BugScanApp() }
    }
}

@Composable
private fun BugScanApp() {
    var tab by remember { mutableStateOf("Home") }
    var cidr by remember { mutableStateOf("192.168.1.0/24") }
    var host by remember { mutableStateOf("example.com") }
    var sni by remember { mutableStateOf("example.com") }
    var port by remember { mutableStateOf("443") }
    var authorized by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("Ready. Choose a diagnostic module to begin.") }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Green, secondary = Cyan, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Panel, contentColor = Color.White) {
                listOf("Home", "CIDR", "HTTP", "SNI", "Ports").forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Text(when(item) { "Home" -> "⌂"; "CIDR" -> "⌗"; "HTTP" -> "↗"; "SNI" -> "◇"; else -> "⇄" }, fontSize = 20.sp) },
                        label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green, selectedTextColor = Green,
                            indicatorColor = Color(0xFF20382F), unselectedTextColor = Muted
                        )
                    )
                }
            }
        }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("BUGSCAN PRO", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Network diagnostics", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Color(0xFF18382D), shape = RoundedCornerShape(50)) {
                        Text("● READY", color = Green, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (tab == "Home") {
                    Surface(color = Panel, shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Professional toolkit", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("HTTP, TLS/SNI, CIDR and TCP checks for networks you own or are authorized to test.", color = Muted, lineHeight = 20.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Stat("04", "Modules")
                                Stat("LOCAL", "Processing")
                                Stat("SAFE", "Scope-first")
                            }
                        }
                    }
                    listOf("CIDR" to "Discover responsive hosts in a small private IPv4 subnet", "HTTP" to "Inspect HTTP status, redirects and response timing", "SNI" to "Verify TLS handshake and certificate details", "Ports" to "Check a selected TCP port on an authorized host").forEach { (name, desc) ->
                        Surface(onClick = { tab = name }, color = Panel, shape = RoundedCornerShape(16.dp)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) { Text(name, color = Color.White, fontWeight = FontWeight.Bold); Text(desc, color = Muted, fontSize = 12.sp) }
                                Text("›", color = Green, fontSize = 28.sp)
                            }
                        }
                    }
                } else {
                    Text(tab + " Scanner", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    when (tab) {
                        "CIDR" -> {
                            InputField("Private IPv4 CIDR (maximum /24)", cidr) { cidr = it }
                            Text("Only RFC1918 private ranges are accepted. Host discovery probes TCP 80/443; maximum 256 addresses.", color = Muted, fontSize = 12.sp)
                        }
                        "HTTP" -> {
                            InputField("Hostname or URL", host) { host = it }
                            Text("Sends a HEAD request and records status/redirect. HTTPS certificate validation remains enabled.", color = Muted, fontSize = 12.sp)
                        }
                        "SNI" -> {
                            InputField("TLS server name (SNI)", sni) { sni = it }
                            Text("Performs a normal certificate-validated TLS handshake on port 443.", color = Muted, fontSize = 12.sp)
                        }
                        "Ports" -> {
                            InputField("Authorized hostname / IP", host) { host = it }
                            InputField("TCP port (1–65535)", port) { port = it }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = authorized, onCheckedChange = { authorized = it }, colors = CheckboxDefaults.colors(checkedColor = Green))
                        Text("I own this target or have permission to test it.", color = Muted, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (!authorized) {
                                output = "Blocked: confirm that you own or are authorized to test this target."
                            } else if (busy) {
                                output = "A scan is already running."
                            } else {
                                busy = true
                                output = "Running diagnostic…"
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        runCatching {
                                            when (tab) {
                                                "CIDR" -> NetworkDiagnostics.scanPrivateCidr(cidr)
                                                "HTTP" -> NetworkDiagnostics.checkHttp(host)
                                                "SNI" -> NetworkDiagnostics.checkTls(sni)
                                                "Ports" -> NetworkDiagnostics.checkPort(host, port.toInt())
                                                else -> "Select a module."
                                            }
                                        }.getOrElse { "ERROR: ${it.message ?: it.javaClass.simpleName}" }
                                    }
                                    output = result
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color(0xFF07110C)),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                        else Text("START DIAGNOSTIC", fontWeight = FontWeight.Bold)
                    }
                }

                Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("LIVE RESULTS", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            Text(if (busy) "RUNNING" else "OUTPUT", color = Muted, fontSize = 10.sp)
                        }
                        Text(output, color = Color(0xFFD6E5EC), fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
                Text("BugScan Pro • Local diagnostics • Use responsibly", color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Surface(color = Color(0xFF1A2933), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(value, color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun InputField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValue, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green, focusedLabelColor = Green,
            unfocusedBorderColor = Color(0xFF344651), unfocusedLabelColor = Muted,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White
        )
    )
}
