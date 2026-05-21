package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelompok4.smartmaney.data.local.preferences.CurrencyPreferenceStore
import com.kelompok4.smartmaney.data.local.preferences.ThemePreferenceStore
import com.kelompok4.smartmaney.data.remote.service.GeminiReceiptService
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository

class SmartManeyViewModelFactory(
    private val repository: SmartManeyRepository,
    private val transactionId: Long? = null,
    private val geminiReceiptService: GeminiReceiptService? = null,
    private val themePreferenceStore: ThemePreferenceStore? = null,
    private val currencyPreferenceStore: CurrencyPreferenceStore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> OnboardingViewModel(repository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(WalletViewModel::class.java) -> WalletViewModel(repository) as T
            modelClass.isAssignableFrom(ExpenseHistoryViewModel::class.java) -> ExpenseHistoryViewModel(
                repository
            ) as T
            modelClass.isAssignableFrom(BudgetPlanningViewModel::class.java) -> BudgetPlanningViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository) as T
            modelClass.isAssignableFrom(SuggestionViewModel::class.java) -> SuggestionViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                val theme = requireNotNull(themePreferenceStore) {
                    "ThemePreferenceStore is required for SettingsViewModel."
                }
                val currency = requireNotNull(currencyPreferenceStore) {
                    "CurrencyPreferenceStore is required for SettingsViewModel."
                }
                SettingsViewModel(repository, theme, currency) as T
            }
            modelClass.isAssignableFrom(TransactionDetailViewModel::class.java) -> {
                val id = requireNotNull(transactionId) { "Transaction ID is required for TransactionDetailViewModel." }
                TransactionDetailViewModel(repository, id) as T
            }

            modelClass.isAssignableFrom(ScanReceiptViewModel::class.java) -> {
                val service = requireNotNull(geminiReceiptService) {
                    "GeminiReceiptService is required for ScanReceiptViewModel."
                }
                ScanReceiptViewModel(repository, service) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

