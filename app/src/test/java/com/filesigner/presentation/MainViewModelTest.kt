package com.filesigner.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.filesigner.domain.model.FileInfo
import com.filesigner.domain.model.SigningResult
import com.filesigner.domain.usecase.GenerateKeyPairUseCase
import com.filesigner.domain.usecase.GetFileInfoUseCase
import com.filesigner.domain.usecase.SignFileUseCase
import com.filesigner.domain.usecase.VerifyFileUseCase
import com.filesigner.domain.usecase.VerifyResult
import com.filesigner.util.SamplePdfGenerator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var signFileUseCase: SignFileUseCase
    private lateinit var getFileInfoUseCase: GetFileInfoUseCase
    private lateinit var generateKeyPairUseCase: GenerateKeyPairUseCase
    private lateinit var verifyFileUseCase: VerifyFileUseCase
    private lateinit var samplePdfGenerator: SamplePdfGenerator
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: MainViewModel
    private lateinit var mockUri: Uri
    private val testUriString = "content://test/file.pdf"
    private val testSignatureUriString = "content://test/file.pdf.sig"
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        signFileUseCase = mockk()
        getFileInfoUseCase = mockk()
        generateKeyPairUseCase = mockk()
        verifyFileUseCase = mockk()
        samplePdfGenerator = mockk()
        savedStateHandle = SavedStateHandle()

        mockkStatic(Uri::class)
        mockUri = mockk()
        every { mockUri.toString() } returns testUriString

        coEvery { generateKeyPairUseCase() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(handle: SavedStateHandle = savedStateHandle) = MainViewModel(
        signFileUseCase, getFileInfoUseCase, generateKeyPairUseCase, verifyFileUseCase, samplePdfGenerator, handle
    )

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.signingState is SigningUiState.Idle)
    }

    @Test
    fun `onFileSelected updates state to FileSelected`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.signingState
        assertTrue(state is SigningUiState.FileSelected)
        assertEquals(fileInfo, (state as SigningUiState.FileSelected).fileInfo)
    }

    @Test
    fun `onFileSelected updates state to Error when file access fails`() = runTest {
        coEvery { getFileInfoUseCase(testUriString) } returns Result.failure(Exception("Access denied"))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.signingState
        assertTrue(state is SigningUiState.Error)
        assertEquals("Access denied", (state as SigningUiState.Error).errorMessage)
    }

    @Test
    fun `onSignFile transitions through Signing to Success`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        val signingResult = SigningResult.Success(
            originalFileUri = testUriString,
            signatureUri = testSignatureUriString,
            signatureBase64 = "dGVzdA=="
        )

        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)
        coEvery { signFileUseCase(testUriString) } returns signingResult

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.signingState is SigningUiState.FileSelected)

            viewModel.onSignFile()

            val signing = awaitItem()
            assertTrue(signing.signingState is SigningUiState.Signing)

            val success = awaitItem()
            assertTrue(success.signingState is SigningUiState.Success)
            assertEquals(testSignatureUriString, (success.signingState as SigningUiState.Success).signatureUri)
        }
    }

    @Test
    fun `onSignFile transitions to Error on failure`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        val signingResult = SigningResult.Error.SigningFailed("Crypto error")

        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)
        coEvery { signFileUseCase(testUriString) } returns signingResult

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSignFile()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.signingState
        assertTrue(state is SigningUiState.Error)
        assertEquals("Crypto error", (state as SigningUiState.Error).errorMessage)
    }

    @Test
    fun `resetState returns to Idle`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetState()

        assertTrue(viewModel.uiState.value.signingState is SigningUiState.Idle)
    }

    @Test
    fun `dismissError returns to FileSelected when file info exists`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        val signingResult = SigningResult.Error.SigningFailed("Error")

        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)
        coEvery { signFileUseCase(testUriString) } returns signingResult

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSignFile()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.signingState is SigningUiState.Error)

        viewModel.dismissError()

        val state = viewModel.uiState.value.signingState
        assertTrue(state is SigningUiState.FileSelected)
    }

    @Test
    fun `onPermissionResult updates permission state correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPermissionResult(isGranted = true, shouldShowRationale = false)
        assertEquals(PermissionState.Granted, viewModel.uiState.value.permissionState)

        viewModel.onPermissionResult(isGranted = false, shouldShowRationale = true)
        assertEquals(PermissionState.ShowRationale, viewModel.uiState.value.permissionState)

        viewModel.onPermissionResult(isGranted = false, shouldShowRationale = false)
        assertEquals(PermissionState.PermanentlyDenied, viewModel.uiState.value.permissionState)
    }

    @Test
    fun `restores file selection from SavedStateHandle`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)

        val handle = SavedStateHandle(mapOf("selected_file_uri" to testUriString))
        viewModel = createViewModel(handle)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.signingState
        assertTrue(state is SigningUiState.FileSelected)
        assertEquals(fileInfo, (state as SigningUiState.FileSelected).fileInfo)
    }

    @Test
    fun `resetState clears saved file URI`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)

        val handle = SavedStateHandle(mapOf("selected_file_uri" to testUriString))
        viewModel = createViewModel(handle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetState()

        assertTrue(viewModel.uiState.value.signingState is SigningUiState.Idle)
        assertEquals(null, handle.get<String>("selected_file_uri"))
    }

    @Test
    fun `onVerifySignature updates state to Valid on success`() = runTest {
        coEvery { verifyFileUseCase(testUriString, testSignatureUriString) } returns VerifyResult.Valid

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(VerificationUiState.Idle, initial.verificationState)

            viewModel.onVerifySignature(testUriString, testSignatureUriString)

            val verifying = awaitItem()
            assertEquals(VerificationUiState.Verifying, verifying.verificationState)

            val valid = awaitItem()
            assertEquals(VerificationUiState.Valid, valid.verificationState)
        }
    }

    @Test
    fun `onVerifySignature updates state to Invalid when signature mismatch`() = runTest {
        coEvery { verifyFileUseCase(testUriString, testSignatureUriString) } returns VerifyResult.Invalid

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVerifySignature(testUriString, testSignatureUriString)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VerificationUiState.Invalid, viewModel.uiState.value.verificationState)
    }

    @Test
    fun `onVerifySignature updates state to Error on failure`() = runTest {
        coEvery { verifyFileUseCase(testUriString, testSignatureUriString) } returns VerifyResult.Error("File not found")

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVerifySignature(testUriString, testSignatureUriString)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.verificationState
        assertTrue(state is VerificationUiState.Error)
        assertEquals("File not found", (state as VerificationUiState.Error).message)
    }

    @Test
    fun `onDismissVerification resets verification state to Idle`() = runTest {
        coEvery { verifyFileUseCase(testUriString, testSignatureUriString) } returns VerifyResult.Valid

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVerifySignature(testUriString, testSignatureUriString)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(VerificationUiState.Valid, viewModel.uiState.value.verificationState)

        viewModel.onDismissVerification()
        assertEquals(VerificationUiState.Idle, viewModel.uiState.value.verificationState)
    }

    @Test
    fun `signing adds entry to history`() = runTest {
        val fileInfo = FileInfo(testUriString, "test.pdf", 1024L, "application/pdf")
        val signingResult = SigningResult.Success(
            originalFileUri = testUriString,
            signatureUri = testSignatureUriString,
            signatureBase64 = "dGVzdA=="
        )

        coEvery { getFileInfoUseCase(testUriString) } returns Result.success(fileInfo)
        coEvery { signFileUseCase(testUriString) } returns signingResult

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFileSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSignFile()
        testDispatcher.scheduler.advanceUntilIdle()

        val history = viewModel.uiState.value.history
        assertEquals(1, history.size)
        assertEquals("test.pdf", history[0].fileName)
        assertEquals(testUriString, history[0].fileUri)
        assertEquals(testSignatureUriString, history[0].signatureUri)
    }
}
