package com.filesigner.data.repository

import com.filesigner.data.source.KeystoreDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.KeyPair

class SigningRepositoryImplTest {

    private lateinit var keystoreDataSource: KeystoreDataSource
    private lateinit var signingRepository: SigningRepositoryImpl

    @Before
    fun setUp() {
        keystoreDataSource = mockk()
        signingRepository = SigningRepositoryImpl(keystoreDataSource)
    }

    @Test
    fun `generateKeyPairIfNeeded returns success when key already exists`() = runTest {
        // Given
        every { keystoreDataSource.hasKey() } returns true

        // When
        val result = signingRepository.generateKeyPairIfNeeded()

        // Then
        assertTrue(result.isSuccess)
        verify(exactly = 0) { keystoreDataSource.generateKeyPair() }
    }

    @Test
    fun `generateKeyPairIfNeeded generates key when not exists`() = runTest {
        // Given
        val mockKeyPair = mockk<KeyPair>()
        every { keystoreDataSource.hasKey() } returns false
        every { keystoreDataSource.generateKeyPair() } returns Result.success(mockKeyPair)

        // When
        val result = signingRepository.generateKeyPairIfNeeded()

        // Then
        assertTrue(result.isSuccess)
        verify(exactly = 1) { keystoreDataSource.generateKeyPair() }
    }

    @Test
    fun `generateKeyPairIfNeeded returns failure when generation fails`() = runTest {
        // Given
        every { keystoreDataSource.hasKey() } returns false
        every { keystoreDataSource.generateKeyPair() } returns Result.failure(Exception("Key gen failed"))

        // When
        val result = signingRepository.generateKeyPairIfNeeded()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Key gen failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signStream returns signature bytes on success`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream("test data".toByteArray())
        val expectedSignature = byteArrayOf(10, 20, 30, 40)
        every { keystoreDataSource.signStream(any()) } returns Result.success(expectedSignature)

        // When
        val result = signingRepository.signStream(inputStream)

        // Then
        assertTrue(result.isSuccess)
        assertArrayEquals(expectedSignature, result.getOrNull())
    }

    @Test
    fun `signStream returns failure when signing fails`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream("test data".toByteArray())
        every { keystoreDataSource.signStream(any()) } returns Result.failure(Exception("Signing error"))

        // When
        val result = signingRepository.signStream(inputStream)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Signing error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `hasSigningKey returns true when key exists`() = runTest {
        // Given
        every { keystoreDataSource.hasKey() } returns true

        // When
        val result = signingRepository.hasSigningKey()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasSigningKey returns false when key does not exist`() = runTest {
        // Given
        every { keystoreDataSource.hasKey() } returns false

        // When
        val result = signingRepository.hasSigningKey()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getPublicKeyEncoded returns encoded key`() {
        // Given
        val expectedBytes = byteArrayOf(1, 2, 3, 4, 5)
        every { keystoreDataSource.getPublicKeyEncoded() } returns expectedBytes

        // When
        val result = signingRepository.getPublicKeyEncoded()

        // Then
        assertArrayEquals(expectedBytes, result)
    }

    @Test
    fun `getPublicKeyEncoded returns null when no key`() {
        // Given
        every { keystoreDataSource.getPublicKeyEncoded() } returns null

        // When
        val result = signingRepository.getPublicKeyEncoded()

        // Then
        assertNull(result)
    }

    @Test
    fun `verifyStream returns true when signature is valid`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream("test data".toByteArray())
        val signature = byteArrayOf(10, 20, 30, 40)
        every { keystoreDataSource.verifyStream(any(), any()) } returns Result.success(true)

        // When
        val result = signingRepository.verifyStream(inputStream, signature)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `verifyStream returns false when signature is invalid`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream("test data".toByteArray())
        val signature = byteArrayOf(10, 20, 30, 40)
        every { keystoreDataSource.verifyStream(any(), any()) } returns Result.success(false)

        // When
        val result = signingRepository.verifyStream(inputStream, signature)

        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
    }

    @Test
    fun `verifyStream returns failure when verification fails`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream("test data".toByteArray())
        val signature = byteArrayOf(10, 20, 30, 40)
        every { keystoreDataSource.verifyStream(any(), any()) } returns Result.failure(Exception("Verification error"))

        // When
        val result = signingRepository.verifyStream(inputStream, signature)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Verification error", result.exceptionOrNull()?.message)
    }
}
