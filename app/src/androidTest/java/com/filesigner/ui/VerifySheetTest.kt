package com.filesigner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.filesigner.presentation.VerificationUiState
import com.filesigner.presentation.components.VerifySheetContent
import org.junit.Rule
import org.junit.Test

class VerifySheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        verificationState: VerificationUiState = VerificationUiState.Idle,
        onSelectFile: () -> Unit = {},
        onSelectSignature: () -> Unit = {},
        onVerify: (String, String) -> Unit = { _, _ -> },
        onDismissVerification: () -> Unit = {},
        selectedFileUri: String? = null,
        selectedFileName: String? = null,
        selectedSigUri: String? = null,
        selectedSigName: String? = null
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                VerifySheetContent(
                    verificationState = verificationState,
                    onSelectFile = onSelectFile,
                    onSelectSignature = onSelectSignature,
                    onVerify = onVerify,
                    onDismissVerification = onDismissVerification,
                    selectedFileUri = selectedFileUri,
                    selectedFileName = selectedFileName,
                    selectedSigUri = selectedSigUri,
                    selectedSigName = selectedSigName
                )
            }
        }
    }

    @Test
    fun verifySheet_rendersTitle() {
        setContent()
        composeTestRule.onNodeWithTag("verifySheetTitle").assertIsDisplayed()
    }

    @Test
    fun verifySheet_rendersFileAndSigButtons() {
        setContent()
        composeTestRule.onNodeWithTag("verifySelectFileButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verifySelectSigButton").assertIsDisplayed()
    }

    @Test
    fun verifySheet_verifyButtonDisabledByDefault() {
        setContent()
        composeTestRule.onNodeWithTag("verifyButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verifyButton").assertIsNotEnabled()
    }

    @Test
    fun verifySheet_verifyButtonEnabledWhenBothSelected() {
        setContent(
            selectedFileUri = "content://test/file",
            selectedFileName = "test.pdf",
            selectedSigUri = "content://test/file.sig",
            selectedSigName = "test.pdf.sig"
        )
        composeTestRule.onNodeWithTag("verifyButton").assertIsEnabled()
    }

    @Test
    fun verifySheet_fileSelectionShowsName() {
        setContent(
            selectedFileUri = "content://test/file",
            selectedFileName = "document.pdf"
        )
        composeTestRule.onNodeWithTag("verifyFileName").assertIsDisplayed()
    }

    @Test
    fun verifySheet_signatureSelectionShowsName() {
        setContent(
            selectedSigUri = "content://test/file.sig",
            selectedSigName = "document.pdf.sig"
        )
        composeTestRule.onNodeWithTag("verifySigName").assertIsDisplayed()
    }
}
