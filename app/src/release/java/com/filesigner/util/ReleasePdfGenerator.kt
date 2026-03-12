package com.filesigner.util

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleasePdfGenerator @Inject constructor() : SamplePdfGenerator {

    override fun isAvailable(): Boolean = false

    override fun generateSamplePdf(title: String, content: String): Result<Uri> {
        return Result.failure(UnsupportedOperationException("PDF generation is not available in release builds"))
    }
}
