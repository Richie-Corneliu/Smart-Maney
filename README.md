# SmartManey

SmartManey is a single-module Android app using Jetpack Compose.

## Current Flow

- `MainActivity` hosts `SmartManeyTheme` and `AppNavHost`.
- Navigation uses Navigation Compose with two routes:
  - `login`
  - `dashboard`
- Login button navigates to dashboard.
- Logout action in dashboard navigates back to login.
- Dashboard keeps local reducer-driven UI state (`selectedTab`, `monthlyBudget`) in `AppState.kt`.

## Quick Run

```zsh
cd /home/brian/StudioProjects/Smart-Maney
./gradlew :app:assembleDebug
```

## Quick Test

```zsh
cd /home/brian/StudioProjects/Smart-Maney
./gradlew :app:testDebugUnitTest
```

