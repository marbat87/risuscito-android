package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.ListaPersonalizzata;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.entities.ListaPers;

public class CustomListsViewModel extends AndroidViewModel {

  public int indDaModif = 0;

  public int idDaCanc;

  public int listaDaCanc;

  public String titoloDaCanc;

  public int indexToShow = 0;

  public ListaPersonalizzata celebrazioneDaCanc;

  private LiveData<List<ListaPers>> mCustomListResult;

  private RisuscitoDatabase mDb;

  public CustomListsViewModel(Application application) {
    super(application);
  }

  public LiveData<List<ListaPers>> getCustomListResult() {
    return mCustomListResult;
  }

  public void createDb() {
    mDb = RisuscitoDatabase.Companion.getInstance(getApplication());
    // Receive changes
    subscribeToDbChanges();
  }

  private void subscribeToDbChanges() {
    mCustomListResult = mDb.listePersDao().getLiveAll();
  }
}
