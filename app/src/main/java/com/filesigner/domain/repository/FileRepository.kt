package com.filesigner.domain.repository

import com.filesigner.domain.model.FileInfo
import java.io.InputStream

interface FileRepository {
    suspend fun getFileInfo(uri: String): Result<FileInfo>
    suspend fun openFileStream(uri: String): Result<InputStream>
    suspend fun saveSignature(originalUri: String, signature: ByteArray): Result<String>
}
