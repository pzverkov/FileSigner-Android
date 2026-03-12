package com.filesigner.data.source

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import timber.log.Timber
import java.io.InputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreDataSource @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "file_signer_ecdsa_p256_v1"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val STREAM_BUFFER_SIZE = 8192
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    fun hasKey(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Timber.w(e, "Failed to check keystore for alias")
            false
        }
    }

    fun generateKeyPair(): Result<KeyPair> {
        Timber.d("Generating ECDSA P-256 key pair")
        return try {
            val keyPair = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Try StrongBox first (hardware-backed secure element)
                try {
                    generateKeyPairWithSpec(strongBox = true)
                } catch (e: StrongBoxUnavailableException) {
                    Timber.i("StrongBox unavailable, falling back to TEE-backed keystore")
                    generateKeyPairWithSpec(strongBox = false)
                }
            } else {
                generateKeyPairWithSpec(strongBox = false)
            }

            Timber.i("Key pair generated successfully")
            Result.success(keyPair)
        } catch (e: Exception) {
            Timber.e(e, "Key pair generation failed")
            Result.failure(e)
        }
    }

    private fun generateKeyPairWithSpec(strongBox: Boolean): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).apply {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            setUserAuthenticationRequired(false)
            setInvalidatedByBiometricEnrollment(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && strongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        // Unlock latency hint - prefer operations that don't need user confirmation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_DEVICE_CREDENTIAL)
        }

        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }

    fun getPrivateKey(): PrivateKey? {
        return try {
            keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve private key")
            null
        }
    }

    fun getPublicKey(): PublicKey? {
        return try {
            keyStore.getCertificate(KEY_ALIAS)?.publicKey
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve public key")
            null
        }
    }

    fun getPublicKeyEncoded(): ByteArray? {
        return getPublicKey()?.encoded
    }

    fun sign(data: ByteArray): Result<ByteArray> {
        Timber.d("Signing %d bytes", data.size)
        return try {
            val privateKey = getPrivateKey()
                ?: return Result.failure(IllegalStateException("No signing key available"))

            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(privateKey)
                update(data)
            }

            Result.success(signature.sign())
        } catch (e: Exception) {
            Timber.e(e, "Signing failed")
            Result.failure(e)
        }
    }

    fun signStream(input: InputStream): Result<ByteArray> {
        Timber.d("Signing file stream")
        return try {
            val privateKey = getPrivateKey()
                ?: return Result.failure(IllegalStateException("No signing key available"))

            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(privateKey)
            }

            val buffer = ByteArray(STREAM_BUFFER_SIZE)
            var bytesRead: Int
            var totalBytes = 0L
            try {
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    signature.update(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
            } finally {
                // Clear the buffer to avoid leaving file contents in memory
                buffer.fill(0)
            }

            Timber.d("Signed %d bytes via stream", totalBytes)
            Result.success(signature.sign())
        } catch (e: Exception) {
            Timber.e(e, "Stream signing failed")
            Result.failure(e)
        }
    }

    fun verify(data: ByteArray, signatureBytes: ByteArray): Result<Boolean> {
        return try {
            val publicKey = getPublicKey()
                ?: return Result.failure(IllegalStateException("No verification key available"))

            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initVerify(publicKey)
                update(data)
            }

            Result.success(signature.verify(signatureBytes))
        } catch (e: Exception) {
            Timber.e(e, "Verification failed")
            Result.failure(e)
        }
    }

    fun verifyStream(input: InputStream, signatureBytes: ByteArray): Result<Boolean> {
        return try {
            val publicKey = getPublicKey()
                ?: return Result.failure(IllegalStateException("No verification key available"))

            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initVerify(publicKey)
            }

            val buffer = ByteArray(STREAM_BUFFER_SIZE)
            var bytesRead: Int
            try {
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    signature.update(buffer, 0, bytesRead)
                }
            } finally {
                buffer.fill(0)
            }

            Result.success(signature.verify(signatureBytes))
        } catch (e: Exception) {
            Timber.e(e, "Stream verification failed")
            Result.failure(e)
        }
    }
}
