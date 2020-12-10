package it.cammino.risuscito.utils

import android.content.Context
import android.util.Log
import it.marbat.pdfjet.lib.*
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class PdfExporter(context: Context) {

    private var localPDFPath: String = ""
    private var pdf: PDF? = null
    private var page: Page? = null
    private var startingY: Float = 0.toFloat()
    private var text: TextLine? = null

    init {
        localPDFPath = context.cacheDir.absolutePath + "/output.pdf"
        Log.d(javaClass.toString(), "localPath:$localPDFPath")
        pdf = PDF(BufferedOutputStream(FileOutputStream(localPDFPath)))
        page = Page(pdf, A4.PORTRAIT)
        val font = Font(pdf, context.resources.assets.open(mFont), Font.STREAM)
        font.size = 14f
        text = TextLine(font)
        startingY = START_Y
    }

    fun exportPdf(htmlContent: String?): PdfExportOutput {
        val output = PdfExportOutput(pdfPath = localPDFPath)

        if (htmlContent.isNullOrEmpty()) {
            output.isError = true
            output.errorMessage = EMPTY_INPUT
        } else {
            try {
                var line: String
                htmlContent.lines().forEach {
                    if ((it.contains("000000") || it.contains("A13F3C")) && !it.contains("BGCOLOR")) {
                        if (it.contains("000000")) {
                            text?.color = Color.black
                        }

                        if (it.contains("A13F3C")) {
                            text?.color = Color.red
                        }
                        line = it.replace("<H4>".toRegex(), "")
                        line = line.replace("</H4>".toRegex(), "")
                        line = line.replace("<FONT COLOR=\"#000000\">".toRegex(), "")
                        line = line.replace("<FONT COLOR=\"#A13F3C\">".toRegex(), "")
                        line = line.replace("<FONT COLOR='#000000'>".toRegex(), "")
                        line = line.replace("<FONT COLOR='#A13F3C'>".toRegex(), "")
                        line = line.replace("</FONT>".toRegex(), "")
                        line = line.replace("<H5>".toRegex(), "")
                        line = line.replace("<H3>".toRegex(), "")
                        line = line.replace("<H2>".toRegex(), "")
                        line = line.replace("</H5>".toRegex(), "")
                        line = line.replace("</H3>".toRegex(), "")
                        line = line.replace("</H2>".toRegex(), "")
                        line = line.replace("<I>".toRegex(), "")
                        line = line.replace("</I>".toRegex(), "")
                        line = line.replace("<i>".toRegex(), "")
                        line = line.replace("</i>".toRegex(), "")
                        line = line.replace("<u>".toRegex(), "")
                        line = line.replace("</u>".toRegex(), "")
                        line = line.replace("<B>".toRegex(), "")
                        line = line.replace("</B>".toRegex(), "")
                        line = line.replace("<br>".toRegex(), "")
                        line = line.replace("<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">".toRegex(), "")
                        line = line.replace("</SPAN>".toRegex(), "")
                        Log.d(TAG, "line: $line")
                        writeString(line)
                    } else {
                        if (it.isEmpty()) writeString(it)
                    }
                }
                pdf?.close()
            } catch (e: IOException) {
                Log.e(javaClass.name, e.message, e)
                output.isError = true
                output.errorMessage = e.message ?: GENERIC_ERROR
            } catch (e: FileNotFoundException) {
                Log.e(javaClass.name, e.message, e)
                output.isError = true
                output.errorMessage = e.message ?: GENERIC_ERROR
            } catch (e: Exception) {
                Log.e(javaClass.name, e.message, e)
                output.isError = true
                output.errorMessage = e.message ?: GENERIC_ERROR
            }
        }

        return output
    }

    class PdfExportOutput(val pdfPath: String, var isError: Boolean = false, var errorMessage: String = "")

    @Throws(Exception::class)
    fun writeString(line: String) {
        if (startingY + START_Y >= A4.PORTRAIT[1]) {
            page = Page(pdf, A4.PORTRAIT)
            startingY = START_Y
        }
        text?.setLocation(START_X, startingY)
        text?.text = line
        text?.drawOn(page)
        startingY += 20f
    }

    companion object {
        internal val TAG = PdfExporter::class.java.canonicalName
        private const val mFont = "fonts/DroidSansMono.ttf.stream"
        private const val GENERIC_ERROR = "Generic Export Error!"
        private const val EMPTY_INPUT = "Empty input html!"
        private const val START_X = 25f
        private const val START_Y = 25f
    }
}