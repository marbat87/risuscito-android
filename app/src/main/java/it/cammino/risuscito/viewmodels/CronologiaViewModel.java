package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.database.CantoCronologia;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.items.SimpleHistoryItem;

public class CronologiaViewModel extends AndroidViewModel {

  public List<SimpleHistoryItem> titoli = new ArrayList<>();
  private LiveData<List<CantoCronologia>> mCanti;
  private RisuscitoDatabase mDb;

  public CronologiaViewModel(Application application) {
    super(application);
  }

  public LiveData<List<CantoCronologia>> getCronologiaCanti() {
    return mCanti;
  }

  public void createDb() {
    mDb = RisuscitoDatabase.getInstance(getApplication());
    // Receive changes
    subscribeToDbChanges();
  }

  private void subscribeToDbChanges() {
    mCanti = mDb.cronologiaDao().getLiveCronologia();
  }
}
