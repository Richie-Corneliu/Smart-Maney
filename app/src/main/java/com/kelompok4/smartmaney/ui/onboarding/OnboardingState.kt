package com.kelompok4.smartmaney.ui.onboarding

enum class OnboardingStep { WELCOME, BALANCE, BUDGET, DONE }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val userName: String = "",
    val balanceInput: String = "",
    val budgetInput: String = "",
    val isSaving: Boolean = false,
    val balanceError: Boolean = false,
    val budgetError: Boolean = false
)