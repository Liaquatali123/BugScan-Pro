# BugScan Pro

Professional, local-first Android network diagnostics app built with Kotlin and Jetpack Compose.

## Implemented in the initial MVP
- Premium dark dashboard and bottom navigation.
- Authorization confirmation before running a diagnostic.
- Private IPv4 CIDR host discovery, limited to RFC1918 /24–/30 ranges.
- HTTP/HTTPS HEAD checks with redirect visibility and bounded timeouts.
- TLS SNI handshake diagnostics with normal certificate validation.
- Single-host TCP port connectivity checks.
- Android CI workflow to build and upload a debug APK artifact.

## Stack
Kotlin 2.2.20, Android Gradle Plugin 8.13.0, Jetpack Compose, Material 3, Coroutines, OkHttp dependency, Android networking and TLS APIs.

## Build locally
Requirements: JDK 17, Android SDK platform 36, Gradle 8.13.

```bash
gradle assembleDebug
```

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
Push to `main` or run the **Android CI** workflow manually. The workflow uploads an artifact named `BugScan-Pro-debug`.

## Safety and privacy
Use only systems and network ranges you own or are explicitly authorized to test. CIDR discovery is restricted to RFC1918 private IPv4 ranges and at most 254 hosts. The app performs diagnostics only; it does not implement carrier bypass, unauthorized tunneling, exploitation, credential attacks, stealth, or access-control circumvention.

All target processing is local. No analytics, remote API, or automatic result uploads.

## Current status
Initial functional Android MVP scaffold committed. Room history, DNS module, batch export, scan pause/resume, and additional result visualizations remain planned follow-up work.
