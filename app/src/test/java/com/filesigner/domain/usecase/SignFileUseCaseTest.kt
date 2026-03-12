package com.filesigner.domain.usecase

import com.filesigner.domain.model.FileInfo
import com.filesigner.domain.model.SigningResult
import com.filesigner.domain.repository.FileRepository
import com.filesigner.domain.repository.SigningRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignFileUseCaseTest {

    private lateinit var signingRepository: SigningRepository
    private lateinit var fileRepository: FileRepository
    private lateinit var signFileUseCase: SignFileUseCase
    private val testUri = "content://test/file.pdf"
    private val testSignatureUri = "content://test/file.pdf.sig"

    @Before
    fun setUp() {
        signingRepository = mockk()
        fileRepository = mockk()
        signFileUseCase = SignFileUseCase(signingRepository, fileRepository)
    }

    private fun fileInfo(size: Long = 1024L) = FileInfo(testUri, "file.pdf", size, "application/pdf")

    @Test
    fun `sign file successfully when key exists`() = runTest {
        // Given
        val fileBytes = "test content".toByteArray()
        val signatureBytes = byteArrayOf(1, 2, 3, 4, 5)

        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo())
        coEvery { fileRepository.openFileStream(testUri) } returns Result.success(ByteArrayInputStream(fileBytes))
        coEvery { signingRepository.signStream(any<InputStream>()) } returns Result.success(signatureBytes)
        coEvery { fileRepository.saveSignature(testUri, signatureBytes) } returns Result.success(testSignatureUri)

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Success)
        val success = result as SigningResult.Success
        assertEquals(testUri, success.originalFileUri)
        assertEquals(testSignatureUri, success.signatureUri)
        assertEquals("SHA256withECDSA", success.algorithm)

        coVerify(exactly = 0) { signingRepository.generateKeyPairIfNeeded() }
    }

    @Test
    fun `sign file generates key when not exists`() = runTest {
        // Given
        val fileBytes = "test content".toByteArray()
        val signatureBytes = byteArrayOf(1, 2, 3, 4, 5)

        coEvery { signingRepository.hasSigningKey() } returns false
        coEvery { signingRepository.generateKeyPairIfNeeded() } returns Result.success(Unit)
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo())
        coEvery { fileRepository.openFileStream(testUri) } returns Result.success(ByteArrayInputStream(fileBytes))
        coEvery { signingRepository.signStream(any<InputStream>()) } returns Result.success(signatureBytes)
        coEvery { fileRepository.saveSignature(testUri, signatureBytes) } returns Result.success(testSignatureUri)

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Success)
        coVerify(exactly = 1) { signingRepository.generateKeyPairIfNeeded() }
    }

    @Test
    fun `returns KeyGenerationError when key generation fails`() = runTest {
        // Given
        coEvery { signingRepository.hasSigningKey() } returns false
        coEvery { signingRepository.generateKeyPairIfNeeded() } returns Result.failure(Exception("Key gen failed"))

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.KeyGenerationError)
    }

    @Test
    fun `returns FileNotFound when file does not exist`() = runTest {
        // Given
        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.failure(FileNotFoundException())

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.FileNotFound)
    }

    @Test
    fun `returns PermissionDenied when security exception occurs`() = runTest {
        // Given
        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.failure(SecurityException())

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.PermissionDenied)
    }

    @Test
    fun `returns FileReadError for other read exceptions`() = runTest {
        // Given
        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo())
        coEvery { fileRepository.openFileStream(testUri) } returns Result.failure(Exception("IO Error"))

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.FileReadError)
    }

    @Test
    fun `returns SigningFailed when signing operation fails`() = runTest {
        // Given
        val fileBytes = "test content".toByteArray()

        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo())
        coEvery { fileRepository.openFileStream(testUri) } returns Result.success(ByteArrayInputStream(fileBytes))
        coEvery { signingRepository.signStream(any<InputStream>()) } returns Result.failure(Exception("Signing failed"))

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.SigningFailed)
    }

    @Test
    fun `returns SignatureSaveError when saving signature fails`() = runTest {
        // Given
        val fileBytes = "test content".toByteArray()
        val signatureBytes = byteArrayOf(1, 2, 3, 4, 5)

        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo())
        coEvery { fileRepository.openFileStream(testUri) } returns Result.success(ByteArrayInputStream(fileBytes))
        coEvery { signingRepository.signStream(any<InputStream>()) } returns Result.success(signatureBytes)
        coEvery { fileRepository.saveSignature(testUri, signatureBytes) } returns Result.failure(Exception("Save failed"))

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.SignatureSaveError)
    }

    @Test
    fun `returns FileReadError when file exceeds max size`() = runTest {
        // Given
        val largeFileInfo = fileInfo(size = 600L * 1024 * 1024) // 600 MB

        coEvery { signingRepository.hasSigningKey() } returns true
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(largeFileInfo)

        // When
        val result = signFileUseCase(testUri)

        // Then
        assertTrue(result is SigningResult.Error.FileReadError)
        assertTrue((result as SigningResult.Error.FileReadError).message.contains("too large"))
    }
}
