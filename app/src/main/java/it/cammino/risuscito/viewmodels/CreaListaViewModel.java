package it.cammino.risuscito.viewmodels;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.items.SwipeableItem;

public class CreaListaViewModel extends ViewModel {

  public int positionToRename = 0;

  public String tempTitle = "";

  public ArrayList<String> data;
  public List<SwipeableItem> dataDrag;

}
