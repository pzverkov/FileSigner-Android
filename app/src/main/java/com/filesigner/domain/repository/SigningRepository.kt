package com.filesigner.domain.repository

import java.io.InputStream

interface SigningRepository {
    suspend fun generateKeyPairIfNeeded(): Result<Unit>
    suspend fun signStream(input: InputStream): Result<ByteArray>
    suspend fun verifyStream(input: InputStream, signature: ByteArray): Result<Boolean>
    suspend fun hasSigningKey(): Boolean
    fun getPublicKeyEncoded(): ByteArray?
}
