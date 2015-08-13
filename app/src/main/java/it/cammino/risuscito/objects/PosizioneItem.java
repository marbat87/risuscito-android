package it.cammino.risuscito.objects;

/**
 * Created by marcello.battain on 08/04/2015.
 */
public class PosizioneItem {

    private int pagina;
    private String titolo;
    private String colore;
    private int idCanto;
    private String timestamp;
    private String source;
    private boolean mSelected;

    public PosizioneItem(int pagina, String titolo, String colore, int idCanto, String source, String timestamp) {
        this.setTitolo(titolo);
        this.setPagina(pagina);
        this.setColore(colore);
        this.setIdCanto(idCanto);
        this.setSource(source);
        this.setTimestamp(timestamp);
        this.setmSelected(false);
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getColore() {
        return colore;
    }

    public void setColore(String colore) {
        this.colore = colore;
    }

    public int getIdCanto() {
        return idCanto;
    }

    public void setIdCanto(int idCanto) {
        this.idCanto = idCanto;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    public void setmSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
