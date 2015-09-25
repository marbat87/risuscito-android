package xyz.danoz.recyclerviewfastscroller.sectionindicator;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.widget.SectionIndexer;

/**
 * An indicator for which section a {@link RecyclerView} is currently in. This is for RecyclerViews whose adapters
 * implement the {@link SectionIndexer} interface.
 */
public interface SectionIndicator<T> {

    /**
     * Sets the progress of the indicator
     * @param progress fraction from [0 to 1] representing progress scrolled through a RecyclerView
     */
    public void setProgress(float progress);

    /**
     * Allows the setting of section types in the indicator. The indicator should appropriately handle the section type
     * @param section the current section to which the list is scrolled
     */
    public void setSection(T section);

    /**
     * Method for show the indicator with animation
     */
    public void showWithAnimation();

    /**
     * Method for hide the indicator with animation
     */
    public void hideWithAnimation();

    /**
     * Called when scroll bar's bounds are updated
     *
     * @param barBounds the bounds of scroll bar view
     */
    public void onUpdateScrollBarBounds(Rect barBounds);
}
