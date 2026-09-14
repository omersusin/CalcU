# In-app runtimes + installable tool/library packs (APPROVED, scoped)

Status: approved 2026-09-14 — on-demand downloadable packs only. Cloud
backup/sync and accounts remain parked (separate future notes).
Scope rules: packs install on demand (never bundled), hash-pinned +
signature-checked, sandboxed (no network/storage for pack code), copyleft
code never bundled (Apache-2.0/MIT only, with attribution).

## The idea
- Embed script runtimes (Python is only an example — JS, Lua, or others count
  too) and let users search, download, and use tool/library packs from inside
  the app. Download once, use offline afterwards.

## Why parked
- APK size: a Python runtime alone dwarfs the 5.7 MB R8 release build.
- Security: downloading + executing code needs sandboxing, pack signing, and
  version/pinning policy.
- Locks it must respect: on-device by default (only pack downloads use the
  network), free forever (no ads/IAP), clean-room only (no GPL/PolyForm/
  unlicensed code — Apache-2.0/MIT only, with attribution), offline use
  after install.
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
