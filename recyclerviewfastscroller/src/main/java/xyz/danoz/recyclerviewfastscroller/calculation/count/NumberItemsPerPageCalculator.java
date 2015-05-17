package xyz.danoz.recyclerviewfastscroller.calculation.count;

import android.support.v7.widget.RecyclerView;

/**
 * Assists in calculating the amount of items per page for a {@link RecyclerView}
 */
public interface NumberItemsPerPageCalculator {
    /**
     * @param recyclerView RecyclerView
     * @return the number of the item views which can be displayed per page
     */
    public float calculateNumItemsPerPage(RecyclerView recyclerView);
}

