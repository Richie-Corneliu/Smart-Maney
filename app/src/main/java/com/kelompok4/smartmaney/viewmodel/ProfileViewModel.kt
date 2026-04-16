package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.profile.ProfileAction
import com.kelompok4.smartmaney.ui.profile.ProfileStatus
import com.kelompok4.smartmaney.ui.profile.ProfileUiState
import com.kelompok4.smartmaney.ui.profile.reduceProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profileUiState.collect { loaded ->
                _uiState.update { current ->
                    loaded.copy(status = current.status)
                }
            }
        }
    }

    fun dispatch(action: ProfileAction) {
        _uiState.update { current -> reduceProfileState(current, action) }
        if (action == ProfileAction.SaveChanges) {
            val latest = _uiState.value
            if (latest.status == ProfileStatus.Updated) {
                viewModelScope.launch {
                    repository.saveProfile(latest)
                }
            }
        }
    }
}

