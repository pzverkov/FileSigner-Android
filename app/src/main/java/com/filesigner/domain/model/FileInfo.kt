package com.filesigner.domain.model

data class FileInfo(
    val uri: String,
    val name: String,
    val size: Long,
    val mimeType: String?
) {
    val displaySize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
}
