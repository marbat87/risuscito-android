package it.cammino.risuscito.utils

import android.util.Xml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.io.InputStream

internal class CantiXmlParser {

    private var entries = Array(300) { arrayOfNulls<String>(2) }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Array<Array<String?>> {
        inputStream.use { mIn ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(mIn, "utf-8")
            parser.nextTag()
            return readCanti(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCanti(parser: XmlPullParser): Array<Array<String?>> {

        var i = 0

        parser.require(XmlPullParser.START_TAG, ns, CANTI)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            // Starts by looking for the entry tag
            if (name == CANTO) {
                readEntry(parser, i++)
            } else {
                skip(parser)
            }
        }
        return entries
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser, i: Int) {
        parser.require(XmlPullParser.START_TAG, ns, CANTO)
        var title: String? = null
        var summary: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                TITOLO -> title = readTitle(parser)
                TESTO -> summary = readSummary(parser)
                else -> skip(parser)
            }
        }
        entries[i][0] = title
        //        Log.i("TITOLO[:" + i + "][0]:", title);
        entries[i][1] = summary
        //        Log.i("TESTO[:" + i + "][1]:", summary);
    }

    // Processes title tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, TITOLO)
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, TITOLO)
        return title
    }

    // Processes summary tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readSummary(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, TESTO)
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, TESTO)
        return summary
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = StringUtils.EMPTY
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    companion object {
        private val ns: String? = null
        private const val CANTI = "canti"
        private const val CANTO = "canto"
        private const val TITOLO = "titolo"
        private const val TESTO = "testo"
    }
}
