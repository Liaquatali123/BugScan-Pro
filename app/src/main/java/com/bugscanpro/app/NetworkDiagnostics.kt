package com.bugscanpro.app

import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object NetworkDiagnostics {
    fun scanPrivateCidr(input: String): String {
        val parts = input.trim().split("/")
        require(parts.size == 2) { "Enter CIDR like 192.168.1.0/24" }
        val prefix = parts[1].toIntOrNull() ?: error("Invalid prefix")
        require(prefix in 24..30) { "For safety and performance, use a private subnet between /24 and /30." }
        val octets = parts[0].split(".").map { it.toIntOrNull() ?: error("Invalid IPv4 address") }
        require(octets.size == 4 && octets.all { it in 0..255 }) { "Invalid IPv4 address" }
        val first = octets.fold(0L) { acc, n -> (acc shl 8) or n.toLong() }
        val mask = (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
        val network = first and mask
        val a = ((network shr 24) and 255).toInt()
        val isPrivate = a == 10 || (a == 172 && ((network shr 16) and 255L) in 16L..31L) ||
            (a == 192 && ((network shr 16) and 255L) == 168L)
        require(isPrivate) { "Only RFC1918 private IPv4 ranges are supported." }
        val count = (1L shl (32 - prefix)).toInt()
        val lines = mutableListOf<String>()
        for (offset in 1 until count - 1) {
            val ipLong = network + offset
            val ip = listOf(24, 16, 8, 0).joinToString(".") { ((ipLong shr it) and 255).toString() }
            val open = probe(ip, 80) || probe(ip, 443)
            lines += "${if (open) "RESPONSIVE" else "NO RESPONSE"}  $ip"
        }
        return "CIDR: $input\nChecked: ${lines.size} hosts\n\n" + lines.joinToString("\n")
    }

    private fun probe(host: String, port: Int): Boolean = runCatching {
        Socket().use { it.connect(InetSocketAddress(host, port), 650); true }
    }.getOrDefault(false)

    fun checkHttp(input: String): String {
        val normalized = if (input.startsWith("http://") || input.startsWith("https://")) input else "https://$input"
        val url = URL(normalized)
        require(url.protocol == "http" || url.protocol == "https") { "Only HTTP and HTTPS are supported." }
        require(url.host.isNotBlank()) { "Enter a valid hostname." }
        val started = System.currentTimeMillis()
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "HEAD"
            connectTimeout = 5000
            readTimeout = 5000
            instanceFollowRedirects = false
            setRequestProperty("User-Agent", "BugScanPro/1.0")
        }
        return try {
            val status = connection.responseCode
            val elapsed = System.currentTimeMillis() - started
            "URL: $normalized\nHTTP status: $status\nMessage: ${connection.responseMessage}\nRedirect: ${connection.getHeaderField("Location") ?: "none"}\nTime: ${elapsed} ms\nServer: ${connection.getHeaderField("Server") ?: "not disclosed"}"
        } finally { connection.disconnect() }
    }

    fun checkTls(hostInput: String): String {
        val host = hostInput.trim().removePrefix("https://").substringBefore("/").substringBefore(":")
        require(host.isNotBlank() && !host.contains(" ")) { "Enter a valid DNS hostname." }
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val started = System.currentTimeMillis()
        factory.createSocket().use { raw ->
            raw.connect(InetSocketAddress(host, 443), 5000)
            val ssl = raw as SSLSocket
            ssl.soTimeout = 5000
            val params = ssl.sslParameters
            params.serverNames = listOf(SNIHostName(host))
            ssl.sslParameters = params
            ssl.startHandshake()
            val cert = ssl.session.peerCertificates.first()
            val elapsed = System.currentTimeMillis() - started
            return "Host: $host\nTLS: ${ssl.session.protocol}\nCipher: ${ssl.session.cipherSuite}\nCertificate: ${cert.type}\nSubject: ${(cert as? java.security.cert.X509Certificate)?.subjectX500Principal?.name ?: "unknown"}\nHandshake: VALID\nTime: ${elapsed} ms"
        }
    }

    fun checkPort(hostInput: String, port: Int): String {
        val host = hostInput.trim()
        require(host.isNotBlank()) { "Enter a hostname or IP." }
        require(port in 1..65535) { "Port must be between 1 and 65535." }
        val started = System.currentTimeMillis()
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), 4000)
            return "Host: $host\nPort: $port\nTCP: OPEN\nTime: ${System.currentTimeMillis() - started} ms"
        }
    }
}
