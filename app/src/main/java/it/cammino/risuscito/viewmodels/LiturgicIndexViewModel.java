package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.mikepenz.fastadapter.IItem;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.database.CantoArgomento;

public class LiturgicIndexViewModel extends GenericIndexViewModel {

  public List<IItem> titoli = new ArrayList<>();
  private LiveData<List<CantoArgomento>> mIndexResult;

  public LiturgicIndexViewModel(Application application) {
    super(application);
  }
  //
  //  public LiveData<List<CantoArgomento>> getIndexResult() {
  //    return mIndexResult;
  //  }
  //
  //  public void createDb() {
  //    mDb = RisuscitoDatabase.getInstance(getApplication());
  //    // Receive changes
  //    subscribeToDbChanges();
  //  }
  //
  //  private void subscribeToDbChanges() {
  //    mIndexResult = mDb.argomentiDao().getLiveAll();
  //  }
}
