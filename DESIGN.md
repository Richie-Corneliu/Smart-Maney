# Design System & Guidelines for SmartManey

## Overview
SmartManey is a Jetpack Compose-first Android application focused on personal expense management. This document outlines design system decisions, UI patterns, and component conventions to ensure consistency across the app.

---

## Design System

### Color Palette

#### Primary Colors
- **SmPrimary**: Primary brand color used for key actions and highlights
- **SmTextPrimary**: Primary text color for all body and headline text
- **SmHeader**: Color for header elements and primary containers
- **SmBackground**: Default background color for screens
- **SmSurface**: Surface/card backgrounds

#### Semantic Colors
- **SmMuted**: Secondary text, disabled states, and subtle elements
- **SmDivider**: Divider lines and subtle separators
- **SmDanger**: Error states and destructive actions
- **SmCategoryRent**: Category indicator (extensible pattern for other categories)

#### Theme Variants
- **Light Theme**: Uses `LightColorScheme` with static colors (see `ui/theme/Color.kt`)
- **Dark Theme**: Uses `DarkColorScheme` with adjusted contrast; automatically applied when system dark mode is enabled
- **Dynamic Color** (Android 12+): Optional runtime color theming based on system wallpaper (disabled by default; enable via `SmartManeyTheme(dynamicColor = true)`)

**Implementation**: All colors are defined in `app/src/main/java/com/kelompok4/smartmaney/ui/theme/Color.kt` and applied centrally via `SmartManeyTheme` in `Theme.kt`.

---

## Typography

Typography tokens are centralized in `app/src/main/java/com/kelompok4/smartmaney/ui/theme/Type.kt` and consist of:
- **Display/Headline styles**: Headlines and page titles
- **Title styles**: Section and card titles
- **Body styles**: Body text and descriptions
- **Label styles**: Labels, buttons, and captions

**Usage Pattern**:
```kotlin
Text(
    text = "Expense History",
    style = MaterialTheme.typography.headlineSmall
)
```

All text should use Material 3 typography tokens from `MaterialTheme.typography` to ensure consistency.

---

## Component Patterns

### Screen Structure
Every screen composable should:
1. Accept a `uiState` parameter (ViewModel state)
2. Accept callback functions for user actions
3. Use `SmartManeyTheme` at the root **only if creating a preview**—normal app screens rely on `MainActivity` wrapping in `SmartManeyTheme`
4. Be previewed with a `XxxScreenPreview()` function at the end of the file

**Example** (`ui/dashboard/DashboardScreen.kt`):
```kotlin
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onTabChange: (DashboardTab) -> Unit,
    onTransactionClick: (Int) -> Unit,
    // ... other callbacks
) {
    // ... UI implementation
}

@Preview
@Composable
fun DashboardScreenPreview() {
    SmartManeyTheme {
        DashboardScreen(
            uiState = DashboardUiState.createPreviewState(),
            onTabChange = {},
            onTransactionClick = {},
            // ...
        )
    }
}
```

### Composable Decomposition
- Keep individual composables **small and focused**
- Extract reusable UI pieces into separate private composables within the file or as public utilities in `ui/<feature>/components/`
- Aim for composables under 200 lines for readability

### State Management in Screens
- Screen-level state lives in `ui/<feature>/<Feature>State.kt` data classes
- Local UI state (e.g., selected tab) is managed via `remember { mutableStateOf(...) }` within the composable
- ViewModel-level state flows should be observed via `collectAsState()`
- Callbacks propagate user actions up to the ViewModel or parent NavHost

**Example** (from `ExpenseHistoryScreen.kt`):
```kotlin
@Composable
fun ExpenseHistoryScreen(
    uiState: ExpenseHistoryUiState,
    onFilterChange: (ExpenseFilter) -> Unit,
    onTransactionClick: (Int) -> Unit,
) {
    var localSelectedFilter by remember { mutableStateOf(ExpenseFilter.ALL) }
    
    // ... UI using uiState and local state
}
```

---

## Theming

### SmartManeyTheme Composable
Located in `ui/theme/Theme.kt`, this is the single source of truth for app-wide theme application.

**Key Parameters**:
- `darkTheme`: Boolean (defaults to system dark mode preference via `isSystemInDarkTheme()`)
- `dynamicColor`: Boolean (defaults to `false`; when `true`, uses Material You colors on Android 12+)

**Usage** (in `MainActivity.kt`):
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
        SmartManeyTheme {
            AppNavHost(appContainer)
        }
    }
}
```

**For Previews**:
```kotlin
@Preview
@Composable
fun MyScreenPreview() {
    SmartManeyTheme {
        MyScreen(...)
    }
}
```

### Dark Mode Support
- Dark mode is **automatically applied** based on system settings
- All colors in `DarkColorScheme` maintain sufficient contrast for readability
- Designer/developer should test previews in both light and dark modes:
  ```kotlin
  @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun MyScreenDarkPreview() {
      SmartManeyTheme(darkTheme = true) {
          MyScreen(...)
      }
  }
  ```

---

## Navigation & Routing

### Route Constants
All navigation routes are centralized in `app/src/main/java/com/kelompok4/smartmaney/navigation/AppDestinations.kt` (not hardcoded in `AppNavHost.kt`).

**Examples**:
```kotlin
const val login = "login"
const val dashboard = "dashboard"
const val wallet_route = "wallet"
const val scan_receipt_route = "scan_receipt"
const val expense_history_route = "expense_history"
const val budget_planning_route = "budget_planning"
const val profile_route = "profile"
const val transaction_detail_route = "transaction_detail/{transactionId}"
const val edit_transaction_route = "edit_transaction/{transactionId}"
```

**Pattern**: When adding a new screen, register its route in `AppDestinations.kt` and wire it in `AppNavHost.kt`.

### Named Navigation Arguments
Routes that accept arguments should use NavType serialization:
```kotlin
composable(
    route = transaction_detail_route,
    arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
) { backStackEntry ->
    val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: return@composable
    // ...
}
```

### Bottom Navigation & Tab Routing
Dashboard uses a bottom navigation bar with tabs. Tab selection is tracked locally in `AppState.kt` and persisted per navigation action:
```kotlin
var selectedDashboardTab by remember { mutableStateOf(DashboardTab.OVERVIEW) }
```

---

## Layout & Spacing

### Density & Padding
Use Material 3 spacing tokens consistently:
- **8dp**: Smallest padding (compact elements)
- **16dp**: Standard padding (cards, sections)
- **24dp**: Large padding (screen margins, major sections)
- **32dp**: Extra-large padding (full-screen spacing)

**Pattern** (using `Modifier`):
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
) {
    // ...
}
```

### Responsive Design
- Compose layouts should adapt to different screen sizes
- Use `BoxWithConstraints` or `WindowSizeClass` for major breakpoints (phone vs. tablet)
- For this app (phone-first), ensure landscape and portrait orientations work

### Safe Area Insets
- Use `enableEdgeToEdge()` in `MainActivity` to maximize screen real estate
- Critically, apply safe insets to Compose layouts using `Modifier.systemBarsPadding()`, `Modifier.navigationBarsPadding()`, etc.
- Example: Bottom navigation bar should respect navigation bar insets

---

## Component Guidelines

### Cards & Surfaces
- Use `Card()` from Material 3 for elevated containers
- Apply consistent corner radius (Material 3 default: 12dp)
- Include subtle shadows for depth

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    shape = RoundedCornerShape(12.dp)
) {
    // Card content
}
```

### Buttons
- Use `Button()` for primary actions
- Use `OutlinedButton()` for secondary actions
- Use `TextButton()` for tertiary actions
- Always provide meaningful labels

```kotlin
Button(
    onClick = { /* action */ },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Confirm")
}
```

### Input Fields
- Use `TextField()` or `OutlinedTextField()` based on context
- Always include `label` and optional `supportingText` for validation errors
- Apply consistent placeholder text styling

```kotlin
OutlinedTextField(
    value = amount,
    onValueChange = { amount = it },
    label = { Text("Amount") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
)
```

### Lists & Lazy Layouts
- Use `LazyColumn()` or `LazyRow()` for scrollable lists
- Apply `contentPadding` to add spacing around lazy content
- Use `items()` DSL for efficient list rendering

```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(transactions.size) { index ->
        TransactionItem(transactions[index])
    }
}
```

---

## Screens & Features

### Login Screen (`ui/login/LoginScreen.kt`)
- Centered form layout with email/password fields
- Call-to-action buttons (Login, Register, Google Sign-In)
- Currently uses callback placeholders; integrate with auth backend

### Dashboard Screen (`ui/dashboard/DashboardScreen.kt`)
- Tab-based navigation (Overview, Analytics, etc.)
- Summary card showing balance and recent transactions
- Quick action buttons for common tasks (Add Expense, Scan Receipt)

### Wallet Screen (`ui/wallet/WalletScreen.kt`)
- Display list of payment methods/accounts
- Add/edit wallet functionality
- State managed via `WalletState.kt` and Room repository

### Expense History Screen (`ui/expensehistory/ExpenseHistoryScreen.kt`)
- Filterable transaction list grouped by date/category
- Filter controls for date range, category, status
- Special mode: `monthly_recap` (via route argument `mode`) shows monthly summary view
- State in `ExpenseHistoryState.kt`

### Budget Planning Screen (`ui/budgetplanning/BudgetPlanningScreen.kt`)
- Budget category cards with spent vs. allocated progress
- Edit/create budget dialogs
- State in `BudgetPlanningState.kt`; seed data loaded from repository

### Scan Receipt Screen (`ui/scanreceipt/ScanReceiptScreen.kt`)
- Camera integration via CameraX
- Gallery picker fallback for test/demo
- Creates draft transaction via `repository.createDraftTransactionFromReceipt()`
- Navigates to transaction detail for review before saving

### Transaction Detail & Edit (`ui/detail/TransactionDetailScreen.kt`, `ui/detail/EditTransactionScreen.kt`)
- Read-only detail view for completed transactions
- Editable form for new/draft transactions
- Routes: `transaction_detail_route/{transactionId}`, `edit_transaction_route/{transactionId}`

### Budget & Spending Suggestions (`ui/suggestion/SuggestionScreen.kt`)
- Displays spending trends and smart saving recommendations
- Stateless composable (no ViewModel); displays repository data
- Route: `suggestion_route` (should be moved to `AppDestinations.kt` for consistency)
- Accessed via `onSuggestionClick` callback from Wallet screen

### Profile Screen (`ui/profile/ProfileScreen.kt`)
- User information display and settings
- Local state in `ProfileState.kt`
- Route: `profile_route`

---

## Data & State Flow

### ViewModel Pattern
Every stateful screen has a corresponding ViewModel in `viewmodel/`:
- ViewModels inherit from `androidx.lifecycle.ViewModel`
- State is exposed as `StateFlow<UiState>` for Compose collection
- User actions trigger state updates via ViewModel methods

**Example**:
```kotlin
class ExpenseHistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseHistoryUiState())
    val uiState: StateFlow<ExpenseHistoryUiState> = _uiState.asStateFlow()
    
    fun onFilterChange(filter: ExpenseFilter) {
        // Update state based on filter
    }
}
```

### Room Database
- Entities defined in `data/local/entity/`
- DAOs in `data/local/dao/`
- Database class: `SmartManeyDatabase`
- Repository orchestrates DAO calls: `data/repository/SmartManeyRepository.kt`
- Initialization: `onCreate()` in `MainActivity` calls `repository.seedIfEmpty()`

### Repository Pattern
All data access (local and future remote) goes through `SmartManeyRepository`:
```kotlin
class SmartManeyRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    // ...
) {
    fun getTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun createDraftTransactionFromReceipt(receipt: Receipt): Int = /* ... */
    fun seedIfEmpty() { /* ... */ }
}
```

---

## Accessibility

### Text Contrast
- Ensure all text meets WCAG AA standards (4.5:1 for body, 3:1 for large text)
- Test in both light and dark modes

### Content Descriptions
All interactive elements should have meaningful descriptions:
```kotlin
IconButton(
    onClick = { /* action */ },
    modifier = Modifier.semantics { 
        contentDescription = "Add new expense"
    }
) {
    Icon(Icons.Default.Add, contentDescription = null)
}
```

### Touch Targets
- Minimum touch target size: 48dp × 48dp
- Exception: Smaller elements (e.g., icons in toolbar) should have padding to reach 48dp

### Screen Reader Support
- Use `Modifier.semantics` to provide context for screen readers
- Mark decorative elements with `contentDescription = null`
- Announce important state changes via `Modifier.semantics { liveRegion = ... }`

---

## Animation & Motion

### Transitions
- Use Material 3 standard motion curves (`StandardEasing`, `EmphasizedEasing`)
- Duration guidelines:
  - Simple state changes: 150ms
  - Entrance animations: 300ms
  - Exit animations: 200ms

**Example**:
```kotlin
val transition = updateTransition(targetState)
val offset by transition.animateDp { state ->
    if (state) 0.dp else 100.dp
}
```

### Principle
Keep animations purposeful—avoid excessive motion that distracts from content.

---

## Best Practices Checklist

- [ ] All composables have preview functions
- [ ] Routes are registered in `AppDestinations.kt`
- [ ] State is managed via ViewModel + UiState
- [ ] Theme colors are used via `MaterialTheme.colorScheme`
- [ ] Typography uses `MaterialTheme.typography` tokens
- [ ] Padding/spacing follows 8dp grid
- [ ] Dark mode is tested and supported
- [ ] Accessibility checked: contrast, touch targets, descriptions
- [ ] Android 12+ dynamic color gracefully handled (optional feature)
- [ ] Camera/permissions declared in manifest if needed
- [ ] No hardcoded routes; use `AppDestinations` constants
- [ ] No ad-hoc `MaterialTheme` instances; use `SmartManeyTheme`

---

## Future Enhancements

- [ ] Add Material You (Material 3 dynamic theming) as opt-in feature
- [ ] Implement Figma design tokens auto-sync
- [ ] Establish animation/motion library for consistency
- [ ] Create shared component library (`ui/components/`) for reusable atoms
- [ ] Document design edge cases (empty states, loading, error)
- [ ] Establish color accessibility tooling in CI/CD

---

## Related Documentation

- **AGENTS.md**: Architecture, navigation, and codebase organization
- **Theme.kt**: Implementation of `SmartManeyTheme` and color schemes
- **Color.kt**: Color palette definitions
- **Type.kt**: Typography tokens
- **Material 3 Design**: https://m3.material.io/


