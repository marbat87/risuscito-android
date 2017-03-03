package it.cammino.risuscito.objects;

public class CantoHistory {

    private int pagina;
    private String titolo;
    private String colore;
    private String timestamp;
    private int idCanto;
    private String source;
    private boolean mSelected;

    public CantoHistory(String total, int idCanto, String source, String timestamp) {
        this.setTitolo(total.substring(10));
        this.setPagina(Integer.valueOf(total.substring(0,3)));
        this.setColore(total.substring(3, 10));
        this.setIdCanto(idCanto);
        this.setTimestamp(timestamp);
        this.setSource(source);
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
}
