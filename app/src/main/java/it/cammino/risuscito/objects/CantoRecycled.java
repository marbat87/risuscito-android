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

    public CantoRecycled(String titolo, int pagina, String colore, int idCanto, String source) {
        this.setTitolo(titolo);
        this.setPagina(pagina);
        this.setColore(colore);
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

    public CantoRecycled setNumeroSalmo(String numeroSalmo) {
        int numeroTemp = 0;
        try {
            numeroTemp = Integer.valueOf(numeroSalmo.substring(0, 3));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {}
        this.numeroSalmo = numeroTemp;
        return this;
    }
}
