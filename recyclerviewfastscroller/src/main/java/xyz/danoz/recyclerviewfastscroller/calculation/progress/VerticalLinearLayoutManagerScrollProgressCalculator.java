package xyz.danoz.recyclerviewfastscroller.calculation.progress;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import xyz.danoz.recyclerviewfastscroller.AbsRecyclerViewFastScroller;
import xyz.danoz.recyclerviewfastscroller.calculation.VerticalScrollBoundsProvider;

/**
 * Calculates scroll progress for a {@link RecyclerView} with a {@link LinearLayoutManager}
 */
public class VerticalLinearLayoutManagerScrollProgressCalculator extends VerticalScrollProgressCalculator {

    public VerticalLinearLayoutManagerScrollProgressCalculator(VerticalScrollBoundsProvider scrollBoundsProvider) {
        super(scrollBoundsProvider);
    }

    /**
     * @param recyclerView recycler that experiences a scroll event
     * @return the progress through the recycler view list content
     */
    @Override
    public float calculateScrollProgress(RecyclerView recyclerView) {
        final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        final int numItemsInList = layoutManager.getItemCount();
        final int height = layoutManager.getHeight();

        if (numItemsInList <= 1 || height <= 0) {
            return 0.0f;
        }

        final int firstVisiblePos = layoutManager.findFirstVisibleItemPosition();
        final int lastVisiblePos = layoutManager.findLastVisibleItemPosition();

        if (firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION) {
            return 0.0f;
        }

        final int firstCompletelyVisiblePos = layoutManager.findFirstCompletelyVisibleItemPosition();
        final int lastCompletelyVisiblePos = layoutManager.findLastCompletelyVisibleItemPosition();

        if (firstCompletelyVisiblePos <= 0) {
            return 0.0f;
        } else if (lastCompletelyVisiblePos >= (numItemsInList - 1)) {
            return 1.0f;
        } else {
            final View incompleteFirstItem = (firstCompletelyVisiblePos != firstVisiblePos)
                    ? recyclerView.findViewHolderForLayoutPosition(firstVisiblePos).itemView : null;
            final View incompleteLastItem = (lastCompletelyVisiblePos != lastVisiblePos)
                    ? recyclerView.findViewHolderForLayoutPosition(lastVisiblePos).itemView : null;

            final int numCompletelyVisibleItems;
            final float incompleteFirstItemVisibleProportion;
            final float incompleteLastItemVisibleProportion;

            if (firstCompletelyVisiblePos != RecyclerView.NO_POSITION && lastCompletelyVisiblePos != RecyclerView.NO_POSITION) {
                numCompletelyVisibleItems = (lastCompletelyVisiblePos - firstCompletelyVisiblePos + 1);
            } else {
                numCompletelyVisibleItems = 0;
            }

            if (incompleteFirstItem != null) {
                final int top = incompleteFirstItem.getTop();
                final int bottom =  incompleteFirstItem.getBottom();

                incompleteFirstItemVisibleProportion = safeFloatDiv((bottom - Math.max(0, top)), (bottom - top));
            } else {
                incompleteFirstItemVisibleProportion = 0.0f;
            }

            if (incompleteLastItem != null) {
                final int top = incompleteLastItem.getTop();
                final int bottom = incompleteLastItem.getBottom();

                incompleteLastItemVisibleProportion = safeFloatDiv((Math.min(height, bottom) - top), (bottom - top));
            } else {
                incompleteLastItemVisibleProportion = 0.0f;
            }

            if (lastVisiblePos == (numItemsInList - 1)) {
                final float base = (numItemsInList - 1 - (numCompletelyVisibleItems + incompleteLastItemVisibleProportion + incompleteFirstItemVisibleProportion)) / (numItemsInList - 1);
                return base + ((1.0f - base) * incompleteLastItemVisibleProportion);
            } else {
                return (firstCompletelyVisiblePos - incompleteFirstItemVisibleProportion) / (numItemsInList - 1);
            }
        }
    }

    private static float safeFloatDiv(int dividend, int divisor) {
        if (divisor == 0) {
            return 0.0f;
        } else {
            return (float) dividend / divisor;
        }
    }
}
