package com.filesigner.benchmark

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class CryptoPerformanceTest {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var keyStore: KeyStore
    private lateinit var keyPair: KeyPair

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val BENCHMARK_KEY_ALIAS = "benchmark_signing_key"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"

        // Test data sizes
        private const val SMALL_FILE_SIZE = 1024 // 1 KB
        private const val MEDIUM_FILE_SIZE = 1024 * 100 // 100 KB
        private const val LARGE_FILE_SIZE = 1024 * 1024 // 1 MB
        private const val VERY_LARGE_FILE_SIZE = 1024 * 1024 * 10 // 10 MB
    }

    @Before
    fun setup() {
        keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        // Generate a key pair for benchmarking if it doesn't exist
        if (!keyStore.containsAlias(BENCHMARK_KEY_ALIAS)) {
            generateBenchmarkKeyPair()
        }

        keyPair = KeyPair(
            keyStore.getCertificate(BENCHMARK_KEY_ALIAS).publicKey,
            keyStore.getKey(BENCHMARK_KEY_ALIAS, null) as java.security.PrivateKey
        )
    }

    private fun generateBenchmarkKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            BENCHMARK_KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).apply {
            setDigests(KeyProperties.DIGEST_SHA256)
            setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            setUserAuthenticationRequired(false)
        }.build()

        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    @Test
    fun benchmarkKeyPairGeneration() {
        // Clean up any existing benchmark key first
        val tempAlias = "benchmark_temp_key_${System.currentTimeMillis()}"

        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                // Cleanup previous iteration's key if exists
                if (keyStore.containsAlias(tempAlias)) {
                    keyStore.deleteEntry(tempAlias)
                }
            }

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )

            val parameterSpec = KeyGenParameterSpec.Builder(
                tempAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).apply {
                setDigests(KeyProperties.DIGEST_SHA256)
                setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
                setUserAuthenticationRequired(false)
            }.build()

            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()

            runWithTimingDisabled {
                keyStore.deleteEntry(tempAlias)
            }
        }
    }

    @Test
    fun benchmarkSigningSmallFile() {
        val data = generateRandomBytes(SMALL_FILE_SIZE)
        benchmarkSigning(data)
    }

    @Test
    fun benchmarkSigningMediumFile() {
        val data = generateRandomBytes(MEDIUM_FILE_SIZE)
        benchmarkSigning(data)
    }

    @Test
    fun benchmarkSigningLargeFile() {
        val data = generateRandomBytes(LARGE_FILE_SIZE)
        benchmarkSigning(data)
    }

    @Test
    fun benchmarkSigningVeryLargeFile() {
        val data = generateRandomBytes(VERY_LARGE_FILE_SIZE)
        benchmarkSigning(data)
    }

    @Test
    fun benchmarkVerificationSmallFile() {
        val data = generateRandomBytes(SMALL_FILE_SIZE)
        val signature = signData(data)
        benchmarkVerification(data, signature)
    }

    @Test
    fun benchmarkVerificationMediumFile() {
        val data = generateRandomBytes(MEDIUM_FILE_SIZE)
        val signature = signData(data)
        benchmarkVerification(data, signature)
    }

    @Test
    fun benchmarkVerificationLargeFile() {
        val data = generateRandomBytes(LARGE_FILE_SIZE)
        val signature = signData(data)
        benchmarkVerification(data, signature)
    }

    @Test
    fun benchmarkFullSignAndVerifyCycle() {
        val data = generateRandomBytes(MEDIUM_FILE_SIZE)

        benchmarkRule.measureRepeated {
            // Sign
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(keyPair.private)
                update(data)
            }
            val signatureBytes = signature.sign()

            // Verify
            val verifier = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initVerify(keyPair.public)
                update(data)
            }
            verifier.verify(signatureBytes)
        }
    }

    @Test
    fun benchmarkSignatureInitialization() {
        benchmarkRule.measureRepeated {
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initSign(keyPair.private)
        }
    }

    @Test
    fun benchmarkHashingOnly() {
        val data = generateRandomBytes(MEDIUM_FILE_SIZE)

        benchmarkRule.measureRepeated {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            digest.update(data)
            digest.digest()
        }
    }

    private fun benchmarkSigning(data: ByteArray) {
        benchmarkRule.measureRepeated {
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(keyPair.private)
                update(data)
            }
            signature.sign()
        }
    }

    private fun benchmarkVerification(data: ByteArray, signatureBytes: ByteArray) {
        benchmarkRule.measureRepeated {
            val verifier = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initVerify(keyPair.public)
                update(data)
            }
            verifier.verify(signatureBytes)
        }
    }

    private fun signData(data: ByteArray): ByteArray {
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
            initSign(keyPair.private)
            update(data)
        }
        return signature.sign()
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        return Random.nextBytes(size)
    }
}
