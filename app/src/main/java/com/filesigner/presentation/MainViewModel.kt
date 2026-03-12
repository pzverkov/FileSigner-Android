package com.filesigner.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filesigner.domain.model.SigningHistoryEntry
import com.filesigner.domain.model.SigningResult
import com.filesigner.domain.usecase.GenerateKeyPairUseCase
import com.filesigner.domain.usecase.GetFileInfoUseCase
import com.filesigner.domain.usecase.SignFileUseCase
import com.filesigner.domain.usecase.VerifyFileUseCase
import com.filesigner.domain.usecase.VerifyResult
import com.filesigner.util.SamplePdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signFileUseCase: SignFileUseCase,
    private val getFileInfoUseCase: GetFileInfoUseCase,
    private val generateKeyPairUseCase: GenerateKeyPairUseCase,
    private val verifyFileUseCase: VerifyFileUseCase,
    private val samplePdfGenerator: SamplePdfGenerator,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SELECTED_FILE_URI = "selected_file_uri"
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var signingJob: Job? = null

    init {
        initializeSigningKey()
        restoreState()
    }

    private fun initializeSigningKey() {
        viewModelScope.launch {
            generateKeyPairUseCase()
        }
    }

    private fun restoreState() {
        val savedUri = savedStateHandle.get<String>(KEY_SELECTED_FILE_URI)
        if (savedUri != null) {
            Timber.d("Restoring previously selected file")
            viewModelScope.launch {
                getFileInfoUseCase(savedUri).fold(
                    onSuccess = { fileInfo ->
                        _uiState.update { it.copy(signingState = SigningUiState.FileSelected(fileInfo)) }
                    },
                    onFailure = {
                        Timber.w(it, "Failed to restore file, clearing saved state")
                        savedStateHandle.remove<String>(KEY_SELECTED_FILE_URI)
                    }
                )
            }
        }
    }

    fun onFileSelected(uri: Uri) {
        val uriString = uri.toString()
        Timber.d("File selected")
        savedStateHandle[KEY_SELECTED_FILE_URI] = uriString
        viewModelScope.launch {
            getFileInfoUseCase(uriString).fold(
                onSuccess = { fileInfo ->
                    Timber.i("File info loaded: %s", fileInfo.displaySize)
                    _uiState.update { it.copy(signingState = SigningUiState.FileSelected(fileInfo)) }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to load file info")
                    savedStateHandle.remove<String>(KEY_SELECTED_FILE_URI)
                    _uiState.update {
                        it.copy(
                            signingState = SigningUiState.Error(
                                fileInfo = null,
                                errorMessage = exception.message ?: "Could not access file"
                            )
                        )
                    }
                }
            )
        }
    }

    fun onSignFile() {
        val currentState = _uiState.value.signingState
        val fileInfo = when (currentState) {
            is SigningUiState.FileSelected -> currentState.fileInfo
            is SigningUiState.Error -> currentState.fileInfo
            is SigningUiState.Success -> currentState.fileInfo
            else -> return
        } ?: return

        Timber.d("Signing file")
        signingJob?.cancel()
        signingJob = viewModelScope.launch {
            _uiState.update { it.copy(signingState = SigningUiState.Signing(fileInfo)) }

            when (val result = signFileUseCase(fileInfo.uri)) {
                is SigningResult.Success -> {
                    Timber.i("File signed successfully")
                    val entry = SigningHistoryEntry(
                        fileName = fileInfo.name,
                        fileUri = fileInfo.uri,
                        signatureUri = result.signatureUri,
                        timestamp = result.timestamp
                    )
                    _uiState.update {
                        it.copy(
                            signingState = SigningUiState.Success(
                                fileInfo = fileInfo,
                                signatureUri = result.signatureUri,
                                signatureBase64 = result.signatureBase64
                            ),
                            history = listOf(entry) + it.history
                        )
                    }
                }
                is SigningResult.Error -> {
                    Timber.e("Signing failed: %s", result.toDisplayMessage())
                    _uiState.update {
                        it.copy(
                            signingState = SigningUiState.Error(
                                fileInfo = fileInfo,
                                errorMessage = result.toDisplayMessage()
                            )
                        )
                    }
                }
            }
        }
    }

    fun onCancelSigning() {
        Timber.d("Signing cancelled by user")
        signingJob?.cancel()
        signingJob = null
        val currentState = _uiState.value.signingState
        if (currentState is SigningUiState.Signing) {
            _uiState.update {
                it.copy(signingState = SigningUiState.FileSelected(currentState.fileInfo))
            }
        }
    }

    fun onVerifySignature(fileUri: String, signatureUri: String) {
        Timber.d("Verifying signature")
        _uiState.update { it.copy(verificationState = VerificationUiState.Verifying) }
        viewModelScope.launch {
            when (val result = verifyFileUseCase(fileUri, signatureUri)) {
                is VerifyResult.Valid -> {
                    Timber.i("Signature valid")
                    _uiState.update { it.copy(verificationState = VerificationUiState.Valid) }
                }
                is VerifyResult.Invalid -> {
                    Timber.w("Signature invalid")
                    _uiState.update { it.copy(verificationState = VerificationUiState.Invalid) }
                }
                is VerifyResult.Error -> {
                    Timber.e("Verification error: %s", result.message)
                    _uiState.update { it.copy(verificationState = VerificationUiState.Error(result.message)) }
                }
            }
        }
    }

    fun onDismissVerification() {
        _uiState.update { it.copy(verificationState = VerificationUiState.Idle) }
    }

    fun onGenerateSamplePdf() {
        viewModelScope.launch {
            samplePdfGenerator.generateSamplePdf().fold(
                onSuccess = { uri -> onFileSelected(uri) },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to generate sample PDF")
                    _uiState.update {
                        it.copy(
                            signingState = SigningUiState.Error(
                                fileInfo = null,
                                errorMessage = exception.message ?: "Could not generate PDF"
                            )
                        )
                    }
                }
            )
        }
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        _uiState.update {
            it.copy(
                permissionState = when {
                    isGranted -> PermissionState.Granted
                    shouldShowRationale -> PermissionState.ShowRationale
                    else -> PermissionState.PermanentlyDenied
                }
            )
        }
    }

    fun onPermissionRationaleShown() {
        _uiState.update { it.copy(permissionState = PermissionState.Denied) }
    }

    fun resetState() {
        signingJob?.cancel()
        signingJob = null
        savedStateHandle.remove<String>(KEY_SELECTED_FILE_URI)
        _uiState.update { it.copy(signingState = SigningUiState.Idle) }
    }

    fun dismissError() {
        val currentState = _uiState.value.signingState
        if (currentState is SigningUiState.Error && currentState.fileInfo != null) {
            _uiState.update {
                it.copy(signingState = SigningUiState.FileSelected(currentState.fileInfo))
            }
        } else {
            resetState()
        }
    }
}
