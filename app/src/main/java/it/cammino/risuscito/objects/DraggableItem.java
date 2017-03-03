package it.cammino.risuscito.objects;

public class DraggableItem {

    private String titolo;
    private int idPosizione;

    public DraggableItem(String title, int idPosizione) {
        this.setTitolo(title);
        this.setIdPosizione(idPosizione);
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public int getIdPosizione() {
        return idPosizione;
    }

    public void setIdPosizione(int idPosizione) {
        this.idPosizione = idPosizione;
    }
}
