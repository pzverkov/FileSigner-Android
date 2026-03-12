package com.filesigner.domain.usecase

import com.filesigner.domain.model.FileInfo
import com.filesigner.domain.repository.FileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException

class GetFileInfoUseCaseTest {

    private lateinit var fileRepository: FileRepository
    private lateinit var getFileInfoUseCase: GetFileInfoUseCase
    private val testUri = "content://test/document.pdf"

    @Before
    fun setUp() {
        fileRepository = mockk()
        getFileInfoUseCase = GetFileInfoUseCase(fileRepository)
    }

    @Test
    fun `returns file info on success`() = runTest {
        // Given
        val expectedFileInfo = FileInfo(
            uri = testUri,
            name = "document.pdf",
            size = 1024L,
            mimeType = "application/pdf"
        )
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(expectedFileInfo)

        // When
        val result = getFileInfoUseCase(testUri)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedFileInfo, result.getOrNull())
        assertEquals("document.pdf", result.getOrNull()?.name)
        assertEquals(1024L, result.getOrNull()?.size)
        assertEquals("application/pdf", result.getOrNull()?.mimeType)
    }

    @Test
    fun `returns failure when file not found`() = runTest {
        // Given
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.failure(FileNotFoundException("File not found"))

        // When
        val result = getFileInfoUseCase(testUri)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundException)
    }

    @Test
    fun `file info calculates display size correctly for bytes`() = runTest {
        // Given
        val fileInfo = FileInfo(
            uri = testUri,
            name = "small.txt",
            size = 500L,
            mimeType = "text/plain"
        )
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo)

        // When
        val result = getFileInfoUseCase(testUri)

        // Then
        assertEquals("500 B", result.getOrNull()?.displaySize)
    }

    @Test
    fun `file info calculates display size correctly for kilobytes`() = runTest {
        // Given
        val fileInfo = FileInfo(
            uri = testUri,
            name = "medium.doc",
            size = 5120L, // 5 KB
            mimeType = "application/msword"
        )
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo)

        // When
        val result = getFileInfoUseCase(testUri)

        // Then
        assertEquals("5 KB", result.getOrNull()?.displaySize)
    }

    @Test
    fun `file info calculates display size correctly for megabytes`() = runTest {
        // Given
        val fileInfo = FileInfo(
            uri = testUri,
            name = "large.pdf",
            size = 5242880L, // 5 MB
            mimeType = "application/pdf"
        )
        coEvery { fileRepository.getFileInfo(testUri) } returns Result.success(fileInfo)

        // When
        val result = getFileInfoUseCase(testUri)

        // Then
        assertEquals("5 MB", result.getOrNull()?.displaySize)
    }
}
