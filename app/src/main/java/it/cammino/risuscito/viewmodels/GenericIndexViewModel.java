package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.items.SimpleItem;

public class GenericIndexViewModel extends AndroidViewModel {

  public int idDaAgg;
  public int idListaDaAgg;
  public int posizioneDaAgg;
  public int idListaClick;
  public int idPosizioneClick;
  public List<SimpleItem> titoli = new ArrayList<>();
  RisuscitoDatabase mDb;

  public GenericIndexViewModel(Application application) {
    super(application);
  }
}
