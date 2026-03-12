package com.filesigner.benchmark

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class SigningThroughputTest {

    private lateinit var keyStore: KeyStore
    private lateinit var keyPair: KeyPair

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val THROUGHPUT_KEY_ALIAS = "throughput_signing_key"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val ITERATIONS = 100
        private const val FILE_SIZE = 1024 * 10 // 10 KB
    }

    @Before
    fun setup() {
        keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        if (!keyStore.containsAlias(THROUGHPUT_KEY_ALIAS)) {
            generateKeyPair()
        }

        keyPair = KeyPair(
            keyStore.getCertificate(THROUGHPUT_KEY_ALIAS).publicKey,
            keyStore.getKey(THROUGHPUT_KEY_ALIAS, null) as java.security.PrivateKey
        )
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            THROUGHPUT_KEY_ALIAS,
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
    fun testSigningThroughput() {
        val data = Random.nextBytes(FILE_SIZE)
        val signatures = mutableListOf<ByteArray>()

        val totalTimeMs = measureTimeMillis {
            repeat(ITERATIONS) {
                val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                    initSign(keyPair.private)
                    update(data)
                }
                signatures.add(signature.sign())
            }
        }

        val averageTimeMs = totalTimeMs.toDouble() / ITERATIONS
        val throughputPerSecond = 1000.0 / averageTimeMs

        println("=== Signing Throughput Results ===")
        println("Total iterations: $ITERATIONS")
        println("File size: ${FILE_SIZE / 1024} KB")
        println("Total time: ${totalTimeMs}ms")
        println("Average time per signature: ${String.format("%.2f", averageTimeMs)}ms")
        println("Throughput: ${String.format("%.2f", throughputPerSecond)} signatures/second")
        println("Data throughput: ${String.format("%.2f", throughputPerSecond * FILE_SIZE / 1024)} KB/second")

        // Assert reasonable performance (at least 10 signatures per second)
        assertTrue("Signing throughput should be at least 10/second", throughputPerSecond >= 10)
    }

    @Test
    fun testVerificationThroughput() {
        val data = Random.nextBytes(FILE_SIZE)

        // Create a signature first
        val signatureBytes = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
            initSign(keyPair.private)
            update(data)
        }.sign()

        var successCount = 0
        val totalTimeMs = measureTimeMillis {
            repeat(ITERATIONS) {
                val verifier = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                    initVerify(keyPair.public)
                    update(data)
                }
                if (verifier.verify(signatureBytes)) {
                    successCount++
                }
            }
        }

        val averageTimeMs = totalTimeMs.toDouble() / ITERATIONS
        val throughputPerSecond = 1000.0 / averageTimeMs

        println("=== Verification Throughput Results ===")
        println("Total iterations: $ITERATIONS")
        println("Successful verifications: $successCount")
        println("Total time: ${totalTimeMs}ms")
        println("Average time per verification: ${String.format("%.2f", averageTimeMs)}ms")
        println("Throughput: ${String.format("%.2f", throughputPerSecond)} verifications/second")

        // Assert all verifications succeeded
        assertTrue("All verifications should succeed", successCount == ITERATIONS)
        // Assert reasonable performance
        assertTrue("Verification throughput should be at least 10/second", throughputPerSecond >= 10)
    }

    @Test
    fun testScalingWithFileSize() {
        val sizes = listOf(
            1024,           // 1 KB
            1024 * 10,      // 10 KB
            1024 * 100,     // 100 KB
            1024 * 1024     // 1 MB
        )

        println("=== Scaling with File Size ===")
        println("File Size\tAvg Time (ms)\tThroughput (KB/s)")

        sizes.forEach { size ->
            val data = Random.nextBytes(size)
            val iterations = 20

            val totalTimeMs = measureTimeMillis {
                repeat(iterations) {
                    val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                        initSign(keyPair.private)
                        update(data)
                    }
                    signature.sign()
                }
            }

            val averageTimeMs = totalTimeMs.toDouble() / iterations
            val throughputKBps = (size / 1024.0) / (averageTimeMs / 1000.0)

            val sizeStr = when {
                size >= 1024 * 1024 -> "${size / (1024 * 1024)} MB"
                size >= 1024 -> "${size / 1024} KB"
                else -> "$size B"
            }

            println("$sizeStr\t\t${String.format("%.2f", averageTimeMs)}\t\t${String.format("%.2f", throughputKBps)}")
        }
    }

    @Test
    fun testMemoryUsageDuringSigning() {
        val runtime = Runtime.getRuntime()
        val data = Random.nextBytes(1024 * 1024) // 1 MB

        // Force GC and get baseline
        System.gc()
        Thread.sleep(100)
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()

        // Perform signing operations
        repeat(50) {
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(keyPair.private)
                update(data)
            }
            signature.sign()
        }

        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore

        println("=== Memory Usage Test ===")
        println("Memory before: ${memoryBefore / 1024} KB")
        println("Memory after: ${memoryAfter / 1024} KB")
        println("Memory used: ${memoryUsed / 1024} KB")

        // Memory usage should be reasonable (less than 50MB for this test)
        assertTrue(
            "Memory usage should be less than 50MB",
            memoryUsed < 50 * 1024 * 1024
        )
    }
}
