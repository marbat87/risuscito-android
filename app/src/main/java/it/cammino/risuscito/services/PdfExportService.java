package it.cammino.risuscito.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.pdfjet.A4;
import com.pdfjet.Color;
import com.pdfjet.Font;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.TextLine;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.cammino.risuscito.BuildConfig;
import it.cammino.risuscito.CambioAccordi;
import it.cammino.risuscito.R;

public class PdfExportService extends IntentService {
  public static final String BROADCAST_EXPORT_ERROR =
      "it.cammino.risuscito.services.broadcast.BROADCAST_EXPORT_ERROR";
  public static final String BROADCAST_EXPORT_COMPLETED =
      "it.cammino.risuscito.services.broadcast.BROADCAST_EXPORT_COMPLETED";
  public static final String DATA_PRIMA_NOTA = "it.cammino.risuscito.services.data.DATA_PRIMA_NOTA";
  public static final String DATA_NOTA_CAMBIO =
      "it.cammino.risuscito.services.data.DATA_NOTA_CAMBIO";
  public static final String DATA_BARRE_CAMBIO =
      "it.cammino.risuscito.services.data.DATA_BARRE_CAMBIO";
  public static final String DATA_PAGINA = "it.cammino.risuscito.services.data.DATA_PAGINA";
  public static final String DATA_PRIMO_BARRE = "it.cammino.risuscito.services.data.PRIMO_BARRE";
  public static final String DATA_EXPORT_ERROR =
      "it.cammino.risuscito.services.data.DATA_EXPORT_ERROR";
  public static final String DATA_PDF_PATH = "it.cammino.risuscito.services.data.DATA_PDF_PATH";
  public static final String DATA_LINGUA = "it.cammino.risuscito.services.data.DATA_LINGUA";
  //    private static final String mFont = "assets/fonts/roboto_mono.ttf";
  private static final String mFont = "fonts/DroidSansMono.ttf.stream";
  private static final float START_X = 25f;
  private static final float START_Y = 25f;
  // The tag we put on debug messages
  final String TAG = getClass().getCanonicalName();
  String pagina;
  String primaNota;
  String notaCambio;
  String primoBarre;
  String localPDFPath;
  String mLingua;
  private PDF pdf;
  private Page page;
  private float startingY;
  private TextLine text;

  public PdfExportService() {
    super("PdfExportService");
  }

  private void writeString(String line) throws Exception {
    if (startingY + START_Y >= A4.PORTRAIT[1]) {
      page = new Page(pdf, A4.PORTRAIT);
      startingY = START_Y;
    }
    text.setLocation(START_X, startingY);
    text.setText(line);
    text.drawOn(page);
    startingY += 20;
  }

  /**
   * This method is invoked on the worker thread with a request to process. Only one Intent is
   * processed at a time, but the processing happens on a worker thread that runs independently from
   * other application logic. So, if this code takes a long time, it will hold up other requests to
   * the same IntentService, but it will not hold up anything else. When all requests have been
   * handled, the IntentService stops itself, so you should not call {@link #stopSelf}.
   *
   * @param intent The value passed to {@link Context#startService(Intent)}.
   */
  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "onHandleIntent: ");
    exportPdf(intent);
  }

  void exportPdf(Intent intent) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "exportPdf: DATA_PRIMA_NOTA " + intent.getStringExtra(DATA_PRIMA_NOTA));
      Log.d(TAG, "exportPdf: DATA_NOTA_CAMBIO " + intent.getStringExtra(DATA_NOTA_CAMBIO));
      Log.d(TAG, "exportPdf: PRIMO_BARRE " + intent.getStringExtra(DATA_PRIMO_BARRE));
      Log.d(TAG, "exportPdf: DATA_BARRE_CAMBIO " + intent.getStringExtra(DATA_BARRE_CAMBIO));
      Log.d(TAG, "exportPdf: DATA_PAGINA " + intent.getStringExtra(DATA_PAGINA));
      Log.d(TAG, "exportPdf: DATA_LINGUA " + intent.getStringExtra(DATA_LINGUA));
    }

    primaNota = intent.getStringExtra(DATA_PRIMA_NOTA);
    notaCambio = intent.getStringExtra(DATA_NOTA_CAMBIO);
    primoBarre = intent.getStringExtra(DATA_PRIMO_BARRE);
    String barreCambio = intent.getStringExtra(DATA_BARRE_CAMBIO);
    pagina = intent.getStringExtra(DATA_PAGINA);
    localPDFPath = "";
    mLingua = intent.getStringExtra(DATA_LINGUA);

    CambioAccordi cambioAccordi = new CambioAccordi(getApplicationContext(), mLingua);

    HashMap<String, String> testConv = cambioAccordi.diffSemiToni(primaNota, notaCambio);
    HashMap<String, String> testConvMin = null;
    if (mLingua.equalsIgnoreCase("uk"))
      testConvMin = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
    String urlHtml = "";
    if (testConv != null) {
      String nuovoFile = cambiaAccordi(testConv, barreCambio, testConvMin);
      if (nuovoFile != null) urlHtml = nuovoFile;
    } else {
      urlHtml = "file:///android_asset/" + pagina + ".htm";
    }
    // step 1
    //    Float margin = 15f;
    //    Document document = new Document(PageSize.A4, margin, margin, margin, margin);
    // step 2
    try {
      //            if (Utility.isExternalStorageWritable()) {
      //                File[] fileArray = ContextCompat.getExternalFilesDirs(this, null);
      //                localPDFPath = fileArray[0].getAbsolutePath() + "/output.pdf";
      //            }
      //            else {
      //                Log.e(TAG, "Sending broadcast notification: " + BROADCAST_EXPORT_ERROR);
      //                Intent intentBroadcast = new Intent(BROADCAST_EXPORT_ERROR);
      //                intentBroadcast.putExtra(DATA_EXPORT_ERROR,
      // getApplicationContext().getString(R.string.no_memory_writable));
      //                sendBroadcast(intentBroadcast);
      //                return;
      //            }
      //            localPDFPath += "/output.pdf";
      localPDFPath = getCacheDir().getAbsolutePath() + "/output.pdf";
      Log.d(getClass().toString(), "localPath:" + localPDFPath);
      //      PdfWriter.getInstance(document, new FileOutputStream(localPDFPath));
      //      FileOutputStream fos = new FileOutputStream(localPDFPath);
      //
      //      PDF pdf = new PDF(fos);
      pdf = new PDF(new BufferedOutputStream(new FileOutputStream(localPDFPath)));
      page = new Page(pdf, A4.PORTRAIT);
      // step 3
      //      document.open();
      //      Font myFontColor =
      //          FontFactory.getFont(
      //              mFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14, Font.NORMAL,
      // BaseColor.BLACK);
      Font f1 = new Font(pdf, getResources().getAssets().open(mFont), Font.STREAM);
      f1.setSize(14);
      // step 4
      try {
        String line;
        BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream(urlHtml), "UTF-8"));

        line = br.readLine();
        text = new TextLine(f1);
        startingY = START_Y;
        while (line != null) {
          //                        Log.i(getClass().toString(), "line:" + line);
          if ((line.contains("000000") || line.contains("A13F3C")) && !line.contains("BGCOLOR")) {
            if (line.contains("000000")) {
              //              myFontColor =
              //                  FontFactory.getFont(
              //                      mFont,
              //                      BaseFont.IDENTITY_H,
              //                      BaseFont.EMBEDDED,
              //                      14,
              //                      Font.NORMAL,
              //                      BaseColor.BLACK);
              text.setColor(Color.black);
            }

            if (line.contains("A13F3C")) {
              //              myFontColor =
              //                  FontFactory.getFont(
              //                      mFont,
              //                      BaseFont.IDENTITY_H,
              //                      BaseFont.EMBEDDED,
              //                      14,
              //                      Font.NORMAL,
              //                      BaseColor.RED);
              text.setColor(Color.red);
            }
            line = line.replaceAll("<H4>", "");
            line = line.replaceAll("</H4>", "");
            line = line.replaceAll("<FONT COLOR=\"#000000\">", "");
            line = line.replaceAll("<FONT COLOR=\"#A13F3C\">", "");
            line = line.replaceAll("<FONT COLOR='#000000'>", "");
            line = line.replaceAll("<FONT COLOR='#A13F3C'>", "");
            line = line.replaceAll("</FONT>", "");
            line = line.replaceAll("<H5>", "");
            line = line.replaceAll("<H3>", "");
            line = line.replaceAll("<H2>", "");
            line = line.replaceAll("</H5>", "");
            line = line.replaceAll("</H3>", "");
            line = line.replaceAll("</H2>", "");
            line = line.replaceAll("<I>", "");
            line = line.replaceAll("</I>", "");
            line = line.replaceAll("<i>", "");
            line = line.replaceAll("</i>", "");
            line = line.replaceAll("<u>", "");
            line = line.replaceAll("</u>", "");
            line = line.replaceAll("<B>", "");
            line = line.replaceAll("</B>", "");
            line = line.replaceAll("<br>", "");

            //            if (line.equals("")) document.add(Chunk.NEWLINE);
            if (line.equals("")) writeString("");
            else {
              //                                Log.i(getClass().toString(), "line filtered:" +
              // line);
              writeString(line);
            }
          } else {
            if (line.equals("")) writeString("");
          }

          line = br.readLine();
        }
        br.close();

      } catch (IOException e) {
        Log.e(getClass().getName(), e.getLocalizedMessage(), e);
        Log.e(TAG, "Sending broadcast notification: " + BROADCAST_EXPORT_ERROR);
        Intent intentBroadcast = new Intent(BROADCAST_EXPORT_ERROR);
        intentBroadcast.putExtra(DATA_EXPORT_ERROR, e.getLocalizedMessage());
        sendBroadcast(intentBroadcast);
        return;
      }
      // step 5
      //      pdf.flush();
      pdf.close();
      //      document.close();

      //    } catch (FileNotFoundException | DocumentException e) {
    } catch (FileNotFoundException e) {
      Log.e(getClass().getName(), e.getLocalizedMessage(), e);
      Log.e(TAG, "Sending broadcast notification: " + BROADCAST_EXPORT_ERROR);
      Intent intentBroadcast = new Intent(BROADCAST_EXPORT_ERROR);
      intentBroadcast.putExtra(DATA_EXPORT_ERROR, e.getLocalizedMessage());
      sendBroadcast(intentBroadcast);
      return;
    } catch (Exception e) {
      Log.e(getClass().getName(), e.getLocalizedMessage(), e);
      Log.e(TAG, "Sending broadcast notification: " + BROADCAST_EXPORT_ERROR);
      Intent intentBroadcast = new Intent(BROADCAST_EXPORT_ERROR);
      intentBroadcast.putExtra(DATA_EXPORT_ERROR, e.getLocalizedMessage());
      sendBroadcast(intentBroadcast);
      return;
    }

    Log.d(TAG, "Sending broadcast notification: " + BROADCAST_EXPORT_COMPLETED);
    Intent intentBroadcast = new Intent(BROADCAST_EXPORT_COMPLETED);
    intentBroadcast.putExtra(DATA_PDF_PATH, localPDFPath);
    sendBroadcast(intentBroadcast);
  }

  @Nullable
  private String cambiaAccordi(
      HashMap<String, String> conversione, String barre, HashMap<String, String> conversioneMin) {
    String cantoTrasportato = this.getFilesDir() + "/temporaneo.htm";

    Configuration conf = getResources().getConfiguration();
    conf.locale = new Locale(mLingua);
    DisplayMetrics metrics = new DisplayMetrics();
    ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
        .getDefaultDisplay()
        .getMetrics(metrics);
    Resources resources = new Resources(getAssets(), metrics, conf);

    boolean barre_scritto = false;

    try {
      BufferedReader br =
          new BufferedReader(new InputStreamReader(getAssets().open(pagina + ".htm"), "UTF-8"));

      String line = br.readLine();

      BufferedWriter out =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(cantoTrasportato), "UTF-8"));

      Pattern pattern;
      Pattern patternMinore = null;
      switch (mLingua) {
        case "it":
          pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
          break;
        case "uk":
          pattern = Pattern.compile("Cis|C|D|Eb|E|Fis|F|Gis|G|A|B|H");
          // inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
          patternMinore = Pattern.compile("cis|c|d|eb|e|fis|f|gis|g|a| b|h");
          break;
        case "en":
          pattern = Pattern.compile("C|C#|D|Eb|E|F|F#|G|G#|A|Bb|B");
          break;
        default:
          pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
          break;
      }

      while (line != null) {
        Log.d(getClass().getName(), "RIGA DA ELAB: " + line);
        if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
          if (mLingua.equalsIgnoreCase("uk") || mLingua.equalsIgnoreCase("en")) {
            line = line.replaceAll("</FONT><FONT COLOR=\"#A13F3C\">", "<K>");
            line = line.replaceAll("</FONT><FONT COLOR=\"#000000\">", "<K2>");
          }
          Matcher matcher = pattern.matcher(line);
          StringBuffer sb = new StringBuffer();
          StringBuffer sb2 = new StringBuffer();
          while (matcher.find()) matcher.appendReplacement(sb, conversione.get(matcher.group(0)));
          matcher.appendTail(sb);
          if (mLingua.equalsIgnoreCase("uk") && patternMinore != null) {
            Matcher matcherMin = patternMinore.matcher(sb.toString());
            while (matcherMin.find())
              matcherMin.appendReplacement(sb2, conversioneMin.get(matcherMin.group(0)));
            matcherMin.appendTail(sb2);
            line = sb2.toString();
            //                        Log.d(getClass().getName(), "RIGA ELAB 1: " + line);
            //                        Log.d(getClass().getName(), "notaHighlighed: " +
            // notaHighlighed);
            //                        Log.d(getClass().getName(), "notaCambio: " + notaCambio);
            //                        Log.d(getClass().getName(), "primaNota: " + primaNota);
            //                        Log.d(getClass().getName(), "RIGA ELAB 2: " + line);
            line = line.replaceAll("<K>", "</FONT><FONT COLOR='#A13F3C'>");
            line = line.replaceAll("<K2>", "</FONT><FONT COLOR='#000000'>");
            //                        Log.d(getClass().getName(), "RIGA ELAB 3: " + line);
          } else {
            line = sb.toString();
            if (mLingua.equalsIgnoreCase("en")) {
              line = line.replaceAll("<K>", "</FONT><FONT COLOR='#A13F3C'>");
              line = line.replaceAll("<K2>", "</FONT><FONT COLOR='#000000'>");
            }
          }
          out.write(line);
          out.newLine();
        } else {
          if (line.contains("<H3>")) {
            if (barre != null && !barre.equals("0")) {
              if (!barre_scritto) {
                String oldLine;
                oldLine =
                    "<H4><FONT COLOR=\"#A13F3C\"><I>"
                        + resources.getString(R.string.barre_al_tasto, barre)
                        + "</I></FONT></H4>";
                out.write(oldLine);
                out.newLine();
                barre_scritto = true;
              }
            }
            out.write(line);
            out.newLine();
          } else {
            if (!line.contains(resources.getString(R.string.barre_search_string))) {
              out.write(line);
              out.newLine();
            }
          }
        }
        line = br.readLine();
      }
      br.close();
      out.flush();
      out.close();
      return cantoTrasportato;
    } catch (Exception e) {
      Log.e(getClass().getName(), e.getLocalizedMessage(), e);
      return null;
    }
  }
}
