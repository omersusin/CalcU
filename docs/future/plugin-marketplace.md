# Future idea: in-app runtimes + installable tool/library packs (NOT approved)

Status: parked. Decided 2026-09-14 — do NOT build now. Revisit only with an
explicit user vote (tools still need scope votes per AGENTS.md).

## The idea
- Embed script runtimes (Python is only an example — JS, Lua, or others count
  too) and let users search, download, and use tool/library packs from inside
  the app. Download once, use offline afterwards.

## Why parked
- APK size: a Python runtime alone dwarfs the 5.7 MB R8 release build.
- Security: downloading + executing code needs sandboxing, pack signing, and
  version/pinning policy.
- Locks it must respect: local-only forever, free forever (no ads/IAP),
  clean-room only (no GPL/PolyForm/ unlicensed code — Apache-2.0/MIT only,
  with attribution), offline-first after install.
- Store policy risk: dynamically downloaded executable code is a Play-policy
  minefield; needs legal/policy review before any implementation.

## Cheaper alternative (approved direction instead)
- More built-in offline tools in Text+Data (hex, URL, JWT decode, hashes,
  UUID, text diff, …) in Kotlin — zero size cost, zero risk.
- Small function definitions inside the existing calculator engine
  (variables already persist; user functions are the natural next step).

## If revived, decide first
1. Which runtimes (embedded Python via Chaquopy vs JS engine vs pure-Kotlin DSL).
2. Pack source + trust: bundled catalog vs URL, signing, updates, removal.
3. Sandbox limits: network access for packs? storage? extra permissions?
4. Size budget: max APK delta the user accepts.
5. Policy clearance for downloadable executable code.
