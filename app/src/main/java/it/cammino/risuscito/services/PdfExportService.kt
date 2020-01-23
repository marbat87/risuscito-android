package it.cammino.risuscito.services

import android.annotation.TargetApi
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import it.cammino.risuscito.BuildConfig
import it.cammino.risuscito.CambioAccordi
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ITALIAN
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.ui.LocaleManager.Companion.setSystemLocale
import it.marbat.pdfjet.lib.*
import java.io.*
import java.util.*
import java.util.regex.Pattern

class PdfExportService : IntentService("PdfExportService") {

    internal lateinit var pagina: String
    private lateinit var primaNota: String
    private lateinit var notaCambio: String
    private lateinit var primoBarre: String
    private lateinit var localPDFPath: String
    private lateinit var mLingua: String
    private var pdf: PDF? = null
    private var page: Page? = null
    private var startingY: Float = 0.toFloat()
    private var text: TextLine? = null

    @Throws(Exception::class)
    private fun writeString(line: String) {
        if (startingY + START_Y >= A4.PORTRAIT[1]) {
            page = Page(pdf, A4.PORTRAIT)
            startingY = START_Y
        }
        text?.setLocation(START_X, startingY)
        text?.text = line
        text?.drawOn(page)
        startingY += 20f
    }

    /**
     * This method is invoked on the worker thread with a request to process. Only one Intent is
     * processed at a time, but the processing happens on a worker thread that runs independently from
     * other application logic. So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else. When all requests have been
     * handled, the IntentService stops itself, so you should not call [.stopSelf].
     *
     * @param intent The value passed to [Context.startService].
     */
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent: ")
        exportPdf(intent)
    }

    private fun exportPdf(intent: Intent?) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "exportPdf: DATA_PRIMA_NOTA ${intent?.getStringExtra(DATA_PRIMA_NOTA)}")
            Log.d(TAG, "exportPdf: DATA_NOTA_CAMBIO ${intent?.getStringExtra(DATA_NOTA_CAMBIO)}")
            Log.d(TAG, "exportPdf: PRIMO_BARRE ${intent?.getStringExtra(DATA_PRIMO_BARRE)}")
            Log.d(TAG, "exportPdf: DATA_BARRE_CAMBIO ${intent?.getStringExtra(DATA_BARRE_CAMBIO)}")
            Log.d(TAG, "exportPdf: DATA_PAGINA ${intent?.getStringExtra(DATA_PAGINA)}")
            Log.d(TAG, "exportPdf: DATA_LINGUA ${intent?.getStringExtra(DATA_LINGUA)}")
        }

        primaNota = intent?.getStringExtra(DATA_PRIMA_NOTA) ?: ""
        notaCambio = intent?.getStringExtra(DATA_NOTA_CAMBIO) ?: ""
        primoBarre = intent?.getStringExtra(DATA_PRIMO_BARRE) ?: ""
        val barreCambio = intent?.getStringExtra(DATA_BARRE_CAMBIO) ?: ""
        pagina = intent?.getStringExtra(DATA_PAGINA) ?: ""
        localPDFPath = ""
        mLingua = intent?.getStringExtra(DATA_LINGUA) ?: ""

        val cambioAccordi = CambioAccordi(applicationContext, mLingua)

        val testConv = cambioAccordi.diffSemiToni(primaNota, notaCambio)
        var testConvMin: HashMap<String, String>? = null
        if (mLingua.equals(LANGUAGE_UKRAINIAN, ignoreCase = true))
            testConvMin = cambioAccordi.diffSemiToniMin(primaNota, notaCambio)

        val urlHtml = testConv?.let { cambiaAccordi(it, barreCambio, testConvMin) ?: "" } ?: ""
        try {
            localPDFPath = cacheDir.absolutePath + "/output.pdf"
            Log.d(javaClass.toString(), "localPath:$localPDFPath")
            pdf = PDF(BufferedOutputStream(FileOutputStream(localPDFPath)))
            page = Page(pdf, A4.PORTRAIT)
            val f1 = Font(pdf, resources.assets.open(mFont), Font.STREAM)
            f1.size = 14f
            try {
                var line: String?
                val br = if (urlHtml.isNotEmpty()) BufferedReader(InputStreamReader(FileInputStream(urlHtml), "UTF-8")) else
                    BufferedReader(InputStreamReader(resources.openRawResource(LUtils.getResId(pagina, R.raw::class.java)), "UTF-8"))

                line = br.readLine()
                text = TextLine(f1)
                startingY = START_Y
                while (line != null) {
                    if ((line.contains("000000") || line.contains("A13F3C")) && !line.contains("BGCOLOR")) {
                        if (line.contains("000000")) {
                            text?.color = Color.black
                        }

                        if (line.contains("A13F3C")) {
                            text?.color = Color.red
                        }
                        line = line.replace("<H4>".toRegex(), "")
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

                        if (line == "")
                            writeString("")
                        else {
                            writeString(line)
                        }
                    } else {
                        if (line == "") writeString("")
                    }

                    line = br.readLine()
                }
                br.close()

            } catch (e: IOException) {
                Log.e(javaClass.name, e.localizedMessage, e)
                Log.e(TAG, "Sending broadcast notification: $BROADCAST_EXPORT_ERROR")
                val intentBroadcast = Intent(BROADCAST_EXPORT_ERROR)
                intentBroadcast.putExtra(DATA_EXPORT_ERROR, e.localizedMessage)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                return
            }

            pdf?.close()
        } catch (e: FileNotFoundException) {
            Log.e(javaClass.name, e.localizedMessage, e)
            Log.e(TAG, "Sending broadcast notification: $BROADCAST_EXPORT_ERROR")
            val intentBroadcast = Intent(BROADCAST_EXPORT_ERROR)
            intentBroadcast.putExtra(DATA_EXPORT_ERROR, e.localizedMessage)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
            return
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage, e)
            Log.e(TAG, "Sending broadcast notification: $BROADCAST_EXPORT_ERROR")
            val intentBroadcast = Intent(BROADCAST_EXPORT_ERROR)
            intentBroadcast.putExtra(DATA_EXPORT_ERROR, e.localizedMessage)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
            return
        }

        Log.d(TAG, "Sending broadcast notification: $BROADCAST_EXPORT_COMPLETED")
        val intentBroadcast = Intent(BROADCAST_EXPORT_COMPLETED)
        intentBroadcast.putExtra(DATA_PDF_PATH, localPDFPath)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
    }

    private fun cambiaAccordi(
            conversione: HashMap<String, String>, barre: String?, conversioneMin: HashMap<String, String>?): String? {
        val cantoTrasportato = this.filesDir.toString() + "/temporaneo.htm"

        val conf = resources.configuration
        setSystemLocale(conf, Locale(mLingua))
        val resources = createConfigurationWrapper(conf)

        var barreScritto = false

        try {
            val br = BufferedReader(InputStreamReader(resources.openRawResource(LUtils.getResId(pagina, R.raw::class.java)), "UTF-8"))

            var line: String? = br.readLine()

            val out = BufferedWriter(
                    OutputStreamWriter(FileOutputStream(cantoTrasportato), "UTF-8"))

            val pattern: Pattern
            var patternMinore: Pattern? = null
            when (mLingua) {
                LANGUAGE_ITALIAN -> pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si")
                LANGUAGE_UKRAINIAN -> {
                    pattern = Pattern.compile("Cis|C|D|Eb|E|Fis|F|Gis|G|A|B|H")
                    // inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
                    patternMinore = Pattern.compile("cis|c|d|eb|e|fis|f|gis|g|a| b|h")
                }
                LANGUAGE_ENGLISH -> pattern = Pattern.compile("C|C#|D|Eb|E|F|F#|G|G#|A|Bb|B")
                else -> pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si")
            }

            while (line != null) {
                Log.d(javaClass.name, "RIGA DA ELAB: $line")
                if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
                    if (mLingua.equals(LANGUAGE_UKRAINIAN, ignoreCase = true) || mLingua.equals(LANGUAGE_ENGLISH, ignoreCase = true)) {
                        line = line.replace("</FONT><FONT COLOR=\"#A13F3C\">".toRegex(), "<K>")
                        line = line.replace("</FONT><FONT COLOR=\"#000000\">".toRegex(), "<K2>")
                    }
                    val matcher = pattern.matcher(line)
                    val sb = StringBuffer()
                    val sb2 = StringBuffer()
                    while (matcher.find()) matcher.appendReplacement(sb, conversione[matcher.group(0)
                            ?: ""] ?: "")
                    matcher.appendTail(sb)
                    if (mLingua.equals(LANGUAGE_UKRAINIAN, ignoreCase = true) && patternMinore != null) {
                        val matcherMin = patternMinore.matcher(sb.toString())
                        while (matcherMin.find())
                            matcherMin.appendReplacement(sb2, conversioneMin?.get(matcherMin.group(0)
                                    ?: "") ?: "")
                        matcherMin.appendTail(sb2)
                        line = sb2.toString()
                        line = line.replace("<K>".toRegex(), "</FONT><FONT COLOR='#A13F3C'>")
                        line = line.replace("<K2>".toRegex(), "</FONT><FONT COLOR='#000000'>")
                    } else {
                        line = sb.toString()
                        if (mLingua.equals(LANGUAGE_ENGLISH, ignoreCase = true)) {
                            line = line.replace("<K>".toRegex(), "</FONT><FONT COLOR='#A13F3C'>")
                            line = line.replace("<K2>".toRegex(), "</FONT><FONT COLOR='#000000'>")
                        }
                    }
                    out.write(line)
                    out.newLine()
                } else {
                    if (line.contains("<H3>")) {
                        if (barre != null && barre != "0") {
                            if (!barreScritto) {
                                val oldLine: String = ("<H4><FONT COLOR=\"#A13F3C\"><I>"
                                        + resources.getString(R.string.barre_al_tasto, barre)
                                        + "</I></FONT></H4>")
                                out.write(oldLine)
                                out.newLine()
                                barreScritto = true
                            }
                        }
                        out.write(line)
                        out.newLine()
                    } else {
                        if (!line.contains(resources.getString(R.string.barre_search_string))) {
                            out.write(line)
                            out.newLine()
                        }
                    }
                }
                line = br.readLine()
            }
            br.close()
            out.flush()
            out.close()
            return cantoTrasportato
        } catch (e: Exception) {
            Log.e(javaClass.name, e.localizedMessage, e)
            return null
        }

    }

    companion object {
        internal val TAG = PdfExportService::class.java.canonicalName
        const val BROADCAST_EXPORT_ERROR = "it.cammino.risuscito.services.broadcast.BROADCAST_EXPORT_ERROR"
        const val BROADCAST_EXPORT_COMPLETED = "it.cammino.risuscito.services.broadcast.BROADCAST_EXPORT_COMPLETED"
        const val DATA_PRIMA_NOTA = "it.cammino.risuscito.services.data.DATA_PRIMA_NOTA"
        const val DATA_NOTA_CAMBIO = "it.cammino.risuscito.services.data.DATA_NOTA_CAMBIO"
        const val DATA_BARRE_CAMBIO = "it.cammino.risuscito.services.data.DATA_BARRE_CAMBIO"
        const val DATA_PAGINA = "it.cammino.risuscito.services.data.DATA_PAGINA"
        const val DATA_PRIMO_BARRE = "it.cammino.risuscito.services.data.PRIMO_BARRE"
        const val DATA_EXPORT_ERROR = "it.cammino.risuscito.services.data.DATA_EXPORT_ERROR"
        const val DATA_PDF_PATH = "it.cammino.risuscito.services.data.DATA_PDF_PATH"
        const val DATA_LINGUA = "it.cammino.risuscito.services.data.DATA_LINGUA"
        private const val mFont = "fonts/DroidSansMono.ttf.stream"
        private const val START_X = 25f
        private const val START_Y = 25f
    }

    @Suppress("DEPRECATION")
    private fun createConfContextLegacy(conf: Configuration): Resources {
        val metrics = DisplayMetrics()
        (getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.getMetrics(metrics)
        return Resources(assets, metrics, conf)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun createConfContextO(conf: Configuration): Resources {
        return createConfigurationContext(conf).resources
    }

    private fun createConfigurationWrapper(conf: Configuration): Resources {
        return if (LUtils.hasJB())
            createConfContextO(conf)
        else createConfContextLegacy(conf)
    }

}
