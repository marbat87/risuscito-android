package it.cammino.risuscito.objects;

/**
 * Created by marcello.battain on 08/04/2015.
 */
public class CantoInsert {

    private int pagina;
    private String titolo;
    private String colore;
    private int idCanto;
    private String source;

    public CantoInsert(String total, int idCanto, String source) {
        this.setTitolo(total.substring(10));
        this.setPagina(Integer.valueOf(total.substring(0,3)));
        this.setColore(total.substring(3, 10));
        this.setIdCanto(idCanto);
        this.setSource(source);
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
}
