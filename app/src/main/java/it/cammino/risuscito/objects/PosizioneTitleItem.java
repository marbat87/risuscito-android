package it.cammino.risuscito.objects;

/**
 * Created by marcello.battain on 08/04/2015.
 */
public class PosizioneTitleItem {

    private String titoloPosizione;
    private int idLista;
    private int idPosizione;
    private int tag;
    private boolean multiple;

    public PosizioneTitleItem(String titoloPosizione, int idLista, int idPosizione, int tag, boolean multiple) {
        this.setTitoloPosizione(titoloPosizione);
        this.setIdLista(idLista);
        this.setIdPosizione(idPosizione);
        this.setTag(tag);
        this.setMultiple(multiple);
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

    public int getIdPosizione() {
        return idPosizione;
    }

    public void setIdPosizione(int idPosizione) {
        this.idPosizione = idPosizione;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
}
