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

    public static final String[] accordi_it =
            {"Do", "Do#", "Re", "Mib", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "Sib", "Si"};

    public static final String[] accordi_uk =
            {"C", "Cis", "D","Eb", "E", "F", "Fis", "G", "Gis", "A", "B","H"};

    public static final String[] accordi_uk_lower =
            {"c", "cis", "d","eb", "e", "f", "fis", "g", "gis", "a", "b","h"};

    public static final String[] accordi_en =
            {"C", "C#", "D","Eb", "E", "F", "F#", "G", "G#", "A", "Bb","B"};

    private Context context;
    private String mLanguage;

    public CambioAccordi(Context context) {
        this.context = context;
    }

    public CambioAccordi(Context context, String language) {
        this.context = context;
        this.mLanguage = language;
    }

    public static String recuperaPrimoAccordo(InputStream canto, String language) {

        if (canto == null)
            return "";

        String primaNota = "";

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            canto, "UTF-8"));

            String line = br.readLine();
            boolean found = false;

            while (line != null && !found) {
                if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
                    Log.d(TAG, "recuperaPrimoAccordo - RIGA: " + line);
                    int inizioRiga = line.indexOf("A13F3C") + 8;

                    if (inizioRiga < line.length()) {
                        Log.d(TAG, "recuperaPrimoAccordo - inizioRiga: " + inizioRiga);
                        Log.d(TAG, "recuperaPrimoAccordo - carattere: " + line.charAt(inizioRiga));
                        int i = inizioRiga;
                        while (i < line.length()) {
                            Log.d(TAG, "recuperaPrimoAccordo - LETTERA: " + line.charAt(i));
                            if (line.charAt(i) != ' ') {
                                found = true;
                                break;
                            }
                            i++;
                        }
                        Log.d(TAG, "recuperaPrimoAccordo - inizio Nota: " + i);
                        Log.d(TAG, "recuperaPrimoAccordo - lunghezza stringa: " + line.length());
                        primaNota += line.charAt(i);
                        Log.d(TAG, "recuperaPrimoAccordo - prima lettera: " + primaNota);
                        for (int j = i+1; j < line.length(); j++) {
                            Log.d(TAG, "recuperaPrimoAccordo - DA ISP: " + line.charAt(j));
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
                                Log.d(TAG, "recuperaPrimoAccordo - matchato OK");
                                primaNota += line.charAt(j);
                            }
                            else
                                break;
                        }
                    }

                }
                line = br.readLine();
            }
            br.close();
            Log.d(TAG, "recuperaPrimoAccordo - risultato: " + primaNota);
            return primaNota;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }

    public String recuperaBarre(InputStream canto) {

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
                if (line.contains(context.getResources().getString(R.string.barre_search_string))) {
                    Log.d(TAG, "recuperaBarre - RIGA: " + line);
                    found = true;
                    int start = line.indexOf(context.getResources().getString(R.string.barre_add_al)) + 3;

                    primoBarre = "";
                    for (int i = start; i < line.length(); i++) {
                        if (line.charAt(i) == ' ')
                            break;
                        else
                            primoBarre += line.charAt(i);
                    }
                }
                line = br.readLine();
            }
            br.close();
            Log.d(TAG, "recuperaBarre - risultato: " + primoBarre);
            return primoBarre;
        }
        catch (Exception ex) {
            Log.e(getClass().getName(), ex.getLocalizedMessage(), ex);
            return "";
        }
    }

    public HashMap<String, String> diffSemiToni(String primaNota, String notaCambio) {

//		if (primaNota.equals(notaCambio))
        Log.d(TAG, "diffSemiToni - primaNota: " + primaNota);
        Log.d(TAG, "diffSemiToni - notaCambio: " + notaCambio);
//			return null;

        if (primaNota == null || primaNota.equals("")
                || notaCambio == null || notaCambio.equals(""))
            return null;

//        String language = context.getResources().getConfiguration().locale.getLanguage();
        String language = ThemeableActivity.getSystemLocalWrapper(context.getResources().getConfiguration()).getLanguage();
        if (mLanguage != null && !mLanguage.isEmpty())
            language = mLanguage;

        Log.d(TAG, "diffSemiToni: language " + language);

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
        Log.d(TAG, "diffSemiToni - posizionePrimaNota: " + start);
        int end;
        for (end = 0; end < accordi.length; end++) {
            if (cambioAccordo.equals(accordi[end]))
                break;
        }
        if (end == accordi.length)
            return null;
        Log.d(TAG, "diffSemiToni - posizioneNotaCambio: " + end);
        int differenza;
        if (end > start)
            differenza = (end - start);
        else
            differenza = (end + 12 - start);

        HashMap<String, String> mappa = new HashMap<>();
        for (int i = 0; i < accordi.length; i++) {
            Log.d(TAG, "diffSemiToni - NUOVO: " + (i+differenza)%12);
            Log.d(TAG, "diffSemiToni - CONVE: " + accordi[i] + " in " + accordi[(i+differenza)%12]);
            mappa.put(accordi[i], accordi[(i+differenza)%12]);
        }
        return mappa;
    }

    public HashMap<String, String> diffSemiToniMin(String primaNota, String notaCambio) {

        Log.d(TAG, "diffSemiToniMin");

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
        Log.d(TAG, "diffSemiToniMin - posizionePrimaNota: " + start);
        int end;
        for (end = 0; end < accordi_uk_lower.length; end++) {
            if (cambioAccordo.equals(accordi_uk_lower[end]))
                break;
        }
        if (end == accordi_uk_lower.length)
            return null;
        Log.d(TAG, "diffSemiToniMin - posizioneNotaCambio: " + end);
        int differenza;
        if (end > start)
            differenza = (end - start);
        else
            differenza = (end + 12 - start);

        HashMap<String, String> mappa = new HashMap<>();
        for (int i = 0; i < accordi_uk_lower.length; i++) {
            Log.d(TAG, "diffSemiToniMin - NUOVO: " + (i+differenza)%12);
            Log.d(TAG, "diffSemiToniMin - CONVE: " + accordi_uk_lower[i] + " in " + accordi_uk_lower[(i + differenza) % 12]);
            mappa.put(accordi_uk_lower[i], accordi_uk_lower[(i+differenza)%12]);
        }
        return mappa;
    }
}
