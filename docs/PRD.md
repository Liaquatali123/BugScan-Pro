# BugScan Pro — Product Requirements

## Goal
A local-first Android diagnostics app for systems and private network ranges the operator owns or is explicitly authorized to test.

## MVP modules
- Private IPv4 CIDR validation and bounded TCP 80/443 host discovery (/24 to /30).
- HTTP/HTTPS HEAD diagnostics with redirects not followed automatically.
- TLS SNI handshake with normal platform certificate validation.
- Single-host TCP port connectivity check.
- Clear result classifications and actionable errors.

## UX
- Premium dark interface with green/cyan accents.
- Bottom navigation: Home, CIDR, HTTP, SNI, Ports.
- Authorization confirmation before every diagnostic.
- Live progress/result panel; no fabricated sample results.

## Privacy
All operations run locally. No analytics, remote API, telemetry, or upload of targets/results.

## Safety and scope
CIDR discovery is limited to RFC1918 private IPv4 ranges and at most 254 host addresses. No carrier-bypass, exploit, credential attack, stealth, proxy tunneling, or access-control circumvention features.

## Release acceptance
- Builds as a signed or debug APK.
- Real network I/O only.
- HTTP redirects and errors remain distinguishable.
- TLS certificate validation stays enabled.
- Scan input and port bounds are validated.
- No unnecessary permissions.
