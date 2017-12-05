package it.cammino.risuscito.viewmodels;

import android.arch.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

  public boolean showSnackbar = true;
  public boolean dbRestoreRunning;
  public boolean prefRestoreRunning;
  public boolean dbBackupRunning;
  public boolean prefBackupRunning;
}
