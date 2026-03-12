package com.filesigner.data.repository

import android.net.Uri
import com.filesigner.data.source.FileDataSource
import com.filesigner.domain.model.FileInfo
import com.filesigner.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val fileDataSource: FileDataSource
) : FileRepository {

    override suspend fun getFileInfo(uri: String): Result<FileInfo> = withContext(Dispatchers.IO) {
        fileDataSource.getFileInfo(Uri.parse(uri))
    }

    override suspend fun openFileStream(uri: String): Result<InputStream> = withContext(Dispatchers.IO) {
        fileDataSource.openFileStream(Uri.parse(uri))
    }

    override suspend fun saveSignature(originalUri: String, signature: ByteArray): Result<String> =
        withContext(Dispatchers.IO) {
            fileDataSource.saveSignature(Uri.parse(originalUri), signature)
                .map { it.toString() }
        }
}
