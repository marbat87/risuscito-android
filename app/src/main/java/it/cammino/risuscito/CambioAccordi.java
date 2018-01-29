package it.cammino.risuscito;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.cammino.risuscito.ui.ThemeableActivity;

public class CambioAccordi {

    private static final String TAG = "CambioAccordi";

    static final String[] accordi_it =
            {"Do", "Do#", "Re", "Mib", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "Sib", "Si"};

    static final String[] accordi_uk =
            {"C", "Cis", "D","Eb", "E", "F", "Fis", "G", "Gis", "A", "B","H"};

    private static final String[] accordi_uk_lower =
            {"c", "cis", "d","eb", "e", "f", "fis", "g", "gis", "a", "b","h"};

    static final String[] accordi_en =
            {"C", "C#", "D","Eb", "E", "F", "F#", "G", "G#", "A", "Bb","B"};

    static final String[] barre_it = {"I", "II", "III", "IV", "V", "VI", "VII"};
    static final String[] barre_uk = {"I", "II", "III", "IV", "V", "VI", "VII"};
    static final String[] barre_en = {"1", "2", "3", "4", "5", "6", "7"};

    private Context context;
    private String mLanguage;

    CambioAccordi(Context context) {
        this.context = context;
    }

    public CambioAccordi(Context context, String language) {
        this.context = context;
        this.mLanguage = language;
    }

    static String recuperaPrimoAccordo(InputStream canto, String language) {

        if (canto == null)
            return "";

        StringBuilder primaNota = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            canto, "UTF-8"));

            String line = br.readLine();
            boolean found = false;

            while (line != null && !found) {
                if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
                    Log.v(TAG, "recuperaPrimoAccordo - RIGA: " + line);
                    int inizioRiga = line.indexOf("A13F3C") + 8;

                    if (inizioRiga < line.length()) {
                        Log.v(TAG, "recuperaPrimoAccordo - inizioRiga: " + inizioRiga);
                        Log.v(TAG, "recuperaPrimoAccordo - carattere: " + line.charAt(inizioRiga));
                        int i = inizioRiga;
                        while (i < line.length()) {
                            Log.v(TAG, "recuperaPrimoAccordo - LETTERA: " + line.charAt(i));
                            if (line.charAt(i) != ' ') {
                                found = true;
                                break;
                            }
                            i++;
                        }
                        Log.v(TAG, "recuperaPrimoAccordo - inizio Nota: " + i);
                        Log.v(TAG, "recuperaPrimoAccordo - lunghezza stringa: " + line.length());
                        primaNota.append(line.charAt(i));
                        Log.v(TAG, "recuperaPrimoAccordo - prima lettera: " + primaNota);
                        for (int j = i+1; j < line.length(); j++) {
                            Log.v(TAG, "recuperaPrimoAccordo - DA ISP: " + line.charAt(j));
//                            Matcher myMatcher = Pattern.compile("[a-z]|#]")
//                                    .matcher(String.valueOf(line.charAt(j)));
//                            if (language.equalsIgnoreCase("en"))
//                                myMatcher = Pattern.compile("[^m][a-z]|#]")
//                                        .matcher(String.valueOf(line.charAt(j)));
                            Matcher myMatcher;
                            switch (language) {
                                case "en":
                                    myMatcher = Pattern.compile("[^m][a-z]|#]")
                                            .matcher(String.valueOf(line.charAt(j)));
                                    break;
                                default:
                                    myMatcher = Pattern.compile("[a-z]|#]")
                                            .matcher(String.valueOf(line.charAt(j)));
                                    break;
                            }
                            if (myMatcher.find()) {
                                Log.v(TAG, "recuperaPrimoAccordo - matchato OK");
                                primaNota.append(line.charAt(j));
                            }
                            else
                                break;
                        }
                    }

                }
                line = br.readLine();
            }
            br.close();
            Log.v(TAG, "recuperaPrimoAccordo - risultato: " + primaNota);
            return primaNota.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }

    String recuperaBarre(InputStream canto, String language) {

        if (canto == null)
            return "";

//        String language = context.getResources().getConfiguration().locale.getLanguage();

        String primoBarre = "0";

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            canto, "UTF-8"));

            String line = br.readLine();
            boolean found = false;

            while (line != null && !found) {
                int start = line.indexOf(context.getResources().getString(R.string.barre_search_string));
//                if (line.contains(context.getResources().getString(R.string.barre_search_string))) {
                if (start >= 0) {
                    Log.v(TAG, "recuperaBarre - RIGA: " + line);
                    found = true;

//                    int start = line.indexOf(context.getResources().getString(R.string.barre_add_al)) + 3;
                    start = language.equalsIgnoreCase("en")? start + 5 : (line.indexOf(context.getResources().getString(R.string.barre_add_al)) + 3);

                    StringBuilder primoBarreBuilder = new StringBuilder();
                    for (int i = start; i < line.length(); i++) {
                        if (line.charAt(i) == ' ' || line.charAt(i) == '<')
                            break;
                        else
                            primoBarreBuilder.append(line.charAt(i));
                    }
                    primoBarre = primoBarreBuilder.toString();
                }
                line = br.readLine();
            }
            br.close();
            Log.v(TAG, "recuperaBarre - risultato: " + primoBarre);
            return primoBarre;
        }
        catch (Exception ex) {
            Log.e(getClass().getName(), ex.getLocalizedMessage(), ex);
            return "";
        }
    }

    public HashMap<String, String> diffSemiToni(String primaNota, String notaCambio) {

//		if (primaNota.equals(notaCambio))
        Log.v(TAG, "diffSemiToni - primaNota: " + primaNota);
        Log.v(TAG, "diffSemiToni - notaCambio: " + notaCambio);
//			return null;

        if (primaNota == null || primaNota.equals("")
                || notaCambio == null || notaCambio.equals(""))
            return null;

//        String language = context.getResources().getConfiguration().locale.getLanguage();
        String language = ThemeableActivity.getSystemLocalWrapper(context.getResources().getConfiguration()).getLanguage();
        if (mLanguage != null && !mLanguage.isEmpty())
            language = mLanguage;

        Log.v(TAG, "diffSemiToni: language " + language);

        String primoAccordo = primaNota;
        String cambioAccordo = notaCambio;

        String[] accordi;
//        String[] accordi = accordi_it;
//        if (language.equalsIgnoreCase("uk")) {
//            accordi = accordi_uk;
//            if (primoAccordo.length() == 1)
//                primoAccordo = primoAccordo.toUpperCase();
//            else
//                primoAccordo = primoAccordo.substring(0,1).toUpperCase() + primoAccordo.substring(1);
//            if (cambioAccordo.length() == 1)
//                cambioAccordo = cambioAccordo.toUpperCase();
//            else
//                cambioAccordo = cambioAccordo.substring(0,1).toUpperCase() + cambioAccordo.substring(1);
//        }
//
//        if (language.equalsIgnoreCase("en"))
//            accordi = accordi_en;

        switch (language) {
            case "it":
                accordi = accordi_it;
                break;
            case "uk":
                accordi = accordi_uk;
                if (primoAccordo.length() == 1)
                    primoAccordo = primoAccordo.toUpperCase();
                else
                    primoAccordo = primoAccordo.substring(0,1).toUpperCase() + primoAccordo.substring(1);
                if (cambioAccordo.length() == 1)
                    cambioAccordo = cambioAccordo.toUpperCase();
                else
                    cambioAccordo = cambioAccordo.substring(0,1).toUpperCase() + cambioAccordo.substring(1);
                break;
            case "en":
                accordi = accordi_en;
                break;
            default:
                accordi = accordi_it;
                break;
        }

        int start;
        for (start = 0; start < accordi.length; start++) {
            if (primoAccordo.equals(accordi[start]))
                break;
        }
        if (start == accordi.length)
            return null;
        Log.v(TAG, "diffSemiToni - posizionePrimaNota: " + start);
        int end;
        for (end = 0; end < accordi.length; end++) {
            if (cambioAccordo.equals(accordi[end]))
                break;
        }
        if (end == accordi.length)
            return null;
        Log.v(TAG, "diffSemiToni - posizioneNotaCambio: " + end);
        int differenza;
        if (end > start)
            differenza = (end - start);
        else
            differenza = (end + 12 - start);

        HashMap<String, String> mappa = new HashMap<>();
        for (int i = 0; i < accordi.length; i++) {
            Log.v(TAG, "diffSemiToni - NUOVO: " + (i+differenza)%12);
            Log.v(TAG, "diffSemiToni - CONVE: " + accordi[i] + " in " + accordi[(i+differenza)%12]);
            mappa.put(accordi[i], accordi[(i+differenza)%12]);
        }
        return mappa;
    }

    public HashMap<String, String> diffSemiToniMin(String primaNota, String notaCambio) {

        Log.v(TAG, "diffSemiToniMin");

        if (primaNota == null || primaNota.equals("")
                || notaCambio == null || primaNota.equals(""))
            return null;

        String primoAccordo = primaNota;
        if (primoAccordo.length() == 1)
            primoAccordo = primoAccordo.toLowerCase();
        else
            primoAccordo = primoAccordo.substring(0,1).toLowerCase() + primoAccordo.substring(1);
        String cambioAccordo = notaCambio;
        if (cambioAccordo.length() == 1)
            cambioAccordo = cambioAccordo.toLowerCase();
        else
            cambioAccordo = cambioAccordo.substring(0,1).toLowerCase() + cambioAccordo.substring(1);

        int start;
        for (start = 0; start < accordi_uk_lower.length; start++) {
            if (primoAccordo.equals(accordi_uk_lower[start]))
                break;
        }
        if (start == accordi_uk_lower.length)
            return null;
        Log.v(TAG, "diffSemiToniMin - posizionePrimaNota: " + start);
        int end;
        for (end = 0; end < accordi_uk_lower.length; end++) {
            if (cambioAccordo.equals(accordi_uk_lower[end]))
                break;
        }
        if (end == accordi_uk_lower.length)
            return null;
        Log.v(TAG, "diffSemiToniMin - posizioneNotaCambio: " + end);
        int differenza;
        if (end > start)
            differenza = (end - start);
        else
            differenza = (end + 12 - start);

        HashMap<String, String> mappa = new HashMap<>();
        for (int i = 0; i < accordi_uk_lower.length; i++) {
            Log.v(TAG, "diffSemiToniMin - NUOVO: " + (i+differenza)%12);
            Log.v(TAG, "diffSemiToniMin - CONVE: " + accordi_uk_lower[i] + " in " + accordi_uk_lower[(i + differenza) % 12]);
            mappa.put(accordi_uk_lower[i], accordi_uk_lower[(i+differenza)%12]);
        }
        return mappa;
    }
}
