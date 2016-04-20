package it.cammino.risuscito;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CambioAccordi {

    public static final String[] accordi_it =
            {"Do", "Do#", "Re", "Mib", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "Sib", "Si"};

    public static final String[] accordi_uk =
            {"C", "Cis", "D","Eb", "E", "F", "Fis", "G", "Gis", "A", "B","H"};

    public static final String[] accordi_uk_lower =
            {"c", "cis", "d","eb", "e", "f", "fis", "g", "gis", "a", "b","h"};

    private Activity context;

    public CambioAccordi(Activity context) {
        this.context = context;
    }

    public static String recuperaPrimoAccordo(InputStream canto) {

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
//                    Log.i("RIGA", line);
                    int inizioRiga = line.indexOf("A13F3C") + 8;

                    if (inizioRiga < line.length()) {
//                        Log.i("inizioRiga", inizioRiga + "");
//                        Log.i("carattere", line.charAt(inizioRiga) + "");
                        int i = inizioRiga;
                        while (i < line.length()) {
//                            Log.i("LETTERA", line.charAt(i) + "");
                            if (line.charAt(i) != ' ') {
                                found = true;
                                break;
                            }
                            i++;
                        }
//                        Log.i("inizio Nota", i + "");
//                        Log.i("lunghezza stringa", line.length() + "");
                        primaNota += line.charAt(i);
//                        Log.i("prima lettera", primaNota);
                        for (int j = i+1; j < line.length(); j++) {
//                            Log.i("DA ISP", line.charAt(j) + " ");
                            Matcher myMatcher = Pattern.compile("[a-z]|#]")
                                    .matcher(String.valueOf(line.charAt(j)));
                            if (myMatcher.find()) {
//                                Log.i("matchato", "OK");
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
//            Log.i("risultato", primaNota);
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
//                if (language.equalsIgnoreCase(context.getResources().getString(R.string.barre_search_string))) {
                    if (line.contains(context.getResources().getString(R.string.barre_search_string))) {
//	        		Log.i("RIGA", line);
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
//                }
//                else {
//                    if (line.contains("Barrè") || line.contains("Barr&#232;")) {
////	        		Log.i("RIGA", line);
//                        found = true;
//                        int start = line.indexOf("al") + 3;
//
//                        primoBarre = "";
//                        for (int i = start; i < line.length(); i++) {
//                            if (line.charAt(i) == ' ')
//                                break;
//                            else
//                                primoBarre += line.charAt(i);
//                        }
//                    }
//                }
                line = br.readLine();
            }
            br.close();
//            Log.i("risultato", primoBarre);
            return primoBarre;
        }
        catch (Exception ex) {
            Log.e(getClass().getName(), ex.getLocalizedMessage(), ex);
            return "";
        }
    }

    public HashMap<String, String> diffSemiToni(String primaNota, String notaCambio) {

//		if (primaNota.equals(notaCambio))
//			return null;

        if (primaNota == null || primaNota.equals("")
                || notaCambio == null || notaCambio.equals(""))
            return null;

        String language = context.getResources().getConfiguration().locale.getLanguage();

        String primoAccordo = primaNota;
        String cambioAccordo = notaCambio;

        String[] accordi = accordi_it;
        if (language.equalsIgnoreCase("uk")) {
            accordi = accordi_uk;
            if (primoAccordo.length() == 1)
                primoAccordo = primoAccordo.toUpperCase();
            else
                primoAccordo = primoAccordo.substring(0,1).toUpperCase() + primoAccordo.substring(1);
            if (cambioAccordo.length() == 1)
                cambioAccordo = cambioAccordo.toUpperCase();
            else
                cambioAccordo = cambioAccordo.substring(0,1).toUpperCase() + cambioAccordo.substring(1);
        }

        int start;
        for (start = 0; start < accordi.length; start++) {
            if (primoAccordo.equals(accordi[start]))
                break;
        }
        if (start == accordi.length)
            return null;
//		Log.i("posizionePrimaNota", start + "");
        int end;
        for (end = 0; end < accordi.length; end++) {
            if (cambioAccordo.equals(accordi[end]))
                break;
        }
        if (end == accordi.length)
            return null;
//		Log.i("posizioneNotaCambio", end + "");	
        int differenza;
        if (end > start)
            differenza = (end - start);
        else
            differenza = (end + 12 - start);

        HashMap<String, String> mappa = new HashMap<>();
        for (int i = 0; i < accordi.length; i++) {
//			Log.i("NUOVO", (i+differenza)%12 + "");
//			Log.i("CONVE", accordi[i] + " in " + accordi[(i+differenza)%12]);
            mappa.put(accordi[i], accordi[(i+differenza)%12]);
        }
        return mappa;
    }

    public HashMap<String, String> diffSemiToniMin(String primaNota, String notaCambio) {

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
//		Log.i("posizionePrimaNota", start + "");
        int end;
        for (end = 0; end < accordi_uk_lower.length; end++) {
            if (cambioAccordo.equals(accordi_uk_lower[end]))
                break;
        }
        if (end == accordi_uk_lower.length)
            return null;
//		Log.i("posizioneNotaCambio", end + "");
        int differenza;
        if (end > start)
            differenza = (end - start);
        else
            differenza = (end + 12 - start);

        HashMap<String, String> mappa = new HashMap<>();
        for (int i = 0; i < accordi_uk_lower.length; i++) {
//			Log.i("NUOVO", (i+differenza)%12 + "");
//			Log.i("CONVE", accordi_uk_lower[i] + " in " + accordi_uk_lower[(i + differenza) % 12]);
            mappa.put(accordi_uk_lower[i], accordi_uk_lower[(i+differenza)%12]);
        }
        return mappa;
    }
}
