package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.Posizione;
import it.cammino.risuscito.database.RisuscitoDatabase;


public class CantiEucarestiaViewModel extends AndroidViewModel {

    private LiveData<List<Posizione>> mCantiEucarestiaResult;

    private RisuscitoDatabase mDb;

    public CantiEucarestiaViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Posizione>> getCantiEucarestiaResult() {
        return mCantiEucarestiaResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mCantiEucarestiaResult = mDb.customListDao().getList(2);
    }
}
