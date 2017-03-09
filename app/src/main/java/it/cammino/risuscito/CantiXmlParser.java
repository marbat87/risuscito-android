package it.cammino.risuscito;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class CantiXmlParser {
    private static final String ns = null;

    private String[][] entries;
    private final String CANTI = "canti";
    private final String CANTO = "canto";
    private final String TITOLO = "titolo";
    private final String TESTO = "testo";
    
    // We don't use namespaces

    public String[][] parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, "utf-8");
            parser.nextTag();
            return readCanti(parser);
        } finally {
            in.close();
        }
    }

    private String[][] readCanti(XmlPullParser parser) throws XmlPullParserException, IOException {
        
    	entries = new String[300][2];
    	int i = 0;
    	
        parser.require(XmlPullParser.START_TAG, ns, CANTI);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(CANTO)) {
                readEntry(parser, i++);
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private void readEntry(XmlPullParser parser, int i) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, CANTO);
        String title = null;
        String summary = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
//            if (name.equals(TITOLO)) {
//                title = readTitle(parser);
//            } else if (name.equals(TESTO)) {
//                summary = readSummary(parser);
//            } else {
//                skip(parser);
//            }
            switch (name) {
                case TITOLO:
                    title = readTitle(parser);
                    break;
                case TESTO:
                    summary = readSummary(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        entries[i][0] = title;
//        Log.i("TITOLO[:" + i + "][0]:", title);
        entries[i][1] = summary;
//        Log.i("TESTO[:" + i + "][1]:", summary);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, TITOLO);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, TITOLO);
        return title;
    }

    // Processes summary tags in the feed.
    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, TESTO);
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, TESTO);
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
