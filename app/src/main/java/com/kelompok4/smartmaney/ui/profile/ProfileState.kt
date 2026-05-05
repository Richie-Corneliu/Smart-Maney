package com.kelompok4.smartmaney.ui.profile

data class ProfileUiState(
    val fullName: String = "Andra Pratama",
    val email: String = "andra@example.com",
    val photoUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val status: ProfileStatus? = null
)

enum class ProfileStatus {
    Updated,
    EmptyName,
    InvalidEmail
}

sealed interface ProfileAction {
    data class UpdateFullName(val value: String) : ProfileAction
    data class UpdateEmail(val value: String) : ProfileAction
    data class ToggleNotifications(val enabled: Boolean) : ProfileAction
    data class ToggleDarkMode(val enabled: Boolean) : ProfileAction
    data object SaveChanges : ProfileAction
}

fun reduceProfileState(current: ProfileUiState, action: ProfileAction): ProfileUiState {
    return when (action) {
        is ProfileAction.UpdateFullName -> current.copy(
            fullName = action.value,
            status = null
        )

        is ProfileAction.UpdateEmail -> current.copy(
            email = action.value,
            status = null
        )

        is ProfileAction.ToggleNotifications -> current.copy(
            notificationsEnabled = action.enabled,
            status = null
        )

        is ProfileAction.ToggleDarkMode -> current.copy(
            darkModeEnabled = action.enabled,
            status = null
        )

        ProfileAction.SaveChanges -> {
            val name = current.fullName.trim()
            val email = current.email.trim()
            when {
                name.isBlank() -> current.copy(status = ProfileStatus.EmptyName)
                !email.contains("@") -> current.copy(status = ProfileStatus.InvalidEmail)
                else -> current.copy(fullName = name, email = email, status = ProfileStatus.Updated)
            }
        }
    }
}


