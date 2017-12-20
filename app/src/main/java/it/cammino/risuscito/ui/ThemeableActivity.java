package it.cammino.risuscito.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Tasks;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import it.cammino.risuscito.DatabaseCanti;
import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.utils.ThemeUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class ThemeableActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  final String TAG = getClass().getCanonicalName();
  protected boolean hasNavDrawer = false;
  private ThemeUtils mThemeUtils;

  public static boolean isMenuWorkaroundRequired() {
    return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT
        &&
        //                android.os.Build.VERSION.SDK_INT >
        // android.os.Build.VERSION_CODES.GINGERBREAD_MR1 &&
        ("LGE".equalsIgnoreCase(Build.MANUFACTURER) || "E6710".equalsIgnoreCase(Build.DEVICE));
  }

  @SuppressWarnings("deprecation")
  private static Locale getSystemLocaleLegacy(Configuration config) {
    return config.locale;
  }

  @TargetApi(Build.VERSION_CODES.N)
  private static Locale getSystemLocale(Configuration config) {
    return config.getLocales().get(0);
  }

  public static Locale getSystemLocalWrapper(Configuration config) {
    if (LUtils.hasN()) return ThemeableActivity.getSystemLocale(config);
    else return ThemeableActivity.getSystemLocaleLegacy(config);
  }

  @SuppressWarnings("deprecation")
  private static void setSystemLocaleLegacy(Configuration config, Locale locale) {
    config.locale = locale;
  }

  @TargetApi(Build.VERSION_CODES.N)
  private static void setSystemLocale(Configuration config, Locale locale) {
    config.setLocale(locale);
  }

  public static void setSystemLocalWrapper(Configuration config, Locale locale) {
    if (LUtils.hasN()) ThemeableActivity.setSystemLocale(config, locale);
    else ThemeableActivity.setSystemLocaleLegacy(config, locale);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    Log.d(TAG, "onSharedPreferenceChanged: " + s);
    if (s.equalsIgnoreCase("primary_color")) Log.d(TAG, "onSharedPreferenceChanged: primary_color" + sharedPreferences.getInt(s, 0));
    if (s.equals(Utility.SYSTEM_LANGUAGE)) {
      //            Log.d(TAG, "onSharedPreferenceChanged: cur lang" +
      // getResources().getConfiguration().locale.getLanguage());
      Log.d(
          TAG,
          "onSharedPreferenceChanged: cur lang "
              + ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
                  .getLanguage());
      Log.d(TAG, "onSharedPreferenceChanged: cur set " + sharedPreferences.getString(s, ""));
      if (!ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
          .getLanguage()
          .equalsIgnoreCase(sharedPreferences.getString(s, "it"))) {
        Intent i =
            getBaseContext()
                .getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (i != null) {
          i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          i.putExtra(Utility.DB_RESET, true);
          String currentLang =
              ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
                  .getLanguage();
          i.putExtra(
              Utility.CHANGE_LANGUAGE, currentLang + "-" + sharedPreferences.getString(s, ""));
        }
        startActivity(i);
      }
    }
    if (s.equals(Utility.SCREEN_ON)) ThemeableActivity.this.checkScreenAwake();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    if (isMenuWorkaroundRequired()) {
      forceOverflowMenu();
    }
    mThemeUtils = new ThemeUtils(this);
    LUtils mLUtils = LUtils.getInstance(this);
    mLUtils.convertIntPreferences();
    setTheme(mThemeUtils.getCurrent());
    AppCompatDelegate.setDefaultNightMode(
        ThemeUtils.isDarkMode(this)
            ? AppCompatDelegate.MODE_NIGHT_YES
            : AppCompatDelegate.MODE_NIGHT_NO);

    // setta il colore della barra di stato, solo su KITKAT
    Utility.setupTransparentTints(
        ThemeableActivity.this, mThemeUtils.primaryColorDark(), hasNavDrawer);

    if (LUtils.hasL()) {
      // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
      // list gets weird. We need to change either the icon or the color
      // of the TaskDescription.
      ActivityManager.TaskDescription taskDesc =
          new ActivityManager.TaskDescription(null, null, mThemeUtils.primaryColor());
      setTaskDescription(taskDesc);
    }

    // Iconic
    LayoutInflaterCompat.setFactory2(
        getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkScreenAwake();

    PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this)
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this)
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired()) {
      openOptionsMenu();
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired())
        || super.onKeyDown(keyCode, event);
  }

  // controlla se l'app deve mantenere lo schermo acceso
  public void checkScreenAwake() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    boolean screenOn = pref.getBoolean(Utility.SCREEN_ON, false);
    if (screenOn) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  private void forceOverflowMenu() {
    try {
      ViewConfiguration config = ViewConfiguration.get(this);
      Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
      if (menuKeyField != null) {
        menuKeyField.setAccessible(true);
        menuKeyField.setBoolean(config, false);
      }
    } catch (IllegalAccessException e) {
      Log.w(getClass().toString(), "IllegalAccessException - Failed to force overflow menu.", e);
    } catch (NoSuchFieldException e) {
      Log.w(getClass().toString(), "NoSuchFieldException - Failed to force overflow menu.", e);
    }
  }

  public ThemeUtils getThemeUtils() {
    return mThemeUtils;
  }

  @Override
  protected void attachBaseContext(Context newBase) {

    Configuration config = new Configuration();
    //        boolean changeConfig = false;

    // lingua
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(newBase);
    String language = sp.getString(Utility.SYSTEM_LANGUAGE, "");
    Log.d(TAG, "attachBaseContext - language: " + language);
    // ho settato almeno una volta la lingua --> imposto quella
    if (!language.equals("")) {
      Locale locale = new Locale(language);
      Locale.setDefault(locale);
      ThemeableActivity.setSystemLocalWrapper(config, locale);
      //            changeConfig = true;
    }
    // non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
    // selezionabile oppure IT se non presente
    else {
      SharedPreferences.Editor mEditor = sp.edit();
      String mLanguage;
      switch (getSystemLocalWrapper(newBase.getResources().getConfiguration()).getLanguage()) {
        case "uk":
          mLanguage = "uk";
          break;
        case "en":
          mLanguage = "en";
          break;
        default:
          mLanguage = "it";
          break;
      }
      mEditor.putString(Utility.SYSTEM_LANGUAGE, mLanguage);
      mEditor.apply();
      Locale locale = new Locale(mLanguage);
      Locale.setDefault(locale);
      ThemeableActivity.setSystemLocalWrapper(config, locale);
      //            changeConfig = true;
    }

    // fond dimension
    try {
      float actualScale = newBase.getResources().getConfiguration().fontScale;
      Log.d(TAG, "actualScale: " + actualScale);
      float systemScale =
          Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE);
      Log.d(TAG, "systemScale: " + systemScale);
      if (actualScale != systemScale) {
        config.fontScale = systemScale;
        //                changeConfig = true;
      }
    } catch (Settings.SettingNotFoundException e) {
      Log.e(
          TAG,
          "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: "
              + e.getLocalizedMessage());
    } catch (NullPointerException e) {
      Log.e(
          TAG,
          "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA: "
              + e.getLocalizedMessage());
    }

    //        if (changeConfig) {
    if (LUtils.hasJB()) {
      newBase = newBase.createConfigurationContext(config);
    } else {
      //noinspection deprecation
      newBase
          .getResources()
          .updateConfiguration(config, newBase.getResources().getDisplayMetrics());
    }
    //        }

    // Calligraphy
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

  protected boolean saveSharedPreferencesToFile(OutputStream out) {
    boolean res = false;
    ObjectOutputStream output = null;
    try {
      output = new ObjectOutputStream(out);
      SharedPreferences pref =
          PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this);
      output.writeObject(pref.getAll());

    } catch (IOException e) {
      String error = "saveSharedPreferencesToFile - IOException: " + e.getLocalizedMessage();
      Log.e(getClass().getName(), error, e);
      Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
    } finally {
      try {
        if (output != null) {
          output.flush();
          output.close();
          res = true;
        }
      } catch (IOException e) {
        String error = "saveSharedPreferencesToFile - IOException: " + e.getLocalizedMessage();
        Log.e(getClass().getName(), error, e);
        Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
      }
    }
    return res;
  }

  @SuppressWarnings({"unchecked"})
  protected void loadSharedPreferencesFromFile(InputStream in) {
    //        boolean res = false;
    ObjectInputStream input = null;
    try {
      input = new ObjectInputStream(in);
      SharedPreferences.Editor prefEdit =
          PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this).edit();
      prefEdit.clear();
      Map<String, ?> entries = (Map<String, ?>) input.readObject();
      for (Map.Entry<String, ?> entry : entries.entrySet()) {
        Object v = entry.getValue();
        String key = entry.getKey();

        if (v instanceof Boolean) prefEdit.putBoolean(key, (Boolean) v);
        else if (v instanceof Float) prefEdit.putFloat(key, (Float) v);
        else if (v instanceof Integer) prefEdit.putInt(key, (Integer) v);
        else if (v instanceof Long) prefEdit.putLong(key, (Long) v);
        else if (v instanceof String) prefEdit.putString(key, ((String) v));
      }
      prefEdit.apply();
    } catch (ClassNotFoundException e) {
      String error =
          "loadSharedPreferencesFromFile - ClassNotFoundException: " + e.getLocalizedMessage();
      Log.e(getClass().getName(), error, e);
      Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
    } catch (IOException e) {
      String error = "loadSharedPreferencesFromFile - IOException: " + e.getLocalizedMessage();
      Log.e(getClass().getName(), error, e);
      Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
        String error = "loadSharedPreferencesFromFile - IOException: " + e.getLocalizedMessage();
        Log.e(getClass().getName(), error, e);
        Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * **************************************************************** controlla se il file è già
   * esistente; se esiste lo cancella e poi lo ricrea
   *
   * @param titl file name
   * @param mime file mime type (application/x-sqlite3)
   */
  public void checkDuplTosave(String titl, String mime, boolean dataBase)
      throws IOException, ExecutionException, InterruptedException, NoPermissioneException {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    // Synchronously check for necessary permissions
    checkDrivePermissions(account);

    final DriveResourceClient client = Drive.getDriveResourceClient(this, account);
    // task di recupero la cartella applicativa
    DriveFolder folder = Tasks.await(client.getAppFolder());

    File file = getNewDbPath();
    if (folder != null && titl != null && (!dataBase || file != null)) {
      // create content from file
      Log.d(getClass().getName(), "saveCheckDupl - dataBase? " + dataBase);
      Log.d(getClass().getName(), "saveCheckDupl - title: " + titl);
      Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, titl)).build();

      // task di recupero metadata del file se già presente
      MetadataBuffer metadataBuffer = Tasks.await(client.query(query));

      int count = metadataBuffer.getCount();
      Log.d(getClass().getName(), "saveCheckDupl - Count files old: " + count);
      if (count > 0) {
        DriveId mDriveId = metadataBuffer.get(count - 1).getDriveId();
        Log.d(getClass().getName(), "saveCheckDupl - driveIdRetrieved: " + mDriveId);
        Log.d(
            getClass().getName(),
            "saveCheckDupl - filesize in cloud " + metadataBuffer.get(0).getFileSize());
        metadataBuffer.release();

        DriveFile mFile = mDriveId.asDriveFile();
        // task di cancellazione file eventualmente già presente
        Tasks.await(client.delete(mFile));
        Log.d(getClass().getName(), "saveCheckDupl - deleted");

        saveToDrive(folder, titl, mime, file, dataBase);
      } else {
        metadataBuffer.release();
        saveToDrive(folder, titl, mime, file, dataBase);
      }
    }
  }

  /**
   * **************************************************************** create file in GOODrive
   *
   * @param titl file name
   * @param mime file mime type (application/x-sqlite3)
   * @param file file (with content) to create
   */
  public void saveToDrive(
      DriveFolder driveFolder,
      final String titl,
      final String mime,
      final File file,
      final boolean dataBase)
      throws ExecutionException, InterruptedException, IOException, NoPermissioneException {
    Log.d(getClass().getName(), "saveToDrive - title: " + titl + " / database? " + dataBase);

    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    // Synchronously check for necessary permissions
    checkDrivePermissions(account);

    final DriveResourceClient client = Drive.getDriveResourceClient(this, account);
    // task di recupero la cartella applicativa
    if (driveFolder != null && titl != null && mime != null && (!dataBase || file != null)) {
      // task di creazione content da file
      DriveContents driveContents = Tasks.await(client.createContents());
      if (dataBase) {
        OutputStream oos = driveContents.getOutputStream();
        if (oos != null)
          try {
            InputStream is = new FileInputStream(file);
            byte[] buf = new byte[4096];
            int c;
            while ((c = is.read(buf, 0, buf.length)) > 0) {
              oos.write(buf, 0, c);
              oos.flush();
            }
          } finally {
            oos.close();
          }
      } else {
        if (!saveSharedPreferencesToFile(driveContents.getOutputStream())) {
          return;
        }
      }

      // content's COOL, create metadata
      MetadataChangeSet meta =
          new MetadataChangeSet.Builder().setTitle(titl).setMimeType(mime).build();

      // task di creazione file in Google Drive
      DriveFile driveFile = Tasks.await(client.createFile(driveFolder, meta, driveContents));
      if (driveFile != null) {
        Metadata metadata = Tasks.await(client.getMetadata(driveFile));
        DriveId mDriveId = metadata.getDriveId();
        Log.d(getClass().getName(), "driveIdSaved: " + mDriveId);
        String error = "saveToDrive - FILE CARICATO";
        Log.d(getClass().getName(), error);
      }
    }
  }

  /**
   * **************************************************************** controlla se il file è già
   * esistente; se esiste lo cancella e poi lo ricrea
   *
   * @param titl file name
   */
  public boolean checkDupl(String titl)
      throws IOException, ExecutionException, InterruptedException, NoPermissioneException {
    boolean fileFound = false;
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    // Synchronously check for necessary permissions
    checkDrivePermissions(account);

    final DriveResourceClient client = Drive.getDriveResourceClient(this, account);
    // task di recupero la cartella applicativa
    DriveFolder folder = Tasks.await(client.getAppFolder());
    if (folder != null && titl != null) {
      // create content from file
      Log.d(getClass().getName(), "saveCheckDupl - title: " + titl);
      Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, titl)).build();

      // task di recupero metadata del file se già presente
      MetadataBuffer metadataBuffer = Tasks.await(client.query(query));

      int count = metadataBuffer.getCount();
      metadataBuffer.release();
      Log.d(getClass().getName(), "saveCheckDupl - Count files old: " + count);
      fileFound = count > 0;
    }
    return fileFound;
  }

  public void restoreNewDbBackup()
      throws ExecutionException, InterruptedException, NoPermissioneException, IOException {
    Log.d(
        getClass().getName(), "restoreNewDriveBackup - Db name: " + RisuscitoDatabase.getDbName());
    Query query =
        new Query.Builder()
            .addFilter(Filters.eq(SearchableField.TITLE, RisuscitoDatabase.getDbName()))
            .build();

    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    // Synchronously check for necessary permissions
    checkDrivePermissions(account);

    final DriveResourceClient client = Drive.getDriveResourceClient(this, account);

    MetadataBuffer metadataBuffer = Tasks.await(client.query(query));

    int count = metadataBuffer.getCount();
    Log.d(getClass().getName(), "restoreNewDriveBackup - Count files backup: " + count);
    if (count > 0) {
      DriveId mDriveId = metadataBuffer.get(count - 1).getDriveId();
      Log.d(getClass().getName(), "restoreNewDriveBackup - driveIdRetrieved: " + mDriveId);
      Log.d(
          getClass().getName(),
          "restoreNewDriveBackup - filesize in cloud " + metadataBuffer.get(0).getFileSize());
      metadataBuffer.release();

      DriveFile mFile = mDriveId.asDriveFile();

      DriveContents driveContents = Tasks.await(client.openFile(mFile, DriveFile.MODE_READ_ONLY));

      RisuscitoDatabase.getInstance(this);

      File dbFile = getNewDbPath();
      String path = dbFile.getPath();

      if (!dbFile.exists())
        //noinspection ResultOfMethodCallIgnored
        dbFile.delete();

      dbFile = new File(path);
      try {
        FileOutputStream fos = new FileOutputStream(dbFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        BufferedInputStream in = new BufferedInputStream(driveContents.getInputStream());

        byte[] buffer = new byte[1024];
        int n;
        while ((n = in.read(buffer)) > 0) {
          bos.write(buffer, 0, n);
          bos.flush();
        }
        bos.close();
      } catch (FileNotFoundException e) {
        client.discardContents(driveContents);
        throw e;
      } catch (IOException e) {
        client.discardContents(driveContents);
        throw e;
      }
      RisuscitoDatabase.getInstance(this);
    } else {
      metadataBuffer.release();
      Snackbar.make(
              findViewById(R.id.main_content), R.string.no_restore_found, Snackbar.LENGTH_LONG)
          .show();
    }
  }

  public void restoreOldDriveBackup()
      throws NoPermissioneException, ExecutionException, InterruptedException, IOException {
    Log.d(getClass().getName(), "restoreOldDriveBackup - Db name: " + DatabaseCanti.getDbName());
    Query query =
        new Query.Builder()
            .addFilter(Filters.eq(SearchableField.TITLE, DatabaseCanti.getDbName()))
            .build();

    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    // Synchronously check for necessary permissions
    checkDrivePermissions(account);

    final DriveResourceClient client = Drive.getDriveResourceClient(this, account);

    // esevuzione task per metadata da query
    MetadataBuffer metadataBuffer = Tasks.await(client.query(query));

    int count = metadataBuffer.getCount();
    Log.d(getClass().getName(), "restoreOldDriveBackup - Count files backup: " + count);
    if (count > 0) {
      DriveId mDriveId = metadataBuffer.get(count - 1).getDriveId();
      Log.d(getClass().getName(), "restoreOldDriveBackup - driveIdRetrieved: " + mDriveId);
      Log.d(
          getClass().getName(),
          "restoreOldDriveBackup - filesize in cloud " + metadataBuffer.get(0).getFileSize());
      metadataBuffer.release();

      DriveFile mFile = mDriveId.asDriveFile();
      // esecuzione task per recupero driveContent
      DriveContents driveContents = Tasks.await(client.openFile(mFile, DriveFile.MODE_READ_ONLY));

      DatabaseCanti listaCanti = new DatabaseCanti(this);
      listaCanti.close();

      File dbFile = getOldDbPath();
      String path = dbFile.getPath();

      if (!dbFile.exists())
        //noinspection ResultOfMethodCallIgnored
        dbFile.delete();

      dbFile = new File(path);
      try {
        FileOutputStream fos = new FileOutputStream(dbFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        BufferedInputStream in = new BufferedInputStream(driveContents.getInputStream());

        byte[] buffer = new byte[1024];
        int n;
        while ((n = in.read(buffer)) > 0) {
          bos.write(buffer, 0, n);
          bos.flush();
        }
        bos.close();
      } catch (FileNotFoundException e) {
        client.discardContents(driveContents);
        throw e;
      } catch (IOException e) {
        client.discardContents(driveContents);
        throw e;
      }

      listaCanti = new DatabaseCanti(this);
      listaCanti.close();
      client.discardContents(driveContents);

      RisuscitoDatabase.getInstance(this).importFromOldDB(this);

      checkDuplTosave(RisuscitoDatabase.getDbName(), "application/x-sqlite3", true);
    } else {
      metadataBuffer.release();
      restoreNewDbBackup();
    }
  }

  public void restoreDrivePrefBackup(String title)
      throws NoPermissioneException, ExecutionException, InterruptedException {
    Log.d(getClass().getName(), "restoreDrivePrefBackup - pref title: " + title);
    Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, title)).build();

    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    // Synchronously check for necessary permissions
    checkDrivePermissions(account);

    final DriveResourceClient client = Drive.getDriveResourceClient(this, account);

    MetadataBuffer metadataBuffer = Tasks.await(client.query(query));

    int count = metadataBuffer.getCount();
    Log.d(getClass().getName(), "restoreDrivePrefBackup - Count files backup: " + count);
    if (count > 0) {
      DriveId mDriveId = metadataBuffer.get(count - 1).getDriveId();
      Log.d(getClass().getName(), "restoreDrivePrefBackup - driveIdRetrieved: " + mDriveId);
      Log.d(
          getClass().getName(),
          "restoreDrivePrefBackup - filesize in cloud " + metadataBuffer.get(0).getFileSize());
      metadataBuffer.release();

      DriveFile mFile = mDriveId.asDriveFile();
      DriveContents driveContents = Tasks.await(client.openFile(mFile, DriveFile.MODE_READ_ONLY));

      loadSharedPreferencesFromFile(driveContents.getInputStream());
      client.discardContents(driveContents);
    } else {
      Snackbar.make(
              findViewById(R.id.main_content), R.string.no_restore_found, Snackbar.LENGTH_LONG)
          .show();
    }
  }

  private File getNewDbPath() {
    Log.d(getClass().getName(), "dbpath:" + getDatabasePath(RisuscitoDatabase.getDbName()));
    return getDatabasePath(RisuscitoDatabase.getDbName());
  }

  private File getOldDbPath() {
    Log.d(getClass().getName(), "dbpath:" + getDatabasePath(DatabaseCanti.getDbName()));
    return getDatabasePath(DatabaseCanti.getDbName());
  }

  private void checkDrivePermissions(GoogleSignInAccount account) throws NoPermissioneException {
    if (!GoogleSignIn.hasPermissions(account, Drive.SCOPE_FILE)) {
      // Note: this launches a sign-in flow, however the code to detect
      // the result of the sign-in flow and retry the API call is not
      // shown here.
      GoogleSignIn.requestPermissions(this, 9002, account, Drive.SCOPE_FILE);
      throw new NoPermissioneException();
    }
    if (!GoogleSignIn.hasPermissions(account, Drive.SCOPE_APPFOLDER)) {
      // Note: this launches a sign-in flow, however the code to detect
      // the result of the sign-in flow and retry the API call is not
      // shown here.
      GoogleSignIn.requestPermissions(this, 9003, account, Drive.SCOPE_APPFOLDER);
      throw new NoPermissioneException();
    }
  }

  public class NoPermissioneException extends Exception {
    NoPermissioneException() {
      super("no permission for drive SCOPE_FILE or SCOPE_APPFOLDER");
    }
  }
}
