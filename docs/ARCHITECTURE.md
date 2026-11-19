# Architecture

## Layers
- UI: Jetpack Compose screens and state.
- Presentation: screen state, coroutine launch/cancellation.
- Diagnostics: pure Kotlin network operations on Dispatchers.IO.
- Platform: Android networking APIs and JVM TLS sockets.

## Initial implementation
The MVP currently keeps the small UI and diagnostics engine in the app module to make the first build easy to reproduce. As features grow, move each engine behind interfaces in core/network and add Room-backed history.

## Network behavior
- CIDR: RFC1918-only IPv4, prefix /24–/30, TCP connect probes to ports 80 and 443, bounded host count.
- HTTP: HEAD request, no automatic redirects, 5-second connect/read timeouts.
- SNI: explicit SNIHostName with default trust validation, port 443.
- Port: one user-selected TCP port and host, 4-second timeout.

## Build
JDK 17, Android Gradle Plugin 8.13.0, Kotlin 2.2.20, compile SDK 36.
