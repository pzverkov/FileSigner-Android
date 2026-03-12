package com.filesigner

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.filesigner.util.DebugPdfGenerator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfGeneratorTest {

    private lateinit var context: Context
    private lateinit var pdfGenerator: DebugPdfGenerator

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        pdfGenerator = DebugPdfGenerator(context)
    }

    @Test
    fun testGenerateSamplePdf() {
        val result = pdfGenerator.generateSamplePdf(
            title = "Test PDF",
            content = "This is test content for the PDF document."
        )

        assertTrue("PDF generation should succeed", result.isSuccess)
        assertNotNull("PDF URI should not be null", result.getOrNull())
    }

    @Test
    fun testGenerateDefaultPdf() {
        val result = pdfGenerator.generateSamplePdf()

        assertTrue("Default PDF generation should succeed", result.isSuccess)
        assertNotNull("PDF URI should not be null", result.getOrNull())
    }

    @Test
    fun testPdfCanBeRead() {
        val result = pdfGenerator.generateSamplePdf(
            title = "Readable Test",
            content = "Content to verify"
        )

        assertTrue(result.isSuccess)
        val uri = result.getOrNull()!!

        val inputStream = context.contentResolver.openInputStream(uri)
        assertNotNull("Should be able to open generated PDF", inputStream)

        val bytes = inputStream?.readBytes()
        assertNotNull("Should be able to read PDF bytes", bytes)
        assertTrue("PDF should have content", bytes!!.isNotEmpty())

        val header = String(bytes.take(4).toByteArray())
        assertTrue("File should be a valid PDF", header == "%PDF")

        inputStream?.close()
    }
}
