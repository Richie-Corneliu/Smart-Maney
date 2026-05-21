package com.kelompok4.smartmaney.ui.settings

import com.kelompok4.smartmaney.data.local.preferences.CurrencyOption
import com.kelompok4.smartmaney.data.local.preferences.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: CurrencyOption = CurrencyOption.IDR,
    val deleteState: DeleteAccountState = DeleteAccountState.Idle,
    val isExporting: Boolean = false
)

sealed interface DeleteAccountState {
    data object Idle : DeleteAccountState
    data object Confirming : DeleteAccountState
    data object InProgress : DeleteAccountState
    data object Success : DeleteAccountState
    data object RequiresReauth : DeleteAccountState
    data class Error(val message: String) : DeleteAccountState
}
