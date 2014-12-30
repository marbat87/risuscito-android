package it.cammino.risuscito;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CambioAccordi {

	public static final String[] accordi = 
		{"Do", "Do#", "Re", "Mib", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "Sib", "Si"};
	
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
//	        		Log.i("RIGA", line);	        		
	        		int inizioRiga = line.indexOf("A13F3C") + 8;
	        		
	        		if (inizioRiga < line.length()) {
//		        		Log.i("inizioRiga", inizioRiga + "");
//		        		Log.i("carattere", line.charAt(inizioRiga) + "");
		        		int i = inizioRiga;
		        		while (i < line.length()) {
//		        			Log.i("LETTERA", line.charAt(i) + "");
		        			if (line.charAt(i) != ' ') {
		        				found = true;
		        				break;
		        			}
		        			i++;
		        		}		        		
//		        		Log.i("inizio Nota", i + "");
//		        		Log.i("lunghezza stringa", line.length() + "");
		        		primaNota += line.charAt(i);	
//		        		Log.i("prima lettera", primaNota);
		        		for (int j = i+1; j < line.length(); j++) {       		
//		        			Log.i("DA ISP", line.charAt(j) + " ");
		        			Matcher myMatcher = Pattern.compile("[a-z]|#]")
		        					.matcher(String.valueOf(line.charAt(j)));
		        			if (myMatcher.find()) {
//		        				Log.i("matchato", "OK");
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
//	        Log.i("risultato", primaNota);
	        return primaNota;
		}
        catch (Exception ex)
        {
        	ex.printStackTrace();
        	return "";
        }
	}
	
	public static String recuperaBarre(InputStream canto) {
		
		if (canto == null)
			return "";
		
		String primoBarre = "0";
		
		try {	
	        BufferedReader br = new BufferedReader(
	        		new InputStreamReader(  
	                canto, "UTF-8"));
	        
	        String line = br.readLine();
	        boolean found = false;
	        
	        while (line != null && !found) {
	        	if (line.contains("Barrè") || line.contains("Barr&#232;")) {      		
//	        		Log.i("RIGA", line);
	        		found = true;
	        		int start = line.indexOf("al") + 3;
	        		
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
//  	        Log.i("risultato", primoBarre);
	        return primoBarre;
		}
        catch (Exception ex)
        {
        	ex.printStackTrace();
        	return "";
        }
	}
	
	public static HashMap<String, String> diffSemiToni(String primaNota, String notaCambio) {
		
//		if (primaNota.equals(notaCambio))
//			return null;
		
		if (primaNota == null || primaNota.equals("")
				|| notaCambio == null || primaNota.equals(""))
			return null;
		
		int start;
		for (start = 0; start < accordi.length; start++) {
			if (primaNota.equals(accordi[start]))
				break;
		}
		if (start == accordi.length)
			return null;		
//		Log.i("posizionePrimaNota", start + "");
		int end;
		for (end = 0; end < accordi.length; end++) {
			if (notaCambio.equals(accordi[end]))
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
		
		HashMap<String, String> mappa = new HashMap<String, String>();
		for (int i = 0; i < accordi.length; i++) {
//			Log.i("NUOVO", (i+differenza)%12 + "");
//			Log.i("CONVE", accordi[i] + " in " + accordi[(i+differenza)%12]);
			mappa.put(accordi[i], accordi[(i+differenza)%12]);
		}
		return mappa;
	}
}
