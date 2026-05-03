package com.filesigner.data.source

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import com.filesigner.domain.model.FileInfo
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDataSource @Inject constructor(
    private val contentResolver: ContentResolver
) {
    fun getFileInfo(uri: Uri): Result<FileInfo> {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    val name = if (nameIndex >= 0) {
                        cursor.getString(nameIndex) ?: "unknown"
                    } else {
                        uri.lastPathSegment ?: "unknown"
                    }

                    val size = if (sizeIndex >= 0) {
                        cursor.getLong(sizeIndex)
                    } else {
                        0L
                    }

                    val mimeType = contentResolver.getType(uri)

                    Timber.d("File info resolved: size=%d, type=%s", size, mimeType)
                    Result.success(
                        FileInfo(
                            uri = uri.toString(),
                            name = name,
                            size = size,
                            mimeType = mimeType
                        )
                    )
                } else {
                    Timber.w("Cursor empty for file query")
                    Result.failure(java.io.FileNotFoundException("Could not query file info"))
                }
            } ?: Result.failure(java.io.FileNotFoundException("Could not access file"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to get file info")
            Result.failure(e)
        }
    }

    fun openFileStream(uri: Uri): Result<InputStream> {
        return try {
            val stream = contentResolver.openInputStream(uri)
                ?: return Result.failure(java.io.FileNotFoundException("Could not open file"))
            Result.success(stream)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open file stream")
            Result.failure(e)
        }
    }

    fun saveSignature(originalUri: Uri, signature: ByteArray): Result<Uri> {
        return try {
            val originalName = getFileInfo(originalUri).getOrNull()?.name ?: "file"
            val signatureFileName = "${originalName}.sig"

            Timber.d("Saving signature (%d bytes)", signature.size)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveSignatureScoped(signatureFileName, signature)
            } else {
                saveSignatureLegacy(signatureFileName, signature)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save signature")
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveSignatureScoped(fileName: String, signature: ByteArray): Result<Uri> {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return Result.failure(Exception("Could not create signature file"))

        return try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(signature)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)

            Timber.i("Signature saved successfully")
            Result.success(uri)
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun saveSignatureLegacy(fileName: String, signature: ByteArray): Result<Uri> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val signatureFile = File(downloadsDir, fileName)

        return try {
            FileOutputStream(signatureFile).use { fos ->
                fos.write(signature)
            }
            Timber.i("Signature saved successfully (legacy)")
            Result.success(Uri.fromFile(signatureFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
