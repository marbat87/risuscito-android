package it.cammino.risuscito.objects;

/**
 * Created by marcello.battain on 17/05/2015.
 */
public class CantoRecycled {

    private int pagina;
    private String titolo;
    private String colore;
    private String source;
    private int idCanto;
    private int numeroSalmo;
    private boolean mSelected;

    public CantoRecycled(String titolo, int pagina, String colore, int idCanto, String source) {
        this.setTitolo(titolo);
        this.setPagina(pagina);
        this.setColore(colore);
        this.setIdCanto(idCanto);
        this.setSource(source);
        this.setNumeroSalmo(0);
        this.setmSelected(false);
    }

    public CantoRecycled(String titolo, int pagina, String colore, int idCanto, String source, String numeroSalmo) {
        this.setTitolo(titolo);
        this.setPagina(pagina);
        this.setColore(colore);
        this.setIdCanto(idCanto);
        this.setSource(source);
        int numeroTemp = 0;
        try {
            numeroTemp = Integer.valueOf(numeroSalmo.substring(0, 3));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {}
        this.setNumeroSalmo(numeroTemp);
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getIdCanto() {
        return idCanto;
    }

    public void setIdCanto(int idCanto) {
        this.idCanto = idCanto;
    }

    public int getNumeroSalmo() {
        return numeroSalmo;
    }

    public void setNumeroSalmo(int numeroSalmo) {
        this.numeroSalmo = numeroSalmo;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    public void setmSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }
}
