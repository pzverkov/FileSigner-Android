package com.filesigner.data.source

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

class FileDataSourceTest {

    private lateinit var contentResolver: ContentResolver
    private lateinit var fileDataSource: FileDataSource
    private lateinit var mockUri: Uri

    @Before
    fun setUp() {
        contentResolver = mockk()
        fileDataSource = FileDataSource(contentResolver)

        mockkStatic(Uri::class)
        mockUri = mockk()
        every { mockUri.toString() } returns "content://test/document.pdf"
        every { mockUri.lastPathSegment } returns "document.pdf"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getFileInfo returns file info on success`() {
        val cursor = mockk<Cursor>()
        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 1
        every { cursor.getString(0) } returns "document.pdf"
        every { cursor.getLong(1) } returns 2048L
        every { contentResolver.getType(mockUri) } returns "application/pdf"
        every { cursor.close() } returns Unit

        val result = fileDataSource.getFileInfo(mockUri)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()!!
        assertEquals("document.pdf", fileInfo.name)
        assertEquals(2048L, fileInfo.size)
        assertEquals("application/pdf", fileInfo.mimeType)
    }

    @Test
    fun `getFileInfo returns failure when cursor is null`() {
        every { contentResolver.query(mockUri, null, null, null, null) } returns null

        val result = fileDataSource.getFileInfo(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundException)
    }

    @Test
    fun `getFileInfo returns failure when cursor is empty`() {
        val cursor = mockk<Cursor>()
        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns false
        every { cursor.close() } returns Unit

        val result = fileDataSource.getFileInfo(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundException)
    }

    @Test
    fun `getFileInfo uses lastPathSegment when column missing`() {
        val cursor = mockk<Cursor>()
        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns -1
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns -1
        every { contentResolver.getType(mockUri) } returns null
        every { cursor.close() } returns Unit

        val result = fileDataSource.getFileInfo(mockUri)

        assertTrue(result.isSuccess)
        assertEquals("document.pdf", result.getOrNull()?.name)
        assertEquals(0L, result.getOrNull()?.size)
    }

    @Test
    fun `getFileInfo handles SecurityException`() {
        every { contentResolver.query(mockUri, null, null, null, null) } throws SecurityException("Permission denied")

        val result = fileDataSource.getFileInfo(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun `openFileStream returns stream on success`() {
        val inputStream = ByteArrayInputStream("test content".toByteArray())
        every { contentResolver.openInputStream(mockUri) } returns inputStream

        val result = fileDataSource.openFileStream(mockUri)

        assertTrue(result.isSuccess)
        assertEquals("test content", result.getOrNull()?.bufferedReader()?.readText())
    }

    @Test
    fun `openFileStream returns failure when stream is null`() {
        every { contentResolver.openInputStream(mockUri) } returns null

        val result = fileDataSource.openFileStream(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundException)
    }

    @Test
    fun `openFileStream returns failure on exception`() {
        every { contentResolver.openInputStream(mockUri) } throws SecurityException("No access")

        val result = fileDataSource.openFileStream(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
