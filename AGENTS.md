# AGENTS Guide for SmartManey

## Project Scope and Shape
- Single-module Android app: `:app` only (`settings.gradle.kts`).
- Package root is `com.kelompok4.smartmaney` (`app/build.gradle.kts`, `AndroidManifest.xml`).
- UI is Jetpack Compose-first; no XML layouts are used for screens.
- Current app keeps one activity host (`MainActivity.kt`) and multiple Compose screens wired through Navigation Compose (`app/src/main/java/com/kelompok4/smartmaney/navigation/AppNavHost.kt`).

## Architecture and Data Flow (Current)
- Entry point: `MainActivity.onCreate()` calls `enableEdgeToEdge()` then `setContent { ... }`.
- Compose tree starts with `SmartManeyTheme { AppNavHost(...) }` in `MainActivity.kt`.
- Theme boundary is centralized in `ui/theme/Theme.kt`; keep app-wide color/typography decisions there.
- `SmartManeyTheme` uses dynamic color on Android 12+ (`Build.VERSION_CODES.S`) and falls back to static palettes.
- Navigation graph is defined in `app/src/main/java/com/kelompok4/smartmaney/navigation/AppNavHost.kt`.
- Route constants are centralized in `app/src/main/java/com/kelompok4/smartmaney/navigation/AppDestinations.kt` (for example `login`, `dashboard`, `wallet_route`, `scan_receipt_route`, `expense_history_route`, `budget_planning_route`). Monthly recap uses `expense_history_route` with mode argument `monthly_recap`.
- There is still no repository layer or persistence yet; feature state is local UI state (`AppState.kt`, `ui/monthlyreport/MonthlyReportState.kt`, `ui/budgetplanning/BudgetPlanningState.kt`).

## Build/Test Workflows
- Use Gradle wrapper from repo root: `./gradlew ...`.
- Useful local checks for this project shape:
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:testDebugUnitTest`
  - `./gradlew :app:connectedDebugAndroidTest` (requires emulator/device)
- Instrumentation runner is fixed to `androidx.test.runner.AndroidJUnitRunner` (`app/build.gradle.kts`).

## Dependency and Tooling Conventions
- Dependency versions are centralized in `gradle/libs.versions.toml`; prefer adding aliases there before module edits.
- Plugins are declared via version catalog aliases (`build.gradle.kts`, `app/build.gradle.kts`).
- Repository policy is strict: `FAIL_ON_PROJECT_REPOS` in `settings.gradle.kts` (do not add module-local repos).
- Java/Kotlin compile target currently aligns with Java 11 (`app/build.gradle.kts` compileOptions).
- Gradle daemon toolchain metadata targets Java 21 for daemon resolution (`gradle/gradle-daemon-jvm.properties`).

## Code Patterns to Mirror
- Keep composables small and previewable: see `LoginScreenPreview()` (`ui/login/LoginScreen.kt`), `DashboardScreenPreview()` (`ui/dashboard/DashboardScreen.kt`), `PreviewMonthlyRecap()` (`ui/monthlyreport/MonthlyRecapScreen.kt`), and `PreviewBudgetPlanning()` (`ui/budgetplanning/BudgetPlanningScreen.kt`).
- Wrap top-level UI in `SmartManeyTheme` instead of creating ad-hoc `MaterialTheme` instances.
- Place design tokens under `app/src/main/java/com/kelompok4/smartmaney/ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`).
- Keep package naming consistent with `com.kelompok4.smartmaney` for new source sets and tests.

## Integration Points and Boundaries
- App launch wiring is manifest-driven: `.MainActivity` with `MAIN/LAUNCHER` intent filter (`AndroidManifest.xml`).
- App label and theme are resource-backed (`res/values/strings.xml`, `res/values/themes.xml`).
- Backup/data extraction XML exists and is referenced in manifest; preserve these links when editing app config.
- Camera integration is manifest + Compose driven: `android.permission.CAMERA` and `android.hardware.camera.any` are declared in `AndroidManifest.xml`, and CameraX binding is implemented in `ui/scanreceipt/ScanReceiptScreen.kt`.

## When Adding New Features
- If you add a new layer (navigation, data, network), document entry points and package layout in this file.
- Add tests in matching source sets: JVM tests in `app/src/test`, device tests in `app/src/androidTest`.
- Prefer incremental, Compose-centric changes that keep `MainActivity` as host and move feature UI into new files.

## Feature Notes (Updated)
- Login UI structure now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/login/LoginScreen.kt`.
- `MainActivity` hosts `AppNavHost()` inside `SmartManeyTheme` and remains the manifest launch entry point.
- Current login actions are callback-based placeholders (`onLoginClick`, `onRegisterClick`, `onGoogleClick`) with no auth integration yet.
- Dashboard UI now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/dashboard/DashboardScreen.kt`.
- App-level screen switching uses Navigation Compose in `app/src/main/java/com/kelompok4/smartmaney/navigation/AppNavHost.kt` (`login`, `dashboard`, `wallet_route`, `scan_receipt_route`, `expense_history_route`, `budget_planning_route`) with monthly recap rendered from `expense_history_route` mode `monthly_recap`.
- Wallet flow now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/wallet/WalletScreen.kt` and reducer/state logic is in `app/src/main/java/com/kelompok4/smartmaney/ui/wallet/WalletState.kt`.
- Local dashboard UI state (`selectedTab`, `monthlyBudget`) is reduced in `app/src/main/java/com/kelompok4/smartmaney/AppState.kt`.
- Monthly recap state and dummy data live in `app/src/main/java/com/kelompok4/smartmaney/ui/monthlyreport/MonthlyReportState.kt`.
- Budget planning state and dummy data live in `app/src/main/java/com/kelompok4/smartmaney/ui/budgetplanning/BudgetPlanningState.kt`.
- Scan receipt flow now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/scanreceipt/ScanReceiptScreen.kt` with camera permission + gallery picker handling.
- Profile flow now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/profile/ProfileScreen.kt` with local reducer/state in `app/src/main/java/com/kelompok4/smartmaney/ui/profile/ProfileState.kt` and route `profile_route` wired through `AppNavHost.kt`.

