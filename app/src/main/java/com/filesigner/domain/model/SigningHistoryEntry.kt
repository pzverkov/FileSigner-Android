package com.filesigner.domain.model

data class SigningHistoryEntry(
    val fileName: String,
    val fileUri: String,
    val signatureUri: String,
    val timestamp: Long,
    val algorithm: String = "SHA256withECDSA"
) {
    val displayTime: String
        get() {
            val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}
