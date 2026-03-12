package com.filesigner.domain.usecase

import com.filesigner.domain.repository.FileRepository
import com.filesigner.domain.repository.SigningRepository
import javax.inject.Inject

class VerifyFileUseCase @Inject constructor(
    private val signingRepository: SigningRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(fileUri: String, signatureUri: String): VerifyResult {
        val fileStream = fileRepository.openFileStream(fileUri).getOrElse { exception ->
            return when (exception) {
                is java.io.FileNotFoundException -> VerifyResult.Error("File not found")
                is SecurityException -> VerifyResult.Error("Permission denied")
                else -> VerifyResult.Error(exception.message ?: "Could not read file")
            }
        }

        val signatureStream = fileRepository.openFileStream(signatureUri).getOrElse { exception ->
            fileStream.close()
            return VerifyResult.Error("Could not read signature file: ${exception.message}")
        }

        val signatureBytes = try {
            signatureStream.use { it.readBytes() }
        } catch (e: Exception) {
            fileStream.close()
            return VerifyResult.Error("Could not read signature: ${e.message}")
        }

        val isValid = fileStream.use { stream ->
            signingRepository.verifyStream(stream, signatureBytes).getOrElse { exception ->
                return VerifyResult.Error("Verification failed: ${exception.message}")
            }
        }

        return if (isValid) VerifyResult.Valid else VerifyResult.Invalid
    }
}

sealed interface VerifyResult {
    data object Valid : VerifyResult
    data object Invalid : VerifyResult
    data class Error(val message: String) : VerifyResult
}
