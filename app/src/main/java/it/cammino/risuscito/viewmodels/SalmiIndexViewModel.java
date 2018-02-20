package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.SalmoCanto;

public class SalmiIndexViewModel extends GenericIndexViewModel {

  private LiveData<List<SalmoCanto>> mIndexResult;


  public SalmiIndexViewModel(Application application) {
    super(application);
  }

  public LiveData<List<SalmoCanto>> getIndexResult() {
    return mIndexResult;
  }

  public void createDb() {
    mDb = RisuscitoDatabase.Companion.getInstance(getApplication());
    // Receive changes
    subscribeToDbChanges();
  }

  private void subscribeToDbChanges() {
    mIndexResult = mDb.salmiDao().getLiveAll();
  }
}
