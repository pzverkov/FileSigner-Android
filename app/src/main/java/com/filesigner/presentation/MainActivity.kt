package com.filesigner.presentation

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filesigner.BuildConfig
import com.filesigner.R
import com.filesigner.presentation.components.AboutSheet
import com.filesigner.presentation.components.FilePickerButton
import com.filesigner.presentation.components.PermissionRationaleDialog
import com.filesigner.presentation.components.SignButton
import com.filesigner.presentation.components.SigningHistorySheet
import com.filesigner.presentation.components.StatusDisplay
import com.filesigner.presentation.components.VerifySheet
import com.filesigner.presentation.theme.FileSignerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FileSignerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FileSignerScreen()
                }
            }
        }
    }
}

@Composable
fun FileSignerScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var showPermissionDialog by remember { mutableStateOf(false) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable URI permission so it survives process death
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions - that's fine
            }
            viewModel.onFileSelected(it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } ?: false

        viewModel.onPermissionResult(isGranted, shouldShowRationale)

        if (isGranted) {
            filePickerLauncher.launch(arrayOf("*/*"))
        } else if (!shouldShowRationale && !isGranted) {
            isPermanentlyDenied = true
            showPermissionDialog = true
        } else {
            showPermissionDialog = true
        }
    }

    val launchFilePicker: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            filePickerLauncher.launch(arrayOf("*/*"))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filePickerLauncher.launch(arrayOf("*/*"))
        } else {
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } ?: false

            if (shouldShowRationale) {
                showPermissionDialog = true
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Standalone verify flow state
    var showVerifySheet by remember { mutableStateOf(false) }
    var verifyFileUri by remember { mutableStateOf<String?>(null) }
    var verifyFileName by remember { mutableStateOf<String?>(null) }
    var verifySigUri by remember { mutableStateOf<String?>(null) }
    var verifySigName by remember { mutableStateOf<String?>(null) }

    val verifyFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            verifyFileUri = it.toString()
            verifyFileName = getDisplayName(context.contentResolver, it)
        }
    }

    val verifySignaturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            verifySigUri = it.toString()
            verifySigName = getDisplayName(context.contentResolver, it)
        }
    }

    if (showVerifySheet) {
        VerifySheet(
            verificationState = uiState.verificationState,
            onSelectFile = { verifyFilePickerLauncher.launch(arrayOf("*/*")) },
            onSelectSignature = { verifySignaturePickerLauncher.launch(arrayOf("*/*")) },
            onVerify = viewModel::onVerifySignature,
            onDismissVerification = viewModel::onDismissVerification,
            onDismiss = {
                showVerifySheet = false
                verifyFileUri = null
                verifyFileName = null
                verifySigUri = null
                verifySigName = null
                viewModel.onDismissVerification()
            },
            selectedFileUri = verifyFileUri,
            selectedFileName = verifyFileName,
            selectedSigUri = verifySigUri,
            selectedSigName = verifySigName
        )
    }

    if (showPermissionDialog) {
        PermissionRationaleDialog(
            onDismiss = {
                showPermissionDialog = false
                isPermanentlyDenied = false
            },
            onConfirm = {
                showPermissionDialog = false
                if (isPermanentlyDenied) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    isPermanentlyDenied = false
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            },
            isPermanentlyDenied = isPermanentlyDenied
        )
    }

    FileSignerContent(
        uiState = uiState,
        onChooseFile = launchFilePicker,
        onSignFile = viewModel::onSignFile,
        onCancelSigning = viewModel::onCancelSigning,
        onVerifySignature = viewModel::onVerifySignature,
        onDismissVerification = viewModel::onDismissVerification,
        onGenerateSamplePdf = viewModel::onGenerateSamplePdf,
        onVerifyFlow = { showVerifySheet = true },
        showDevMenu = BuildConfig.DEBUG
    )
}

@Composable
private fun FileSignerContent(
    uiState: MainUiState,
    onChooseFile: () -> Unit,
    onSignFile: () -> Unit,
    onCancelSigning: () -> Unit,
    onVerifySignature: (fileUri: String, signatureUri: String) -> Unit,
    onDismissVerification: () -> Unit,
    onGenerateSamplePdf: () -> Unit,
    onVerifyFlow: () -> Unit,
    showDevMenu: Boolean = false
) {
    val isLoading = uiState.signingState is SigningUiState.Signing
    val canSign = uiState.signingState is SigningUiState.FileSelected ||
            uiState.signingState is SigningUiState.Error ||
            uiState.signingState is SigningUiState.Success

    var showHistorySheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }

    if (showAboutSheet) {
        AboutSheet(
            versionName = BuildConfig.VERSION_NAME,
            onDismiss = { showAboutSheet = false }
        )
    }

    if (showHistorySheet) {
        SigningHistorySheet(
            history = uiState.history,
            verificationState = uiState.verificationState,
            onVerify = onVerifySignature,
            onDismissVerification = onDismissVerification,
            onDismiss = {
                showHistorySheet = false
                onDismissVerification()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            IconButton(
                onClick = { showAboutSheet = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.about),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status Display
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            StatusDisplay(
                state = uiState.signingState,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Signing status" }
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilePickerButton(
                onClick = onChooseFile,
                enabled = !isLoading
            )

            if (isLoading) {
                SignButton(
                    onClick = {},
                    isLoading = true,
                    enabled = false
                )
                TextButton(
                    onClick = onCancelSigning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel_signing))
                }
            } else {
                SignButton(
                    onClick = onSignFile,
                    isLoading = false,
                    enabled = canSign
                )
            }

            OutlinedButton(
                onClick = onVerifyFlow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = stringResource(R.string.verify_signature),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.verify_signature),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (uiState.history.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.history),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (showDevMenu) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                OutlinedButton(
                    onClick = onGenerateSamplePdf,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = stringResource(R.string.dev_generate_sample_pdf),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dev_generate_sample_pdf),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getDisplayName(contentResolver: ContentResolver, uri: Uri): String {
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return uri.lastPathSegment ?: uri.toString()
}
