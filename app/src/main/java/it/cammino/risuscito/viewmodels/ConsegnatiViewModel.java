package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.entities.Canto;

public class ConsegnatiViewModel extends AndroidViewModel {

  private LiveData<List<Canto>> mIndexResult;

  private RisuscitoDatabase mDb;

  public ConsegnatiViewModel(Application application) {
    super(application);
  }

  public LiveData<List<Canto>> getIndexResult() {
    return mIndexResult;
  }

  public void createDb() {
    mDb = RisuscitoDatabase.getInstance(getApplication());
    // Receive changes
    subscribeToDbChanges();
  }

  private void subscribeToDbChanges() {
    mIndexResult = mDb.consegnatiDao().getLiveConsegnati();
  }
}
