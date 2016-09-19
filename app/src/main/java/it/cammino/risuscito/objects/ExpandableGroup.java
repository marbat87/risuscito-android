package it.cammino.risuscito.objects;

public class ExpandableGroup {

    private String titolo;
    private int idGruppo;

    public ExpandableGroup(String title, int idGruppo) {
        this.setTitolo(title);
        this.setIdGruppo(idGruppo);
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public int getIdGruppo() {
        return idGruppo;
    }

    public void setIdGruppo(int idGruppo) {
        this.idGruppo = idGruppo;
    }
}
