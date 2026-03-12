package com.filesigner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.filesigner.presentation.components.AboutSheetContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AboutSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testVersion = "1.2.3"

    @Test
    fun aboutSheet_rendersAppNameAndVersion() {
        composeTestRule.setContent {
            MaterialTheme { AboutSheetContent(versionName = testVersion) }
        }
        composeTestRule.onNodeWithTag("aboutSheetTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("aboutSheetVersion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version $testVersion").assertIsDisplayed()
    }

    @Test
    fun aboutSheet_rendersDescription() {
        composeTestRule.setContent {
            MaterialTheme { AboutSheetContent(versionName = testVersion) }
        }
        composeTestRule.onNodeWithTag("aboutSheetDescription").assertIsDisplayed()
    }

    @Test
    fun aboutSheet_rendersLicenseSection() {
        composeTestRule.setContent {
            MaterialTheme { AboutSheetContent(versionName = testVersion) }
        }
        composeTestRule.onNodeWithTag("aboutSheetLicensesSection").assertIsDisplayed()
        composeTestRule.onNodeWithTag("aboutSheetLicenseLink").assertIsDisplayed()
    }

    @Test
    fun aboutSheet_rendersAllLibraries() {
        composeTestRule.setContent {
            MaterialTheme { AboutSheetContent(versionName = testVersion) }
        }
        val expectedLibraries = listOf(
            "AndroidX Core KTX",
            "AndroidX Lifecycle Runtime KTX",
            "AndroidX Activity Compose",
            "Jetpack Compose (BOM)",
            "AndroidX Lifecycle ViewModel Compose",
            "AndroidX Lifecycle Runtime Compose",
            "Dagger Hilt Android",
            "AndroidX Hilt Navigation Compose",
            "Timber",
            "Material3 Adaptive",
            "Material3 Window Size Class"
        )
        expectedLibraries.forEach { libName ->
            composeTestRule.onNodeWithText(libName, substring = true).assertIsDisplayed()
        }
    }

    @Test
    fun aboutSheet_rendersDeveloperSection() {
        composeTestRule.setContent {
            MaterialTheme { AboutSheetContent(versionName = testVersion) }
        }
        composeTestRule.onNodeWithTag("aboutSheetDeveloperSection").assertIsDisplayed()
        composeTestRule.onNodeWithTag("aboutSheetGithubLink").assertIsDisplayed()
    }

    @Test
    fun aboutSheet_licenseLinkIsClickable() {
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                AboutSheetContent(
                    versionName = testVersion,
                    onLicenseLinkClick = { clicked = true }
                )
            }
        }
        composeTestRule.onNodeWithTag("aboutSheetLicenseLink").performClick()
        assertTrue("License link click callback should have been invoked", clicked)
    }

    @Test
    fun aboutSheet_githubLinkIsClickable() {
        var clickedUrl = ""
        composeTestRule.setContent {
            MaterialTheme {
                AboutSheetContent(
                    versionName = testVersion,
                    onGithubLinkClick = { url -> clickedUrl = url }
                )
            }
        }
        composeTestRule.onNodeWithTag("aboutSheetGithubLink").performClick()
        assertEquals("https://github.com/pzverkov", clickedUrl)
    }
}
