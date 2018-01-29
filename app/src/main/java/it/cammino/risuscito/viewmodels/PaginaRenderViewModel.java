package it.cammino.risuscito.viewmodels;

import android.arch.lifecycle.ViewModel;

import it.cammino.risuscito.database.entities.Canto;


public class PaginaRenderViewModel extends ViewModel {

    public String notaCambio;
    public String speedValue;
    public boolean scrollPlaying;
    public String mostraAudio;
    public String barreCambio;

    public Canto mCurrentCanto;

}
