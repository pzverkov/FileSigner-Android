package com.filesigner.domain.usecase

import com.filesigner.domain.model.SigningResult
import com.filesigner.domain.repository.FileRepository
import com.filesigner.domain.repository.SigningRepository
import java.util.Base64
import javax.inject.Inject

class SignFileUseCase @Inject constructor(
    private val signingRepository: SigningRepository,
    private val fileRepository: FileRepository
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024 // 500 MB
    }

    suspend operator fun invoke(fileUri: String): SigningResult {
        // Ensure we have a signing key
        if (!signingRepository.hasSigningKey()) {
            val keyResult = signingRepository.generateKeyPairIfNeeded()
            if (keyResult.isFailure) {
                return SigningResult.Error.KeyGenerationError(
                    keyResult.exceptionOrNull()?.message ?: "Failed to generate signing key"
                )
            }
        }

        // Check file size before attempting to sign
        val fileInfo = fileRepository.getFileInfo(fileUri).getOrElse { exception ->
            return when (exception) {
                is java.io.FileNotFoundException -> SigningResult.Error.FileNotFound()
                is SecurityException -> SigningResult.Error.PermissionDenied()
                else -> SigningResult.Error.FileReadError(
                    exception.message ?: "Could not access file"
                )
            }
        }

        if (fileInfo.size > MAX_FILE_SIZE_BYTES) {
            return SigningResult.Error.FileReadError(
                "File is too large (${fileInfo.displaySize}). Maximum supported size is 500 MB."
            )
        }

        // Open file stream and sign
        val inputStream = fileRepository.openFileStream(fileUri).getOrElse { exception ->
            return when (exception) {
                is java.io.FileNotFoundException -> SigningResult.Error.FileNotFound()
                is SecurityException -> SigningResult.Error.PermissionDenied()
                else -> SigningResult.Error.FileReadError(
                    exception.message ?: "Could not read file"
                )
            }
        }

        val signatureBytes = inputStream.use { stream ->
            signingRepository.signStream(stream).getOrElse { exception ->
                return SigningResult.Error.SigningFailed(
                    message = exception.message ?: "Signing operation failed",
                    cause = exception
                )
            }
        }

        // Save the signature
        val signatureUri = fileRepository.saveSignature(fileUri, signatureBytes).getOrElse { exception ->
            return SigningResult.Error.SignatureSaveError(
                exception.message ?: "Could not save signature"
            )
        }

        return SigningResult.Success(
            originalFileUri = fileUri,
            signatureUri = signatureUri,
            signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes)
        )
    }
}
