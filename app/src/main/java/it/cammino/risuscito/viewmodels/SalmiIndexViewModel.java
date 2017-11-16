package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.SalmoCanto;
import it.cammino.risuscito.database.entities.Canto;


public class SalmiIndexViewModel extends AndroidViewModel {

    private LiveData<List<SalmoCanto>> mIndexResult;

    private RisuscitoDatabase mDb;

    public SalmiIndexViewModel(Application application) {
        super(application);
    }

    public LiveData<List<SalmoCanto>> getIndexResult() {
        return mIndexResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mIndexResult = mDb.salmiDao().getLiveAll();
    }
}
