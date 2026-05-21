package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kelompok4.smartmaney.data.export.CsvExporter
import com.kelompok4.smartmaney.data.local.preferences.CurrencyOption
import com.kelompok4.smartmaney.data.local.preferences.CurrencyPreferenceStore
import com.kelompok4.smartmaney.data.local.preferences.ThemeMode
import com.kelompok4.smartmaney.data.local.preferences.ThemePreferenceStore
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.settings.DeleteAccountState
import com.kelompok4.smartmaney.ui.settings.SettingsUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(
    private val repository: SmartManeyRepository,
    private val themePreferenceStore: ThemePreferenceStore,
    private val currencyPreferenceStore: CurrencyPreferenceStore
) : ViewModel() {

    private val deleteState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    private val exporting = MutableStateFlow(false)

    private val _exportEvents = MutableSharedFlow<ExportEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val exportEvents: SharedFlow<ExportEvent> = _exportEvents.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferenceStore.themeMode,
        currencyPreferenceStore.currency,
        deleteState,
        exporting
    ) { theme, currency, delete, isExporting ->
        SettingsUiState(
            themeMode = theme,
            currency = currency,
            deleteState = delete,
            isExporting = isExporting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(
            themeMode = themePreferenceStore.currentThemeMode(),
            currency = currencyPreferenceStore.currentCurrency(),
            deleteState = DeleteAccountState.Idle
        )
    )

    fun setThemeMode(mode: ThemeMode) {
        themePreferenceStore.setThemeMode(mode)
    }

    fun setCurrency(option: CurrencyOption) {
        currencyPreferenceStore.setCurrency(option)
    }

    fun requestDeleteAccount() {
        deleteState.value = DeleteAccountState.Confirming
    }

    fun cancelDeleteAccount() {
        deleteState.value = DeleteAccountState.Idle
    }

    fun acknowledgeDeleteResult() {
        deleteState.value = DeleteAccountState.Idle
    }

    fun confirmDeleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            deleteState.value = DeleteAccountState.Error("Not signed in")
            return
        }
        val uid = user.uid
        deleteState.value = DeleteAccountState.InProgress
        viewModelScope.launch {
            runCatching {
                repository.deleteAccountData(uid)
                user.delete().await()
            }.onSuccess {
                deleteState.value = DeleteAccountState.Success
            }.onFailure { error ->
                deleteState.value = when (error) {
                    is FirebaseAuthRecentLoginRequiredException -> DeleteAccountState.RequiresReauth
                    else -> DeleteAccountState.Error(error.message ?: "Failed to delete account")
                }
            }
        }
    }

    fun exportTransactionsCsv() {
        if (exporting.value) return
        exporting.value = true
        viewModelScope.launch {
            val result = runCatching {
                val transactions = repository.getAllTransactionsForExport()
                if (transactions.isEmpty()) {
                    ExportEvent.Empty
                } else {
                    ExportEvent.Ready(
                        filename = CsvExporter.buildFilename(),
                        content = CsvExporter.buildCsv(transactions)
                    )
                }
            }
            val event = result.getOrElse {
                ExportEvent.Failed(it.message ?: "Export failed")
            }
            _exportEvents.tryEmit(event)
            exporting.value = false
        }
    }
}

sealed interface ExportEvent {
    data class Ready(val filename: String, val content: String) : ExportEvent
    data object Empty : ExportEvent
    data class Failed(val message: String) : ExportEvent
}
