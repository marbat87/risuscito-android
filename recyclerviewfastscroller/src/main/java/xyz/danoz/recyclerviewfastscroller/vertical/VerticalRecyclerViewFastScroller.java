package xyz.danoz.recyclerviewfastscroller.vertical;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import xyz.danoz.recyclerviewfastscroller.AbsRecyclerViewFastScroller;
import xyz.danoz.recyclerviewfastscroller.R;
import xyz.danoz.recyclerviewfastscroller.RecyclerViewScroller;
import xyz.danoz.recyclerviewfastscroller.calculation.VerticalScrollBoundsProvider;
import xyz.danoz.recyclerviewfastscroller.calculation.count.NumberItemsPerPageCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.count.VerticalLinearLayoutManagerNumberItemsPerPageCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.position.VerticalScreenPositionCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.progress.TouchableScrollProgressCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.progress.VerticalLinearLayoutManagerScrollProgressCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.progress.VerticalScrollProgressCalculator;
import xyz.danoz.recyclerviewfastscroller.utils.ViewUtils;

/**
 * Widget used to fast-scroll a vertical {@link RecyclerView}.
 * Currently assumes the use of a {@link LinearLayoutManager}
 */
public class VerticalRecyclerViewFastScroller extends AbsRecyclerViewFastScroller implements RecyclerViewScroller {

    //    private VerticalScrollProgressCalculator mScrollProgressCalculator;
//    private VerticalScreenPositionCalculator mScreenPositionCalculator;
    @Nullable private VerticalScrollProgressCalculator mScrollProgressCalculator;
    @Nullable private VerticalScreenPositionCalculator mScreenPositionCalculator;
    private NumberItemsPerPageCalculator mNumberItemsPerPageCalculator;


    public VerticalRecyclerViewFastScroller(Context context) {
        this(context, null);
    }

    public VerticalRecyclerViewFastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRecyclerViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.rvfs_default_vertical_layout;
    }

    @Override
    @Nullable
    protected TouchableScrollProgressCalculator getScrollProgressCalculator() {
        return mScrollProgressCalculator;
    }

    @Override
    public void moveHandleToPosition(float scrollProgress) {
        if (mScreenPositionCalculator == null) {
            return;
        }
        final float yPos = mScreenPositionCalculator.getYPositionFromScrollProgress(scrollProgress);
        ViewUtils.setTranslationY(mHandle, yPos);
    }

    @Override
    public float getNumItemsPerPage(RecyclerView recyclerView) {
        if (mNumberItemsPerPageCalculator == null) {
            mNumberItemsPerPageCalculator = new VerticalLinearLayoutManagerNumberItemsPerPageCalculator();
        }
        return mNumberItemsPerPageCalculator.calculateNumItemsPerPage(recyclerView);
    }

    @Override
    protected void setStandardScrollerEnabled(RecyclerView recyclerView, boolean enabled) {
        recyclerView.setVerticalScrollBarEnabled(enabled);
    }

    @Override
    protected Animation loadShowAnimation() {
        return AnimationUtils.loadAnimation(getContext(), R.anim.rvfs_fast_scroller_show_slide_in);
    }

    @Override
    protected Animation loadHideAnimation() {
        return AnimationUtils.loadAnimation(getContext(), R.anim.rvfs_fast_scroller_hide_slide_out);
    }

    @Override
    protected void onUpdateScrollBarBounds(Rect barBounds) {
        VerticalScrollBoundsProvider boundsProvider = new VerticalScrollBoundsProvider(barBounds.top, barBounds.bottom);
        mScrollProgressCalculator = new VerticalLinearLayoutManagerScrollProgressCalculator(boundsProvider);
        mScreenPositionCalculator = new VerticalScreenPositionCalculator(boundsProvider);
    }

    @Override
    protected int scrollToProgress(RecyclerView recyclerView, float progress) {
        final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        final int height = layoutManager.getHeight();
        final int itemCount = layoutManager.getItemCount();

        if (height == 0 || itemCount == 0) {
            return 0;
        }

        final float fScrollPosition = Math.max(0.0f, (itemCount - 1)) * progress;
        final int iScrollPosition = (int) fScrollPosition;

        final int firstVisiblePos = layoutManager.findFirstVisibleItemPosition();
        final int lastVisiblePos = layoutManager.findLastVisibleItemPosition();

        final int lastCompletelyVisiblePos = layoutManager.findLastCompletelyVisibleItemPosition();

        if (firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION) {
            return 0;
        }

        if (iScrollPosition >= firstVisiblePos && iScrollPosition <= lastVisiblePos) {
            final RecyclerView.ViewHolder scrollPosViewHolder = recyclerView.findViewHolderForLayoutPosition(iScrollPosition);
            final RecyclerView.ViewHolder lastItemViewHolder = (lastVisiblePos == (itemCount - 1)) ? recyclerView.findViewHolderForLayoutPosition(lastVisiblePos) : null;
            final int curItemTop = scrollPosViewHolder.itemView.getTop();
            final int remainScrollableDistance = (lastItemViewHolder != null) ? Math.max(0, (lastItemViewHolder.itemView.getBottom() - height)) : Integer.MAX_VALUE;
            final int scrollAmount = Math.min(curItemTop, remainScrollableDistance);

            if ((scrollAmount > 0) && (lastCompletelyVisiblePos == (itemCount - 1))) {
                // reached to the end of the list
            } else {
                recyclerView.scrollBy(0, scrollAmount);
            }
        } else {
            recyclerView.scrollToPosition(iScrollPosition);
        }

        return iScrollPosition;
    }

    @Override
    protected boolean isScrollerOrientationSizeChanged(int w, int h, int oldw, int oldh) {
        return (h != oldh);
    }
}
