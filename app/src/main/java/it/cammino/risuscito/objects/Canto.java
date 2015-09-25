package it.cammino.risuscito.objects;

/**
 * Created by marcello.battain on 08/04/2015.
 */
public class Canto {

    private int pagina;
    private String titolo;
    private String colore;
    private boolean selected;
    private int idCanto;

    public Canto(String total, int idCanto, boolean selected) {
        this.setTitolo(total.substring(10));
        this.setPagina(Integer.valueOf(total.substring(0,3)));
        this.setColore(total.substring(3, 10));
        this.setIdCanto(idCanto);
        this.setSelected(selected);
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getIdCanto() {
        return idCanto;
    }

    public void setIdCanto(int idCanto) {
        this.idCanto = idCanto;
    }
}
