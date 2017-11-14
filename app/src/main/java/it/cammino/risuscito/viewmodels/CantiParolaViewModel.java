package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.Canto;
import it.cammino.risuscito.database.CustomList;
import it.cammino.risuscito.database.Posizione;
import it.cammino.risuscito.database.RisuscitoDatabase;


public class CantiParolaViewModel extends AndroidViewModel {

    private LiveData<List<Posizione>> mCantiParolaResult;

    private RisuscitoDatabase mDb;

    public CantiParolaViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Posizione>> getCantiParolaResult() {
        return mCantiParolaResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mCantiParolaResult = mDb.customListDao().getList(1);
    }
}
