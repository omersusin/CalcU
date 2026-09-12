# CalcU — All-in-One Calculator (calc.u)

One app for everyday math: a fast scientific calculator, graphing, 80+ currencies,
27 unit categories, finance, geometry, health, timers, electronics, network, and text tools.
Material3 + Fluent/WinUI-inspired design, offline-first, no ads.

Why CalcU: replaces a dozen single-purpose apps with one searchable Tools hub.
Type once, get answers with steps where it matters (quadratic, EMI, GCD, length).

## Highlights (store-style)

- Scientific calculator with history, notes, search, copy + share, DEG/RAD, haptics
- 2-function grapher with pinch-zoom, pan, tap readout, reset, share
- 27 converter categories + live currency + unit-expression parser
- Tip, EMI + schedule preview, SIP, CAGR, FD, VAT, stock average, Rule of 72
- GCD/LCM/primes/nCr/nPr, stats, quadratic/cubic, 2x2/3x3 solvers, matrices, vectors
- BMI, body-fat (Navy), TDEE, water, pace, 1RM, heart-rate, age + world clock
- Stopwatch (laps), countdown timer, Pomodoro phases
- 4/5-band resistor, divider, LED, RC + IPv4 subnet
- SHA-256/MD5, Base64, word counts, QR, UUID
- Works offline; currency refreshes when online, falls back gracefully

## Features (verified in `core` + `ui/screens`)

- Calculator: EvalEx engine, DEG/RAD trig, history (search/notes/clear/delete), copy/share.
- Graph: f(x)/g(x) plots, zoom 5–500, pan, tap (x/f/g/y), empty-view warning, share.
- Converters: length, mass, volume, temp (C/F/K), area, speed, pressure, energy,
  power, data, fuel (L/100km, mpg, km/L), cooking, shoe, ring, historic, angle,
  force, torque, acceleration, flow, datarate, viscosity, radiation, illuminance,
  magnetic, density, specificenergy; unit-expr (`ft*lbf`→J); 80 currency codes
  (live + fallback); ft/in→cm; cups→grams; bin/oct/hex + Roman 1–3999; color Hex/RGB/HSL.
- Finance: tip/split, EMI + interest preview, simple/compound, sales tax (incl/excl),
  unit-price compare, SIP donut, CAGR, fixed deposit, VAT, trip cost/time,
  stock average, savings goal, Rule of 72.
- Math: GCD/LCM/prime/nCr/nPr/factorial, percent/discount, mean/median/min/max/sum/count,
  median/mode/variance/stdev, quadratic + cubic, 2x2 + 3x3 systems, 2x2 det/transpose/inverse,
  vectors (dot/cross/magnitude/angle), clock angle, totient/mod-inverse/prime-factors/
  Fibonacci(0–92), programmer AND/OR/XOR/NOT/shifts + RNG, 36 constants (search/copy), steps tabs.
- Health: BMI + category, Navy body-fat (M/F), TDEE (Mifflin-St Jeor + 5 activity levels),
  water intake, run pace (min/km), 1RM (Epley), target HR.
- Time: stopwatch (H:MM:SS.cs + laps/splits), countdown (min/sec), Pomodoro (focus/short/long/rounds).
- Electro: resistor 4/5-band decode, voltage divider, LED resistor, RC time constant.
- Network: IPv4 subnet (network/broadcast/mask/hosts/first/last) for prefix 0–32.
- Text+Data: SHA-256, MD5, Base64 encode/decode, words/chars/lines, QR matrix, UUID v4.
- Everyday: trip fuel cost + drive time, unit-price verdict, percent/discount, age YMD,
  weekday/days-until, color picker + swatch, cups-to-grams kitchen helper.
- System: screen aspect ratio + PPI, dec/hex/oct/bin + bitwise, RNG history,
  constants library, zone-ID world clock (HH:mm), unit favorites + saved pairs.

## Architecture + tech stack

- Single `:app` module, `calc.u.core` (pure-Kotlin engines: Engine, Units, UnitExpr,
  Currency, Finance, Geometry/HealthDate/ColorKit/ScreenKit/TripKit/ClockKit/VectorKit/
  ClockAngle, Matrix, NumberTheory, Electro, Network, TimeLab, TextData, Constants)
  + `ui/screens` (Calculator, Graph, Converters, Finance, Math/Geometry/Health/Steps,
  TimeLab, Electro/Subnet, TextData, ToolsHub search, Settings).
- Kotlin 2.0.21, AGP 8.5.2, JDK 17, compile/target 34, minSdk 24, KSP.
- Compose Material3 (BOM 2024.09.00) + Navigation Compose; Hilt 2.51.1; Room 2.6.1
  (history); Retrofit 2.11 + OkHttp 4.12 (currency rates); DataStore prefs
  (unit favorites/pairs); Coroutines + Serialization; Coil 2.6; desugaring.
- WinUI-inspired tokens mapped to M3: Mica/Acrylic→surfaceContainer, AccentFill→primary,
  NavigationView→drawer + rail, ContentDialog→AlertDialog, TeachingTip→Snackbar,
  Pivot→TabRow, InfoBar→tonal Card.

## Build

- CI (`android.yml`, JDK 17, Gradle 8.7): `gradle :app:testDebugUnitTest`,
  `gradle :app:assembleDebug`, `gradle :app:lintDebug` (non-blocking), prove + upload
  `calcu-debug` APK artifact.
- Signed release via secrets: `KEYSTORE_BASE64`→`/tmp/calcu-release.keystore`
  (`KEYSTORE_PATH`), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`;
  `gradle :app:assembleRelease`, `apksigner verify`, upload `calcu-release`.
- Rolling `latest` GitHub release on push to main: debug + release APKs attached.
- Local: same Gradle commands above; release needs the four secrets/keystore env vars.

## License / attribution

- EvalEx (com.ezylang, EvalEx 3.3.0) — Apache-2.0.
- UnitConverterUltimate conversion factors (retyped tables in `Units.kt`) — Apache-2.0.
- WinUI (microsoft/microsoft-ui-xaml) + fluentui-android design language — MIT (adapted, no assets copied).
- CalcHub finance-formula ideas (SIP/CAGR/FD/VAT/stock-average/savings/Rule-72 style tools) — MIT, re-implemented from scratch.
- OpenCalc / Fossify (CalcYou lineage) / CalcYou / Unitto / Calculator-You / ConvertAll /
  Qalculate / kimon / Ever-Dialer / Stagnant09 — ideas re-implemented, no code copied.
- zxing core (QR encode, 3.5.3) — Apache-2.0.
- App code: see repo root for project license; third-party licenses live in Settings > Licenses (SBOM).

## Screenshots / Play listing

Short: every calculator you need, in one offline-first app.
Long: CalcU bundles scientific calc, grapher, converters, finance, geometry, health,
timers, resistor + subnet, and hash/QR tools behind one search bar.
Full description: install, open Tools, search any unit or formula, pin favorites.
Tags: calculator, scientific, graph, unit converter, currency, EMI, BMI, QR.
