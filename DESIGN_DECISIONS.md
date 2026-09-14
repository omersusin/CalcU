# CalcU — Design Decisions & Standing Plan

Sat Decisions captured 2026-09-14 from the user's full message history (all
answers verbatim in `~/calcu_session_user_only.md`). This file is the memory
anchor for the ongoing M3E overhaul. Read it before starting any build work.

## Status snapshot
- **Delete: CANCELLED.** "better delete calcu" is reversed. Repo stays, work continues.
- **Current build:** green. AGP 8.13.0 / Kotlin 2.3.20 / KSP 2.3.12 / Hilt 2.58 / Compose BOM 2026.06 (material3 1.4.0). CI runs test → assemble → lint → signed release APK attach.
- **Depth audit result (2026-09-14):** the code is NOT stub-laden — zero `!!`, zero `TODO()`/`NotImplementedError`, zero TODO comments. The "AI-made / shallow" feeling is driven by *specific screen bugs + perceptual finish*, not dead code. Work below targets those real gaps, not a rewrite.

## Locked design direction (user-confirmed)
1. **Visual: M3E curated + Pixel/Google mature.**
   - Keep Material You dynamic color (decision #93: fix hierarchy, color honesty, radii).
   - Google/Pixel mature finish: pill inputs, generous spacing, soft tonal elevation, tonal color.
   - The theme foundation ALREADY implements this (`theme/FluentTheme.kt`):
     `dynamicFixedScheme` = dynamic + fixed contrast/hierarchy; `FluentShapes`
     radii locked 4/8/12/16/28; tonal schemes via `seedScheme`/`TonalPalette`.
     → Do NOT gratuitously rewrite the theme. Refine gaps only.
2. **Motion: hybrid** = M3E signature springs (SpatialFast/Default/Hero +
   EffectsDefault in `ExpressiveSprings`) + Pixel-calm transitions + iOS-smooth
   damping. Apply across surfaces/navigation.
3. **Numeric input: app-wide shared themed bottom-sheet numeric keypad** (replaces
   per-field system keyboards). Fixes the "IME keyboard never opens on some
   inputs" complaint. `NumPadSheet.kt` exists — wheel it everywhere.
4. **Navigation: Home hub + search + favorites** (AIO-style), drawer becomes
   secondary. Bigger screens: rail + hub.
5. **Dark/light: equal priority.** No dark-first/ligh-first skew.
6. **Process rule: "do first, then confirm"** for UI/motion — build, show, user
   judges, revert if disliked. Tools still require a scope vote first (AGENTS.md).
7. **License: clean-room only.** GPL/AGPL/PolyForm/unlicensed = ideas re-implemented,
   never copied. Apache-2.0/MIT usable as deps with attribution. Verbatim copying
   of copyleft code is refused regardless of repo privacy.

## Priority (both in parallel): polish + feature depth
- Every tool computes for real, unit-tested. No stubs. (AGENTS.md engineering lock.)
- Same aesthetic goal regardless of tool.

## Concrete bug / work inventory (from user reports)
- [ ] Calculator: 8-digit "…" cap + "numbers also shown at top" confusion (#158) — likely `AutoSizingText` shrink + history-row: clarify result line vs expression line.
- [ ] IME keyboard never triggers on some input fields (#109) → app-wide shared numeric keypad.
- [ ] Onboarding keypad layout buttons don't work / keypad shape doesn't change though changed in settings (#155, #157).
- [ ] Sensors: pure-black card + cards in different colors / boxes cut in half (#102–106).
- [ ] Home hub + search + favorites nav rework.
- [ ] App-wide shared numeric keypad wiring.

## Navigation / theme facts to not forget
- Theme entry: `CalcUTheme(theme, dark, dynamic, mode, amoled, customSeedArgb)` in
  `FluentTheme.kt`. Seeds in `CalcUThemeSeeds`; custom HSL seed never renamed.
- `FluentTheme.kt` is 824 lines and already the mature spine — refine, don't rebuild.
- Radii locked: extraSmall 4 / small 8 / medium 12 / large 16 / extraLarge 28.
- Lato bundled for numerals/display; body uses system font.
- `AGENTS.md` standing orders remain authoritative; this doc records the *why/new decisions* on top.

## Never (hard rules)
- Never re-introduce WinUI/Fluent tokens or Microsoft binaries (taste-locked).
- Never add a crowded bottom bar; keep drawer + rail + expanded pane.
- Never add ads/IAP/tracking/account/cloud (business-locked).
- Never copy GPL/PolyForm/unlicensed source into the codebase.
- Never use `!!`, never leave a stub behind.