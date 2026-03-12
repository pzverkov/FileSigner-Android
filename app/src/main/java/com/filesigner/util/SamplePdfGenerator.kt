package com.filesigner.util

import android.net.Uri

interface SamplePdfGenerator {
    fun generateSamplePdf(
        title: String = "Sample Document",
        content: String = ""
    ): Result<Uri>

    fun isAvailable(): Boolean
}
