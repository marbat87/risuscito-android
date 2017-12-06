package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.items.CheckableItem;
import it.cammino.risuscito.items.SimpleItem;

public class ConsegnatiViewModel extends AndroidViewModel {

  public boolean editMode;

  public List<CheckableItem> titoliChoose = new ArrayList<>();

  public List<SimpleItem> titoli = new ArrayList<>();

  private LiveData<List<Canto>> mIndexResult;

  private RisuscitoDatabase mDb;

  public ConsegnatiViewModel(Application application) {
    super(application);
  }

  public LiveData<List<Canto>> getIndexResult() {
    if (mIndexResult == null) mIndexResult = new MutableLiveData<>();
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
