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
  - `monthly_report_route`
  - `budget_planning_route`
- Login button navigates to dashboard.
- Logout action in dashboard navigates back to login.
- Dashboard keeps local reducer-driven UI state (`selectedTab`, `monthlyBudget`) in `AppState.kt`.
- Wallet uses local reducer-driven state in `app/src/main/java/com/kelompok4/smartmaney/ui/wallet/WalletState.kt` and UI in `app/src/main/java/com/kelompok4/smartmaney/ui/wallet/WalletScreen.kt`.

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

