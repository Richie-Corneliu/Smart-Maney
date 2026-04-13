package com.kelompok4.smartmaney.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileStateTest {

    @Test
    fun saveChanges_withValidData_setsUpdatedStatusAndTrimmedValues() {
        val initial = ProfileUiState(
            fullName = "  Andra Pratama  ",
            email = "  andra@example.com  "
        )

        val updated = reduceProfileState(initial, ProfileAction.SaveChanges)

        assertEquals("Andra Pratama", updated.fullName)
        assertEquals("andra@example.com", updated.email)
        assertEquals(ProfileStatus.Updated, updated.status)
    }

    @Test
    fun saveChanges_withBlankName_setsEmptyNameStatus() {
        val initial = ProfileUiState(fullName = "   ", email = "andra@example.com")

        val updated = reduceProfileState(initial, ProfileAction.SaveChanges)

        assertEquals(ProfileStatus.EmptyName, updated.status)
    }

    @Test
    fun saveChanges_withInvalidEmail_setsInvalidEmailStatus() {
        val initial = ProfileUiState(fullName = "Andra", email = "andra-at-example.com")

        val updated = reduceProfileState(initial, ProfileAction.SaveChanges)

        assertEquals(ProfileStatus.InvalidEmail, updated.status)
    }
}

