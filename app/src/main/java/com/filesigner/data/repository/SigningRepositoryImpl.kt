package com.filesigner.data.repository

import com.filesigner.data.source.KeystoreDataSource
import com.filesigner.domain.repository.SigningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SigningRepositoryImpl @Inject constructor(
    private val keystoreDataSource: KeystoreDataSource
) : SigningRepository {

    override suspend fun generateKeyPairIfNeeded(): Result<Unit> = withContext(Dispatchers.IO) {
        if (keystoreDataSource.hasKey()) {
            Result.success(Unit)
        } else {
            keystoreDataSource.generateKeyPair().map { }
        }
    }

    override suspend fun signStream(input: InputStream): Result<ByteArray> = withContext(Dispatchers.IO) {
        keystoreDataSource.signStream(input)
    }

    override suspend fun verifyStream(input: InputStream, signature: ByteArray): Result<Boolean> =
        withContext(Dispatchers.IO) {
            keystoreDataSource.verifyStream(input, signature)
        }

    override suspend fun hasSigningKey(): Boolean = withContext(Dispatchers.IO) {
        keystoreDataSource.hasKey()
    }

    override fun getPublicKeyEncoded(): ByteArray? {
        return keystoreDataSource.getPublicKeyEncoded()
    }
}
