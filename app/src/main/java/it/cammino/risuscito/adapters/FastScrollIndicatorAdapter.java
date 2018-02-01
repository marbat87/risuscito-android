package it.cammino.risuscito.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import it.cammino.risuscito.R;
import it.cammino.risuscito.items.SimpleItem;

public class FastScrollIndicatorAdapter<Item extends IItem> extends FastItemAdapter<Item>
    implements FastScrollRecyclerView.SectionedAdapter, FastScrollRecyclerView.MeasurableAdapter {

  private final String TAG = getClass().getCanonicalName();

  private int mIndicator;

  public FastScrollIndicatorAdapter(int indicator) {
    super();
    this.mIndicator = indicator;
  }

  @NonNull
  @Override
  public String getSectionName(int position) {
    Log.d(TAG, "getSectionName: position " + position);
    IItem item = getAdapterItem(position);
    Log.d(TAG, "getSectionName: " + ((SimpleItem) item).getTitle().getText());
    switch (mIndicator) {
      case 0:
        return ((SimpleItem) item).getTitle().getText().toString().substring(0, 1).toUpperCase();
      case 1:
        return ((SimpleItem) item).getPage().getText().toString();
      case 2:
        return String.valueOf(((SimpleItem) item).getNumSalmo());
      default:
        return ((SimpleItem) item).getTitle().getText().toString().substring(0, 1).toUpperCase();
    }
  }

  @Override
  public int getViewTypeHeight(RecyclerView recyclerView, int viewType) {
    return recyclerView
        .getResources()
        .getDimensionPixelSize(R.dimen.myListPreferredItemHeightLarge);
  }
}
