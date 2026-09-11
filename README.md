# CalcU — All-in-One Calculator (calc.u)

Fluent/WinUI-inspired Material3 calculator suite: standard + scientific (EvalEx engine),
graphing, 60+ currencies, 10 unit categories, math (GCD/LCM/prime/nCr/quadratic/fractions),
geometry, finance (tip/tax/EMI/interest/unit-price), date/health (BMI/body-fat/TDEE/Ohm).

## Design — WinUI (microsoft/microsoft-ui-xaml, MIT) adapted to Compose
Reference clone: `cloned_repos/CalcU-refs/microsoft-ui-xaml` + WinUI-Gallery mapping:
- Mica/Acrylic -> surface/surfaceContainerHigh, Smoke -> scrim
- AccentFill -> primary, 4dp small / 8dp medium / 12dp large shapes
- NavigationView -> ModalNavigationDrawer + NavigationRail pattern
- ContentDialog -> AlertDialog, TeachingTip -> Snackbar, Pivot -> TabRow+Pager, InfoBar -> tonal Card

## OSS ports (ideas re-implemented, licenses respected)
- EvalEx (Apache-2.0, com.ezylang) — expression engine
- Calculator++ JSCL concepts (Apache-2.0) — scientific depth
- UnitConverterUltimate (Apache-2.0) — converter tables
- OpenCalc / Fossify Calculator (GPL-3.0) — history/haptics ideas re-implemented, no verbatim copy
- See Settings > Licenses in-app (to be wired to full SBOM).

## Build
CI builds `:app:testDebugUnitTest` + `assembleDebug` on JDK 17, AGP 8.5.2, Kotlin 2.0.21.
