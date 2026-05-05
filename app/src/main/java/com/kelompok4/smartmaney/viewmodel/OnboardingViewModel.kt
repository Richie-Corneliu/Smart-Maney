package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.onboarding.OnboardingStep
import com.kelompok4.smartmaney.ui.onboarding.OnboardingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun setUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun onBalanceChanged(value: String) {
        _uiState.update { it.copy(balanceInput = value.filter(Char::isDigit), balanceError = false) }
    }

    fun onBudgetChanged(value: String) {
        _uiState.update { it.copy(budgetInput = value.filter(Char::isDigit), budgetError = false) }
    }

    fun nextStep() {
        when (_uiState.value.step) {
            OnboardingStep.WELCOME  -> _uiState.update { it.copy(step = OnboardingStep.BALANCE) }
            OnboardingStep.BALANCE  -> advanceFromBalance()
            OnboardingStep.BUDGET   -> advanceFromBudget()
            OnboardingStep.DONE     -> Unit
        }
    }

    fun prevStep() {
        _uiState.update {
            it.copy(
                step = when (it.step) {
                    OnboardingStep.BALANCE -> OnboardingStep.WELCOME
                    OnboardingStep.BUDGET  -> OnboardingStep.BALANCE
                    else                   -> it.step
                }
            )
        }
    }

    private fun advanceFromBalance() {
        if (_uiState.value.balanceInput.isBlank()) {
            _uiState.update { it.copy(balanceError = true) }
            return
        }
        _uiState.update { it.copy(step = OnboardingStep.BUDGET, balanceError = false) }
    }

    private fun advanceFromBudget() {
        if (_uiState.value.budgetInput.isBlank()) {
            _uiState.update { it.copy(budgetError = true) }
            return
        }
        val state = _uiState.value
        val balance = state.balanceInput.toIntOrNull() ?: 0
        val budget = state.budgetInput.toIntOrNull() ?: 0
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.completeOnboarding(balance, budget)
            _uiState.update { it.copy(isSaving = false, step = OnboardingStep.DONE) }
        }
    }
}