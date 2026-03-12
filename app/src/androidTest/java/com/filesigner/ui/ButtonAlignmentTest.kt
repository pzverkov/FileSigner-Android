package com.filesigner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.filesigner.presentation.components.FilePickerButton
import com.filesigner.presentation.components.SignButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ButtonAlignmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- FilePickerButton tests ---

    @Test
    fun filePickerButton_rendersIconAndText() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}) }
        }
        composeTestRule.onNodeWithTag("filePickerButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filePickerButtonIcon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filePickerButtonText").assertIsDisplayed()
    }

    @Test
    fun filePickerButton_iconIsLeftOfText() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}) }
        }
        val iconBounds = composeTestRule.onNodeWithTag("filePickerButtonIcon").getBoundsInRoot()
        val textBounds = composeTestRule.onNodeWithTag("filePickerButtonText").getBoundsInRoot()
        assertTrue(
            "Icon right edge (${iconBounds.right}) should be left of text left edge (${textBounds.left})",
            iconBounds.right <= textBounds.left
        )
    }

    @Test
    fun filePickerButton_iconSizeIs18dp() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}) }
        }
        val iconBounds = composeTestRule.onNodeWithTag("filePickerButtonIcon").getBoundsInRoot()
        val iconWidth = iconBounds.right - iconBounds.left
        val iconHeight = iconBounds.bottom - iconBounds.top
        assertEquals(18.0, iconWidth.value.toDouble(), 1.5)
        assertEquals(18.0, iconHeight.value.toDouble(), 1.5)
    }

    @Test
    fun filePickerButton_spacingBetweenIconAndTextIs8dp() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}) }
        }
        val iconBounds = composeTestRule.onNodeWithTag("filePickerButtonIcon").getBoundsInRoot()
        val textBounds = composeTestRule.onNodeWithTag("filePickerButtonText").getBoundsInRoot()
        val spacing = textBounds.left - iconBounds.right
        assertEquals(8.0, spacing.value.toDouble(), 1.5)
    }

    @Test
    fun filePickerButton_heightMeetsM3TouchTarget() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}) }
        }
        composeTestRule.onNodeWithTag("filePickerButton").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun filePickerButton_enabledState() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}, enabled = true) }
        }
        composeTestRule.onNodeWithTag("filePickerButton").assertIsEnabled()
    }

    @Test
    fun filePickerButton_disabledState() {
        composeTestRule.setContent {
            MaterialTheme { FilePickerButton(onClick = {}, enabled = false) }
        }
        composeTestRule.onNodeWithTag("filePickerButton").assertIsNotEnabled()
    }

    // --- SignButton tests ---

    @Test
    fun signButton_rendersIconAndText() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = true) }
        }
        composeTestRule.onNodeWithTag("signButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signButtonIcon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signButtonText").assertIsDisplayed()
    }

    @Test
    fun signButton_iconIsLeftOfText() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = true) }
        }
        val iconBounds = composeTestRule.onNodeWithTag("signButtonIcon").getBoundsInRoot()
        val textBounds = composeTestRule.onNodeWithTag("signButtonText").getBoundsInRoot()
        assertTrue(
            "Icon right edge (${iconBounds.right}) should be left of text left edge (${textBounds.left})",
            iconBounds.right <= textBounds.left
        )
    }

    @Test
    fun signButton_iconSizeIs18dp() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = true) }
        }
        val iconBounds = composeTestRule.onNodeWithTag("signButtonIcon").getBoundsInRoot()
        val iconWidth = iconBounds.right - iconBounds.left
        val iconHeight = iconBounds.bottom - iconBounds.top
        assertEquals(18.0, iconWidth.value.toDouble(), 1.5)
        assertEquals(18.0, iconHeight.value.toDouble(), 1.5)
    }

    @Test
    fun signButton_spacingBetweenIconAndTextIs8dp() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = true) }
        }
        val iconBounds = composeTestRule.onNodeWithTag("signButtonIcon").getBoundsInRoot()
        val textBounds = composeTestRule.onNodeWithTag("signButtonText").getBoundsInRoot()
        val spacing = textBounds.left - iconBounds.right
        assertEquals(8.0, spacing.value.toDouble(), 1.5)
    }

    @Test
    fun signButton_heightMeetsM3TouchTarget() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = true) }
        }
        composeTestRule.onNodeWithTag("signButton").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun signButton_enabledState() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = true) }
        }
        composeTestRule.onNodeWithTag("signButton").assertIsEnabled()
    }

    @Test
    fun signButton_disabledState() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = false, enabled = false) }
        }
        composeTestRule.onNodeWithTag("signButton").assertIsNotEnabled()
    }

    @Test
    fun signButton_loadingShowsSpinnerHidesIconAndText() {
        composeTestRule.setContent {
            MaterialTheme { SignButton(onClick = {}, isLoading = true, enabled = true) }
        }
        composeTestRule.onNodeWithTag("signButtonSpinner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signButtonIcon").assertDoesNotExist()
        composeTestRule.onNodeWithTag("signButtonText").assertDoesNotExist()
    }

    // --- Cross-button consistency tests ---

    @Test
    fun bothButtons_haveConsistentHeight() {
        composeTestRule.setContent {
            MaterialTheme {
                FilePickerButton(onClick = {})
                SignButton(onClick = {}, isLoading = false, enabled = true)
            }
        }
        val fpBounds = composeTestRule.onNodeWithTag("filePickerButton").getBoundsInRoot()
        val signBounds = composeTestRule.onNodeWithTag("signButton").getBoundsInRoot()
        val fpHeight = fpBounds.bottom - fpBounds.top
        val signHeight = signBounds.bottom - signBounds.top
        assertEquals(fpHeight.value.toDouble(), signHeight.value.toDouble(), 2.0)
    }

    @Test
    fun bothButtons_useIconLeftLayoutDirection() {
        composeTestRule.setContent {
            MaterialTheme {
                FilePickerButton(onClick = {})
                SignButton(onClick = {}, isLoading = false, enabled = true)
            }
        }
        // FilePickerButton: icon left of text
        val fpIconBounds = composeTestRule.onNodeWithTag("filePickerButtonIcon").getBoundsInRoot()
        val fpTextBounds = composeTestRule.onNodeWithTag("filePickerButtonText").getBoundsInRoot()
        assertTrue(fpIconBounds.right <= fpTextBounds.left)

        // SignButton: icon left of text
        val signIconBounds = composeTestRule.onNodeWithTag("signButtonIcon").getBoundsInRoot()
        val signTextBounds = composeTestRule.onNodeWithTag("signButtonText").getBoundsInRoot()
        assertTrue(signIconBounds.right <= signTextBounds.left)
    }
}
