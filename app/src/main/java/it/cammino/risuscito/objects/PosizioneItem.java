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
    private String titoloPosizione;
    private int idLista;
    private int idPosizione;
    private boolean mChoosen;
    private boolean mSelected;
    private int tag;

    public PosizioneItem(String titoloPosizione, int idLista, int idPosizione, int tag) {
        this.setTitolo("");
        this.setPagina(0);
        this.setColore("");
        this.setIdCanto(0);
        this.setSource("");
        this.setTimestamp("");
        this.setmSelected(false);
        this.setmChoosen(false);
        this.setTitoloPosizione(titoloPosizione);
        this.setIdLista(idLista);
        this.setIdPosizione(idPosizione);
        this.setTag(tag);
    }

    public PosizioneItem(String titoloPosizione, int idLista, int idPosizione
                         , int pagina, String titolo, String colore, int idCanto, String source, String timestamp, int tag) {
        this.setTitolo(titolo);
        this.setPagina(pagina);
        this.setColore(colore);
        this.setIdCanto(idCanto);
        this.setSource(source);
        this.setTimestamp(timestamp);
        this.setmSelected(false);
        this.setmChoosen(true);
        this.setTitoloPosizione(titoloPosizione);
        this.setIdLista(idLista);
        this.setIdPosizione(idPosizione);
        this.setTag(tag);
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

    public String getTitoloPosizione() {
        return titoloPosizione;
    }

    public void setTitoloPosizione(String titoloPosizione) {
        this.titoloPosizione = titoloPosizione;
    }

    public int getIdLista() {
        return idLista;
    }

    public void setIdLista(int idLista) {
        this.idLista = idLista;
    }

    public boolean ismChoosen() {
        return mChoosen;
    }

    public void setmChoosen(boolean mChoosen) {
        this.mChoosen = mChoosen;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    public void setmSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

    public int getIdPosizione() {
        return idPosizione;
    }

    public void setIdPosizione(int idPosizione) {
        this.idPosizione = idPosizione;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
