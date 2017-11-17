package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.CantoCronologia;
import it.cammino.risuscito.database.RisuscitoDatabase;


public class CronologiaViewModel extends AndroidViewModel {

    private LiveData<List<CantoCronologia>> mCronologiaResult;

    private RisuscitoDatabase mDb;

    public CronologiaViewModel(Application application) {
        super(application);
    }

    public LiveData<List<CantoCronologia>> getCronologiaResult() {
        return mCronologiaResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mCronologiaResult = mDb.cronologiaDao().getLiveCronologia();
    }
}
