package com.filesigner.domain.model

sealed class SigningResult {
    data class Success(
        val originalFileUri: String,
        val signatureUri: String,
        val signatureBase64: String,
        val algorithm: String = "SHA256withECDSA",
        val timestamp: Long = System.currentTimeMillis()
    ) : SigningResult()

    sealed class Error : SigningResult() {
        data class FileNotFound(val message: String = "File not found or inaccessible") : Error()
        data class FileReadError(val message: String = "Could not read the file") : Error()
        data class KeyGenerationError(val message: String = "Could not generate signing key") : Error()
        data class SigningFailed(val message: String, val cause: Throwable? = null) : Error()
        data class SignatureSaveError(val message: String = "Could not save signature file") : Error()
        data class PermissionDenied(val message: String = "Storage permission is required") : Error()

        fun toDisplayMessage(): String = when (this) {
            is FileNotFound -> message
            is FileReadError -> message
            is KeyGenerationError -> message
            is SigningFailed -> message
            is SignatureSaveError -> message
            is PermissionDenied -> message
        }
    }
}
