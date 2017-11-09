package it.cammino.risuscito;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("unused")
public class ListaPersonalizzata implements Serializable {

    final static long serialVersionUID = 123456789L;

    private String name;
    private String[] posizioni;
    private String[] canti;
    private int numPosizioni;
    private final int MAX_POSIZIONI = 30;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPosizioni() {
        return posizioni;
    }

    public void setPosizioni(String[] posizioni) {
        this.posizioni = posizioni;
    }

    public String[] getCanti() {
        return canti;
    }

    public void setCanti(String[] canti) {
        this.canti = canti;
    }

    public int getNumPosizioni() {
        return numPosizioni;
    }

    public void setNumPosizioni(int numPosizioni) {
        this.numPosizioni = numPosizioni;
    }

    public ListaPersonalizzata() {
        name = "";
        posizioni = new String[MAX_POSIZIONI];
        canti = new String[MAX_POSIZIONI];
        numPosizioni = 0;

        for (int i = 0; i < MAX_POSIZIONI; i++) {
            posizioni[i] = "";
            canti[i] = "";
        }

    }

    //restituisce il titolo della posizione all'indice "index"
    public String getNomePosizione(int index) {
        if (index < 0 || index >= numPosizioni)
            return "";

        return posizioni[index].trim();
    }

    //restituisce il titolo della canto in posizione "index"
    public String getCantoPosizione(int index) {
        if (index < 0 || index >= numPosizioni)
            return "";

        if (!canti[index].equalsIgnoreCase(""))
            return canti[index].trim();
        else
            return "";
    }

    /*aggiunge una nuova posizione
     ritorna -1 se il nome inserito è nullo o vuoto
     ritorna -2 se la lista è piena
     */
    public int addPosizione(String nomePosizione) {
        if (nomePosizione == null || nomePosizione.trim().equalsIgnoreCase(""))
            return -1;

        if (numPosizioni == MAX_POSIZIONI)
            return -2;

        posizioni[numPosizioni++] = nomePosizione.trim();
        return 0;
    }

    /*aggiunge una nuova canto alla posizione indicata
     ritorna -1 se il nome inserito è nullo o vuoto
     ritorna -2 se l'indice non è valido
     */
    public int addCanto(String titoloCanto, int posizione) {
        if (titoloCanto == null || titoloCanto.trim().equalsIgnoreCase(""))
            return -1;

        if (posizione >= MAX_POSIZIONI || posizione >= numPosizioni)
            return -2;

        canti[posizione] = titoloCanto.trim();
        return 0;
    }

    //rimuove il canto alla posizione indicata
    @SuppressWarnings("UnusedReturnValue")
    int removeCanto(int posizione) {
        if (posizione < 0 || posizione >= MAX_POSIZIONI)
            return -1;

        canti[posizione] = "";
        return 0;
    }

    // rimuove la posizione all'indice "index"
    public int removePosizione(int index) {
        if (index < 0 || index >= MAX_POSIZIONI)
            return -1;

        String[] newPosizioni = new String[20];
        System.arraycopy(posizioni, 0, newPosizioni, 0, index);
        System.arraycopy(posizioni, index + 1, newPosizioni, index, MAX_POSIZIONI - index - 1);
        posizioni = newPosizioni;

        String[] newCanti = new String[20];
        System.arraycopy(canti, 0, newCanti, 0, index);
        System.arraycopy(canti, index + 1, newCanti, index, MAX_POSIZIONI - index - 1);
        canti = newCanti;

        return 0;
    }

    public static byte[] serializeObject(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.close();
            // Get the bytes of the serialized object
//	      byte[] buf = bos.toByteArray();
            return bos.toByteArray();
        }
        catch(IOException ioe) {
            Log.e("serializeObject", "error", ioe);
            return null;
        }
    }

    public static Object deserializeObject(byte[] b) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = in.readObject();
            in.close();
            return object;
        }
        catch(ClassNotFoundException cnfe) {
            Log.e("deserializeObject", "class not found error", cnfe);
            return null;
        }
        catch(IOException ioe) {
            Log.e("deserializeObject", "io error", ioe);
            return null;
        }
    }

}
