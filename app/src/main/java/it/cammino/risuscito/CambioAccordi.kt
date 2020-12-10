package it.cammino.risuscito

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ITALIAN
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_POLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_TURKISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

class CambioAccordi internal constructor(private val mContext: Context) {

    internal fun recuperaBarre(canto: InputStream?, language: String): String {

        if (canto == null) return ""

        var primoBarre = "0"

        try {
            val br = BufferedReader(InputStreamReader(canto, "UTF-8"))

            var line: String? = br.readLine()
            var found = false

            while (line != null && !found) {
                var start = line.indexOf(mContext.resources.getString(R.string.barre_search_string))
                if (start >= 0) {
                    Log.v(TAG, "recuperaBarre - RIGA: $line")
                    found = true

                    start = when (language) {
                        LANGUAGE_ENGLISH, LANGUAGE_TURKISH -> start + 5
                        LANGUAGE_POLISH -> start + 6
                        else -> line.indexOf(mContext.resources.getString(R.string.barre_add_al)) + 3
                    }

                    val primoBarreBuilder = StringBuilder()
                    for (i in start until line.length) {
                        if (line[i] == ' ' || line[i] == '<')
                            break
                        else
                            primoBarreBuilder.append(line[i])
                    }
                    primoBarre = primoBarreBuilder.toString()
                }
                line = br.readLine()
            }
            br.close()
            Log.v(TAG, "recuperaBarre - risultato: $primoBarre")
            return primoBarre
        } catch (ex: Exception) {
            Log.e(javaClass.name, ex.localizedMessage, ex)
            return ""
        }

    }

    fun diffSemiToni(primaNota: String?, notaCambio: String?): HashMap<String, String>? {

        primaNota ?: return null
        notaCambio ?: return null

        Log.v(TAG, "diffSemiToni - primaNota: $primaNota")
        Log.v(TAG, "diffSemiToni - notaCambio: $notaCambio")

        if (primaNota == "" || notaCambio == "")
            return null

        val language = getSystemLocale(mContext.resources).language
        val locale = getSystemLocale(mContext.resources)

        Log.v(TAG, "diffSemiToni: language $language")

        var primoAccordo: String = primaNota
        var cambioAccordo: String = notaCambio

        val accordi: Array<String> =
                when (language) {
                    LANGUAGE_ITALIAN, LANGUAGE_TURKISH -> accordi_it
                    LANGUAGE_UKRAINIAN -> accordi_uk
                    LANGUAGE_POLISH -> accordi_pl
                    LANGUAGE_ENGLISH -> accordi_en
//            LANGUAGE_TURKISH -> accordi = accordi_tr
                    else -> accordi_it
                }

        if (language == LANGUAGE_UKRAINIAN || language == LANGUAGE_POLISH) {
            primoAccordo = if (primoAccordo.length == 1)
                primoAccordo.toUpperCase(locale)
            else
                primoAccordo.substring(0, 1).toUpperCase(locale) + primoAccordo.substring(1)
            cambioAccordo = if (cambioAccordo.length == 1)
                cambioAccordo.toUpperCase(locale)
            else
                cambioAccordo.substring(0, 1).toUpperCase(locale) + cambioAccordo.substring(1)
        }

        var start = 0
        while (start < accordi.size) {
            if (primoAccordo == accordi[start]) break
            start++
        }
        if (start == accordi.size) return null
        Log.v(TAG, "diffSemiToni - posizionePrimaNota: $start")
        var end = 0
        while (end < accordi.size) {
            if (cambioAccordo == accordi[end]) break
            end++
        }
        if (end == accordi.size) return null
        Log.v(TAG, "diffSemiToni - posizioneNotaCambio: $end")

        val differenza = if (end > start)
            end - start
        else
            end + 12 - start

        val mappa = HashMap<String, String>()
        for (i in accordi.indices) {
            Log.v(TAG, "diffSemiToni - NUOVO: " + (i + differenza) % 12)
            Log.v(TAG, "diffSemiToni - CONVE: " + accordi[i] + " in " + accordi[(i + differenza) % 12])
            mappa[accordi[i]] = accordi[(i + differenza) % 12]
        }
        return mappa
    }

    fun diffSemiToniMin(primaNota: String?, notaCambio: String?): HashMap<String, String>? {

        val language = getSystemLocale(mContext.resources).language
        val locale = getSystemLocale(mContext.resources)

        Log.v(TAG, "diffSemiToniMin - language: $language")

        if (primaNota == null || primaNota == "" || notaCambio == null || primaNota == "")
            return null

        var primoAccordo: String = primaNota
        primoAccordo = if (primoAccordo.length == 1)
            primoAccordo.toLowerCase(locale)
        else
            primoAccordo.substring(0, 1).toLowerCase(locale) + primoAccordo.substring(1)
        var cambioAccordo: String = notaCambio
        cambioAccordo = if (cambioAccordo.length == 1)
            cambioAccordo.toLowerCase(locale)
        else
            cambioAccordo.substring(0, 1).toLowerCase(locale) + cambioAccordo.substring(1)

        val accordiMin: Array<String> =
                when (language) {
                    LANGUAGE_UKRAINIAN -> accordi_uk_lower
                    LANGUAGE_POLISH -> accordi_pl_lower
                    else -> accordi_uk_lower
                }

        var start = 0
        while (start < accordiMin.size) {
            if (primoAccordo == accordiMin[start]) break
            start++
        }
        if (start == accordiMin.size) return null
        Log.v(TAG, "diffSemiToniMin - posizionePrimaNota: $start")
        var end = 0
        while (end < accordiMin.size) {
            if (cambioAccordo == accordiMin[end]) break
            end++
        }
        if (end == accordiMin.size) return null
        Log.v(TAG, "diffSemiToniMin - posizioneNotaCambio: $end")
        val differenza = if (end > start)
            end - start
        else
            end + 12 - start

        val mappa = HashMap<String, String>()
        for (i in accordiMin.indices) {
            Log.v(TAG, "diffSemiToniMin - NUOVO: " + (i + differenza) % 12)
            Log.v(
                    TAG,
                    "diffSemiToniMin - CONVE: "
                            + accordiMin[i]
                            + " in "
                            + accordiMin[(i + differenza) % 12])
            mappa[accordiMin[i]] = accordiMin[(i + differenza) % 12]
        }
        return mappa
    }

    companion object {

        internal val accordi_it = arrayOf("Do", "Do#", "Re", "Mib", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "Sib", "Si")
        internal val accordi_uk = arrayOf("C", "Cis", "D", "Eb", "E", "F", "Fis", "G", "Gis", "A", "B", "H")
        internal val accordi_pl = arrayOf("C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "B", "H")
        internal val accordi_en = arrayOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B")
        internal val barre_it = arrayOf("0", "I", "II", "III", "IV", "V", "VI", "VII")
        internal val barre_en = arrayOf("0", "1", "2", "3", "4", "5", "6", "7")
        private val TAG = CambioAccordi::class.java.canonicalName
        private val accordi_uk_lower = arrayOf("c", "cis", "d", "eb", "e", "f", "fis", "g", "gis", "a", "b", "h")
        private val accordi_pl_lower = arrayOf("c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "b", "h")

        internal fun recuperaPrimoAccordo(canto: InputStream?, language: String): String {

            if (canto == null) return ""

            val primaNota = StringBuilder()

            try {
                val br = BufferedReader(InputStreamReader(canto, "UTF-8"))

                var line: String? = br.readLine()
                var found = false

                while (line != null && !found) {
                    if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
                        Log.v(TAG, "recuperaPrimoAccordo - RIGA: $line")
                        val inizioRiga = line.indexOf("A13F3C") + 8

                        if (inizioRiga < line.length) {
                            Log.v(TAG, """recuperaPrimoAccordo - inizioRiga: $inizioRiga""")
                            Log.v(TAG, "recuperaPrimoAccordo - carattere: " + line[inizioRiga])
                            var i = inizioRiga
                            while (i < line.length) {
                                Log.v(TAG, "recuperaPrimoAccordo - LETTERA: " + line[i])
                                if (line[i] != ' ') {
                                    found = true
                                    break
                                }
                                i++
                            }
                            Log.v(TAG, "recuperaPrimoAccordo - inizio Nota: $i")
                            Log.v(TAG, "recuperaPrimoAccordo - lunghezza stringa: " + line.length)
                            primaNota.append(line[i])
                            Log.v(TAG, "recuperaPrimoAccordo - prima lettera: $primaNota")
                            for (j in i + 1 until line.length) {
                                Log.v(TAG, "recuperaPrimoAccordo - DA ISP: " + line[j])
                                val myMatcher = when (language) {
                                    LANGUAGE_ENGLISH -> Pattern.compile("[^m][a-z]|#]").matcher(line[j].toString())
                                    else -> Pattern.compile("[a-z]|#]").matcher(line[j].toString())
                                }
                                if (myMatcher.find()) {
                                    Log.v(TAG, "recuperaPrimoAccordo - matchato OK")
                                    primaNota.append(line[j])
                                } else
                                    break
                            }
                        }
                    }
                    line = br.readLine()
                }
                br.close()
                Log.v(TAG, "recuperaPrimoAccordo - risultato: $primaNota")
                return primaNota.toString()
            } catch (ex: Exception) {
                Log.e(TAG, "Error:", ex)
                FirebaseCrashlytics.getInstance().recordException(ex)
                return ""
            }

        }
    }
}
