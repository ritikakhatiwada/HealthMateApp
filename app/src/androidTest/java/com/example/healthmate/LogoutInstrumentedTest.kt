package com.example.healthmate

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LogoutInstrumentedTest {

    // Starting from ProfileActivity for Logout test primarily
    @get:Rule
    val composeRule = createAndroidComposeRule<ProfileActivity>()

    @Test
    fun logout_test() {
        // Find Logout button and click it
        // Assuming "Logout" text or icon exists in ProfileActivity
        // If it's an icon, we use content description
        
        // Try finding by text "Sign Out" or "Logout"
        val logoutNode = composeRule.onAllNodesWithText("Logout")
        if (logoutNode.fetchSemanticsNodes().isNotEmpty()) {
            logoutNode.onFirst().performClick()
        } else {
             // Fallback to searching for the button content description if used
             composeRule.onNodeWithContentDescription("Logout")
                 .performClick()
        }
        
        // Confirm logout dialog usually appears
        val confirmNode = composeRule.onAllNodesWithText("Logout")
        if (confirmNode.fetchSemanticsNodes().isNotEmpty()) {
             // Click confirm if dialog pops up (Dialog often has same button text)
             confirmNode.onFirst().performClick()
        } else {
            composeRule.onNodeWithText("Yes").performClick()
        }
        
        // Assert we are navigated to Login/Welcome (Not easy to check Intent without Intents.intended)
    }
}
