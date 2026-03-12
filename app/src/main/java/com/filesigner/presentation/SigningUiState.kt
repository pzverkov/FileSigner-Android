package com.filesigner.presentation

import com.filesigner.domain.model.FileInfo
import com.filesigner.domain.model.SigningHistoryEntry

sealed interface SigningUiState {
    data object Idle : SigningUiState

    data class FileSelected(
        val fileInfo: FileInfo
    ) : SigningUiState

    data class Signing(
        val fileInfo: FileInfo
    ) : SigningUiState

    data class Success(
        val fileInfo: FileInfo,
        val signatureUri: String,
        val signatureBase64: String
    ) : SigningUiState

    data class Error(
        val fileInfo: FileInfo?,
        val errorMessage: String
    ) : SigningUiState
}

sealed interface PermissionState {
    data object NotRequested : PermissionState
    data object Granted : PermissionState
    data object Denied : PermissionState
    data object PermanentlyDenied : PermissionState
    data object ShowRationale : PermissionState
}

sealed interface VerificationUiState {
    data object Idle : VerificationUiState
    data object Verifying : VerificationUiState
    data object Valid : VerificationUiState
    data object Invalid : VerificationUiState
    data class Error(val message: String) : VerificationUiState
}

data class MainUiState(
    val signingState: SigningUiState = SigningUiState.Idle,
    val permissionState: PermissionState = PermissionState.NotRequested,
    val verificationState: VerificationUiState = VerificationUiState.Idle,
    val history: List<SigningHistoryEntry> = emptyList()
)
