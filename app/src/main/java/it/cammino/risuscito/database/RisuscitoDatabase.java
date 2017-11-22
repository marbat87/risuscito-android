package it.cammino.risuscito.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.DatabaseCanti;
import it.cammino.risuscito.database.dao.ArgomentiDao;
import it.cammino.risuscito.database.dao.CantoDao;
import it.cammino.risuscito.database.dao.ConsegnatiDao;
import it.cammino.risuscito.database.dao.CronologiaDao;
import it.cammino.risuscito.database.dao.CustomListDao;
import it.cammino.risuscito.database.dao.FavoritesDao;
import it.cammino.risuscito.database.dao.IndiceLiturgicoDao;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.dao.LocalLinksDao;
import it.cammino.risuscito.database.dao.SalmiDao;
import it.cammino.risuscito.database.entities.Argomento;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.database.entities.Consegnato;
import it.cammino.risuscito.database.entities.Cronologia;
import it.cammino.risuscito.database.entities.CustomList;
import it.cammino.risuscito.database.entities.IndiceLiturgico;
import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.database.entities.LocalLink;
import it.cammino.risuscito.database.entities.NomeArgomento;
import it.cammino.risuscito.database.entities.NomeLiturgico;
import it.cammino.risuscito.database.entities.Salmo;

@Database(
  entities = {
    Canto.class,
    ListaPers.class,
    CustomList.class,
    Argomento.class,
    NomeArgomento.class,
    Salmo.class,
    IndiceLiturgico.class,
    NomeLiturgico.class,
    Cronologia.class,
    Consegnato.class,
    LocalLink.class
  },
  version = 1,
  exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class RisuscitoDatabase extends RoomDatabase {

  private static final String TAG = "RisuscitoDatabase";

  private static final String DB_NAME = "RisuscitoDB";

  //    private static final String GIALLO = "#EBD0A5";
  //    private static final String BIANCO = "#FCFCFC";
  //    private static final String AZZURRO = "#6F949A";
  //    private static final String VERDE = "#8FC490";
  //    private static final String GRIGIO = "#CAC8BC";
  // For Singleton instantiation
  private static final Object LOCK = new Object();
  private static final Migration MIGRATION_2_3 =
      new Migration(4, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          Log.d(TAG, "migrate a");
          database.execSQL("DELETE FROM canto");
        }
      };
  /** The only instance */
  private static RisuscitoDatabase sInstance;

  /**
   * Gets the singleton instance of RisuscitoDatabase.
   *
   * @param context The context.
   * @return The singleton instance of RisuscitoDatabase.
   */
  public static synchronized RisuscitoDatabase getInstance(Context context) {
    Log.d(TAG, "getInstance: ");
    if (sInstance == null) {
      synchronized (LOCK) {
        sInstance =
            Room.databaseBuilder(context.getApplicationContext(), RisuscitoDatabase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
        populateAsync(sInstance, context);
      }
    }
    return sInstance;
  }

  /** Inserts the dummy data into the database if it is currently empty. */
  private static void populateInitialData(RisuscitoDatabase mDb, Context context) {
    Log.d(TAG, "populateInitialData: " + mDb.cantoDao().count());
    if (mDb.cantoDao().count() == 0) {
      mDb.beginTransaction();
      try {

        DatabaseCanti listaCanti = new DatabaseCanti(context);
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String[] columns = {
          "_id",
          "pagina",
          "titolo",
          "source",
          "favourite",
          "color",
          "link",
          "zoom",
          "scroll_x",
          "scroll_y",
          "saved_tab",
          "saved_barre",
          "saved_speed"
        };

        Cursor result = db.query("ELENCO", columns, null, null, null, null, null);
        result.moveToFirst();

        List<Canto> elenco = new ArrayList<>();

        while (!result.isAfterLast()) {
          Log.d(TAG, "populateInitialData - ELENCO id:  " + result.getInt(0));
          Canto canto = new Canto();
          canto.id = result.getInt(0);
          canto.pagina = result.getInt(1);
          canto.titolo = result.getString(2);
          canto.source = result.getString(3);
          canto.favorite = result.getInt(4);
          canto.color = result.getString(5);
          canto.link = result.getString(6);
          canto.zoom = result.getInt(7);
          canto.scrollX = result.getInt(8);
          canto.scrollY = result.getInt(9);
          canto.savedTab = result.getString(10);
          canto.savedBarre = result.getString(11);
          canto.savedSpeed = result.getString(12);
          elenco.add(canto);
          result.moveToNext();
        }
        result.close();
        mDb.cantoDao().insertCanto(elenco);

        // ARGOMENTI
        String[] columns2 = {"_id", "id_canto"};

        result = db.query("ARGOMENTI", columns2, null, null, null, null, null);
        result.moveToFirst();

        List<Argomento> argomenti = new ArrayList<>();

        while (!result.isAfterLast()) {
          Log.d(TAG, "populateInitialData - ARGOMENTI id:  " + result.getInt(0));
          Argomento argomento = new Argomento();
          argomento.idArgomento = result.getInt(0);
          argomento.idCanto = result.getInt(1);
          argomenti.add(argomento);
          result.moveToNext();
        }
        result.close();
        mDb.argomentiDao().insertArgomento(argomenti);

        // ARG_NAMES
        String[] columns3 = {"_id", "nome"};

        result = db.query("ARG_NAMES", columns3, null, null, null, null, null);
        result.moveToFirst();

        List<NomeArgomento> nomiArgomenti = new ArrayList<>();

        while (!result.isAfterLast()) {
          Log.d(TAG, "populateInitialData - ARG_NAMES id:  " + result.getInt(0));
          NomeArgomento argomento = new NomeArgomento();
          argomento.idArgomento = result.getInt(0);
          argomento.nomeArgomento = result.getString(1);
          nomiArgomenti.add(argomento);
          result.moveToNext();
        }
        result.close();
        mDb.argomentiDao().insertNomeArgomento(nomiArgomenti);

        // SALMI_MUSICA
        String[] columns4 = {"_id", "num_salmo", "titolo_salmo"};

        result = db.query("SALMI_MUSICA", columns4, null, null, null, null, null);
        result.moveToFirst();

        List<Salmo> salmi = new ArrayList<>();

        while (!result.isAfterLast()) {
          Log.d(TAG, "populateInitialData - SALMI_MUSICA id:  " + result.getInt(0));
          Salmo salmo = new Salmo();
          salmo.id = result.getInt(0);
          salmo.numSalmo = result.getString(1);
          salmo.titoloSalmo = result.getString(2);
          salmi.add(salmo);
          result.moveToNext();
        }
        result.close();
        mDb.salmiDao().insertSalmo(salmi);

        // INDICE_LIT
        String[] columns5 = {"_id", "id_canto"};

        result = db.query("INDICE_LIT", columns5, null, null, null, null, null);
        result.moveToFirst();

        List<IndiceLiturgico> indiceLit = new ArrayList<>();

        while (!result.isAfterLast()) {
          Log.d(TAG, "populateInitialData - INDICE_LIT id:  " + result.getInt(0));
          IndiceLiturgico indice = new IndiceLiturgico();
          indice.idIndice = result.getInt(0);
          indice.idCanto = result.getInt(1);
          indiceLit.add(indice);
          result.moveToNext();
        }
        result.close();
        mDb.indiceLiturgicoDao().insertIndice(indiceLit);

        // INDICE_LIT_NAMES
        String[] columns6 = {"_id", "nome"};

        result = db.query("INDICE_LIT_NAMES", columns6, null, null, null, null, null);
        result.moveToFirst();

        List<NomeLiturgico> nomiLiturgici = new ArrayList<>();

        while (!result.isAfterLast()) {
          Log.d(TAG, "populateInitialData - INDICE_LIT_NAMES id:  " + result.getInt(0));
          NomeLiturgico nomeIndice = new NomeLiturgico();
          nomeIndice.idIndice = result.getInt(0);
          nomeIndice.nome = result.getString(1);
          nomiLiturgici.add(nomeIndice);
          result.moveToNext();
        }
        result.close();
        mDb.indiceLiturgicoDao().insertNomeIndice(nomiLiturgici);

        db.close();
        listaCanti.close();
        mDb.setTransactionSuccessful();
      } finally {
        mDb.endTransaction();
      }
    }
  }

  private static void populateAsync(final RisuscitoDatabase db, Context context) {
    PopulateDbAsync task = new PopulateDbAsync();
    task.execute(db, context);
  }

  public abstract CantoDao cantoDao();

  public abstract FavoritesDao favoritesDao();

  public abstract ListePersDao listePersDao();

  public abstract CustomListDao customListDao();

  public abstract ArgomentiDao argomentiDao();

  public abstract SalmiDao salmiDao();

  public abstract IndiceLiturgicoDao indiceLiturgicoDao();

  public abstract CronologiaDao cronologiaDao();

  public abstract ConsegnatiDao consegnatiDao();

  public abstract LocalLinksDao localLinksDao();

  public String getDbName() {
    return DB_NAME;
  }

  private static class PopulateDbAsync extends AsyncTask<Object, Void, Void> {
    @Override
    protected Void doInBackground(final Object... params) {
      populateInitialData((RisuscitoDatabase) params[0], (Context) params[1]);
      return null;
    }
  }
}
