# BugScan Pro

Professional Android network diagnostics app built with Kotlin and Jetpack Compose.

## Planned capabilities
- Authorized IPv4 CIDR range validation and bounded host checks
- HTTP/HTTPS diagnostics with redirect visibility
- TLS/SNI handshake and certificate diagnostics
- TCP port checks for explicitly supplied hosts and ports
- DNS lookup, local scan history, and TXT/CSV/JSON export

## Stack
Kotlin, Jetpack Compose, Material 3, Coroutines, OkHttp, Room, DataStore, Hilt.

## Safety
Use only against systems and network ranges you own or are explicitly authorized to test. The app is a diagnostic tool; it does not perform carrier bypass, unauthorized tunneling, exploitation, or access-control circumvention.

## Status
Project scaffold is being initialized. See `docs/PRD.md` and `docs/ARCHITECTURE.md`.