package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.database.RisuscitoDatabase;


public class CustomListsViewModel extends AndroidViewModel {

    private LiveData<List<ListaPers>> mCustomListResult;

    private RisuscitoDatabase mDb;

    public CustomListsViewModel(Application application) {
        super(application);
    }

    public LiveData<List<ListaPers>> getCustomListResult() {
        return mCustomListResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mCustomListResult = mDb.listePersDao().getLiveAll();
    }
}
