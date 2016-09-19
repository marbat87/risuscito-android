package it.cammino.risuscito.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.R;
import it.cammino.risuscito.objects.DraggableItem;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ViewUtils;

public class DraggableSwipeableAdapter
        extends RecyclerView.Adapter<DraggableSwipeableAdapter.MyViewHolder>
        implements DraggableItemAdapter<DraggableSwipeableAdapter.MyViewHolder>,
        SwipeableItemAdapter<DraggableSwipeableAdapter.MyViewHolder> {
    private static final String TAG = "MyDSItemAdapter";

    private List<DraggableItem> mData;
    private Activity activity;
    private EventListener mEventListener;

    public interface EventListener {
        void onItemRemoved(int position);
        void onItemMoved(int from, int to);
        void onItemViewLongClicked(View v);
    }

    // Adapter constructor 1
    public DraggableSwipeableAdapter(Activity activity, List<DraggableItem> dataItems) {
        this.activity = activity;
        this.mData = dataItems;
        setHasStableIds(true);
    }

    public static class MyViewHolder extends AbstractDraggableSwipeableItemViewHolder {
        public ViewGroup mContainer;
        public View mDragHandle;
        public TextView mTextView;
        public ImageView mDragImage;

        public MyViewHolder(View v) {
            super(v);
            mContainer = (ViewGroup) v.findViewById(R.id.container);
            mDragHandle = v.findViewById(R.id.drag_handle);
            mTextView = (TextView) v.findViewById(android.R.id.text1);
            mDragImage = (ImageView) v.findViewById(R.id.drag_image);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }


    @Override
    public long getItemId(int position) {
        return mData.get(position).getIdPosizione();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.position_list_item_light, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final DraggableItem item = mData.get(position);

        holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null) {
                    mEventListener.onItemViewLongClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(view));
                }
                return true;
            }
        });

//        Drawable drawable = DrawableCompat.wrap(holder.mDragImage.getBackground());
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(activity, R.color.icon_ative_black));
//        holder.mDragImage.setImageResource(R.drawable.ic_reorder_24dp);
        IconicsDrawable icon = new IconicsDrawable(activity)
                .icon(GoogleMaterial.Icon.gmd_reorder)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(24)
                .paddingDp(2);
        holder.mDragImage.setImageDrawable(icon);
        holder.mDragImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // set text
        holder.mTextView.setText(item.getTitolo());

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        final int swipeState = holder.getSwipeStateFlags();

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0)) {

            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                setBackgroundGeneric(holder.mContainer, ((ThemeableActivity) activity).getThemeUtils().accentColorLight());
            } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.mContainer.setBackgroundResource(typedValue.resourceId);
            } else if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_ACTIVE) != 0) {
                setBackgroundGeneric(holder.mContainer, ((ThemeableActivity) activity).getThemeUtils().accentColorLight());
            } else if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_SWIPING) != 0) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.mContainer.setBackgroundResource(typedValue.resourceId);
            } else {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.mContainer.setBackgroundResource(typedValue.resourceId);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent));
            }

        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }

        mData.add(toPosition, mData.remove(fromPosition));

        notifyItemMoved(fromPosition, toPosition);

        if (mEventListener != null) {
            mEventListener.onItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * Called while dragging in order to check whether the dragging item can be dropped to the specified position.
     * <p/>
     * NOTE: This method will be called when the checkCanDrop option is enabled by {@link RecyclerViewDragDropManager#setCheckCanDropEnabled(boolean)}.
     *
     * @param draggingPosition The position of the currently dragging item.
     * @param dropPosition     The position to check whether the dragging item can be dropped or not.
     * @return Whether can be dropped to the specified position.
     */
    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return false;
    }

    @Override
    public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(MyViewHolder holder, int position, int type) {
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.itemView.setBackgroundResource(typedValue.resourceId);
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                holder.itemView.setBackgroundResource(R.drawable.bg_swipe_item_left);
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                holder.itemView.setBackgroundResource(R.drawable.bg_swipe_item_right);
                break;
        }

    }

    @Override
    public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, int result) {
        Log.d(TAG, "onSwipeItem(result = " + result + ")");

        switch (result) {
            // swipe right
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                // swipe left -- pin
                return new SwipeRightResultAction(this , position);
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return new SwipeRightResultAction(this , position);
            // other --- do nothing
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return new UnpinResultAction();
        }
    }

    private static class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private DraggableSwipeableAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(DraggableSwipeableAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            mAdapter.mData.remove(mPosition);
            mAdapter.notifyItemRemoved(mPosition);
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private static class UnpinResultAction extends SwipeResultActionDefault {
        @Override
        protected void onPerformAction() {
            super.onPerformAction();
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setBackgroundGeneric(View v, int color) {
        if (LUtils.hasJB())
            v.setBackground(new ColorDrawable(color));
        else
            v.setBackgroundDrawable(new ColorDrawable(color));
    }
}
