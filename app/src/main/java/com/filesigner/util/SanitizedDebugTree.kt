package com.filesigner.util

import timber.log.Timber

/**
 * A Timber debug tree that masks potentially sensitive information before logging.
 * Masks file URIs, file paths, and file names to prevent PII leakage in logs.
 */
class SanitizedDebugTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, sanitize(message), t)
    }

    companion object {
        private val URI_PATTERN = Regex(
            """(content://[^\s,)]+)"""
        )
        private val FILE_PATH_PATTERN = Regex(
            """(/storage/[^\s,)]+|/data/[^\s,)]+|/sdcard/[^\s,)]+)"""
        )
        private val FILE_NAME_PATTERN = Regex(
            """(name=)([^\s,)]+)"""
        )

        internal fun sanitize(message: String): String {
            var result = message

            // Mask content:// URIs - keep scheme + authority, mask the path
            result = URI_PATTERN.replace(result) { match ->
                val uri = match.value
                val authorityEnd = uri.indexOf('/', "content://".length)
                if (authorityEnd > 0) {
                    uri.substring(0, authorityEnd) + "/***"
                } else {
                    "content://***"
                }
            }

            // Mask file system paths
            result = FILE_PATH_PATTERN.replace(result) { match ->
                val path = match.value
                val lastSlash = path.lastIndexOf('/')
                if (lastSlash > 0) {
                    path.substring(0, path.indexOf('/', 1) + 1) + "***"
                } else {
                    "/***"
                }
            }

            // Mask file names in key=value pairs (e.g., name=secret_doc.pdf)
            result = FILE_NAME_PATTERN.replace(result) { match ->
                val prefix = match.groupValues[1]
                val name = match.groupValues[2]
                val ext = name.substringAfterLast('.', "")
                if (ext.isNotEmpty()) {
                    "${prefix}***.${ext}"
                } else {
                    "${prefix}***"
                }
            }

            return result
        }
    }
}
