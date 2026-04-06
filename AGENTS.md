# AGENTS Guide for SmartManey

## Project Scope and Shape
- Single-module Android app: `:app` only (`settings.gradle.kts`).
- Package root is `com.kelompok4.smartmaney` (`app/build.gradle.kts`, `AndroidManifest.xml`).
- UI is Jetpack Compose-first; no XML layouts are used for screens.
- Current app is starter-level: one activity and one composable flow (`MainActivity.kt`).

## Architecture and Data Flow (Current)
- Entry point: `MainActivity.onCreate()` calls `enableEdgeToEdge()` then `setContent { ... }`.
- Compose tree starts with `SmartManeyTheme { Scaffold { Greeting(...) } }` in `MainActivity.kt`.
- Theme boundary is centralized in `ui/theme/Theme.kt`; keep app-wide color/typography decisions there.
- `SmartManeyTheme` uses dynamic color on Android 12+ (`Build.VERSION_CODES.S`) and falls back to static palettes.
- There is no navigation graph, repository layer, or persistence yet; new features should define these explicitly.

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
- Keep composables small and previewable: see `Greeting()` + `GreetingPreview()` in `MainActivity.kt`.
- Wrap top-level UI in `SmartManeyTheme` instead of creating ad-hoc `MaterialTheme` instances.
- Place design tokens under `app/src/main/java/com/kelompok4/smartmaney/ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`).
- Keep package naming consistent with `com.kelompok4.smartmaney` for new source sets and tests.

## Integration Points and Boundaries
- App launch wiring is manifest-driven: `.MainActivity` with `MAIN/LAUNCHER` intent filter (`AndroidManifest.xml`).
- App label and theme are resource-backed (`res/values/strings.xml`, `res/values/themes.xml`).
- Backup/data extraction XML exists and is referenced in manifest; preserve these links when editing app config.

## When Adding New Features
- If you add a new layer (navigation, data, network), document entry points and package layout in this file.
- Add tests in matching source sets: JVM tests in `app/src/test`, device tests in `app/src/androidTest`.
- Prefer incremental, Compose-centric changes that keep `MainActivity` as host and move feature UI into new files.

## Feature Notes (Updated)
- Login UI structure now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/login/LoginScreen.kt`.
- `MainActivity` hosts `LoginScreen()` inside `SmartManeyTheme` and remains the manifest launch entry point.
- Current login actions are callback-based placeholders (`onLoginClick`, `onRegisterClick`, `onGoogleClick`) with no auth integration yet.
- Dashboard UI now lives in `app/src/main/java/com/kelompok4/smartmaney/ui/dashboard/DashboardScreen.kt`.
- App-level screen switching now uses Navigation Compose in `app/src/main/java/com/kelompok4/smartmaney/navigation/AppNavHost.kt` (`login` <-> `dashboard`).
- Local dashboard UI state (`selectedTab`, `monthlyBudget`) is reduced in `app/src/main/java/com/kelompok4/smartmaney/AppState.kt`.

