package it.cammino.risuscito;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class CantoItem {

    private String titolo;
    private String pagina;
    private String colore;

    public CantoItem(String canto) {
        this.setTitolo(canto.substring(10));
        int tempPagina = Integer.valueOf(canto.substring(0,3));
        this.setPagina(String.valueOf(tempPagina));
        this.setColore(canto.substring(3, 10));
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getPagina() {
        return pagina;
    }

    public void setPagina(String pagina) {
        this.pagina = pagina;
    }

    public String getColore() {
        return colore;
    }

    public void setColore(String colore) {
        this.colore = colore;
    }
}
