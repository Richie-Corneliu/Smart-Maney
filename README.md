# SmartManey

SmartManey is a single-module Android app using Jetpack Compose.

## Current Flow

- `MainActivity` hosts `SmartManeyTheme` and `AppNavHost`.
- Navigation uses Navigation Compose routes from `AppDestinations` for:
  - `login`
  - `dashboard`
  - `wallet_route`
  - `scan_receipt_route`
  - `expense_history_route`
  - `budget_planning_route`
- Monthly recap is shown through `expense_history_route` with mode argument `monthly_recap`.
- Login button navigates to dashboard.
- Logout action in dashboard navigates back to login.
- `MainActivity` initializes `AppContainer`, seeds Room data, and passes dependencies to `AppNavHost`.
- Persistence uses Room (`data/local`) and repository orchestration (`data/repository/SmartManeyRepository.kt`).
- Screen data flows through ViewModels in `app/src/main/java/com/kelompok4/smartmaney/viewmodel/`.
- Transaction detail/edit screens use ID-based navigation routes backed by Room rows.

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

