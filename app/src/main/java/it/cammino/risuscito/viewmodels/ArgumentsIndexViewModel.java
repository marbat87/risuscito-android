package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.Canto;
import it.cammino.risuscito.database.CantoArgomento;
import it.cammino.risuscito.database.RisuscitoDatabase;


public class ArgumentsIndexViewModel extends AndroidViewModel {

    private LiveData<List<CantoArgomento>> mIndexResult;

    private RisuscitoDatabase mDb;

    public ArgumentsIndexViewModel(Application application) {
        super(application);
    }

    public LiveData<List<CantoArgomento>> getIndexResult() {
        return mIndexResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mIndexResult = mDb.argomentiDao().getLiveAll();
    }
}
