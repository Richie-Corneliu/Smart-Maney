package com.kelompok4.smartmaney.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kelompok4.smartmaney.MainActivity
import org.junit.Rule
import org.junit.Test

class AppNavHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginButton_navigatesToDashboardScreen() {
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Spending Distribution").assertIsDisplayed()
    }

    @Test
    fun walletTab_navigatesToWalletScreen() {
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("Wallet").performClick()

        composeTestRule.onNodeWithText("Current Balance").assertIsDisplayed()
    }

    @Test
    fun profileTab_navigatesToProfileScreen() {
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("Profile").performClick()

        composeTestRule.onNodeWithText("My Profile").assertIsDisplayed()
    }
}

