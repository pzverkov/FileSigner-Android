package com.filesigner.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.filesigner.R
import com.filesigner.presentation.VerificationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySheet(
    verificationState: VerificationUiState,
    onSelectFile: () -> Unit,
    onSelectSignature: () -> Unit,
    onVerify: (fileUri: String, signatureUri: String) -> Unit,
    onDismissVerification: () -> Unit,
    onDismiss: () -> Unit,
    selectedFileUri: String?,
    selectedFileName: String?,
    selectedSigUri: String?,
    selectedSigName: String?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
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

@Composable
internal fun VerifySheetContent(
    verificationState: VerificationUiState,
    onSelectFile: () -> Unit,
    onSelectSignature: () -> Unit,
    onVerify: (fileUri: String, signatureUri: String) -> Unit,
    onDismissVerification: () -> Unit,
    selectedFileUri: String?,
    selectedFileName: String?,
    selectedSigUri: String?,
    selectedSigName: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.verify_signature),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .testTag("verifySheetTitle")
                .semantics { heading() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Select file button
        OutlinedButton(
            onClick = onSelectFile,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verifySelectFileButton")
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = stringResource(R.string.verify_select_file),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.verify_select_file),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (selectedFileName != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.verify_file_selected, selectedFileName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("verifyFileName")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Select signature button
        OutlinedButton(
            onClick = onSelectSignature,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verifySelectSigButton")
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.verify_select_signature),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.verify_select_signature),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (selectedSigName != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.verify_signature_selected, selectedSigName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("verifySigName")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verify button
        val canVerify = selectedFileUri != null && selectedSigUri != null
        Button(
            onClick = { onVerify(selectedFileUri!!, selectedSigUri!!) },
            enabled = canVerify && verificationState !is VerificationUiState.Verifying,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verifyButton")
        ) {
            Text(
                text = stringResource(R.string.verify_ready),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Verification result
        if (verificationState != VerificationUiState.Idle) {
            Spacer(modifier = Modifier.height(12.dp))
            VerificationBanner(
                state = verificationState,
                onDismiss = onDismissVerification
            )
        }
    }
}
