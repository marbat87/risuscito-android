package it.cammino.risuscito.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import it.cammino.risuscito.database.Canto;
import it.cammino.risuscito.database.RisuscitoDatabase;


public class FavoritesViewModel extends AndroidViewModel {

    private LiveData<List<Canto>> mFavoritesResult;

    private RisuscitoDatabase mDb;

    public FavoritesViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Canto>> getmFavoritesResult() {
        return mFavoritesResult;
    }

    public void createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication());
        // Receive changes
        subscribeToDbChanges();
    }

    private void subscribeToDbChanges() {
        mFavoritesResult = mDb.favoritesDao().getLiveFavorites();
    }
}
