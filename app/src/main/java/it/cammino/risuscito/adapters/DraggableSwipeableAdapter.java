package it.cammino.risuscito.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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

import java.util.List;

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
    //    private AbstractDataProvider mProvider;
    private EventListener mEventListener;
//    private View.OnClickListener mItemViewOnClickListener;
//    private View.OnClickListener mSwipeableViewContainerOnClickListener;

    public interface EventListener {
        void onItemRemoved(int position);

        void onItemMoved(int from, int to);
//        void onItemPinned(int position);

        //        void onItemViewClicked(View v, boolean pinned);
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
//            if(longClickListener != null)
//                v.setOnLongClickListener(longClickListener);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

//    public DraggableSwipeableAdapter(AbstractDataProvider dataProvider) {
//        mProvider = dataProvider;
//        mItemViewOnClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemViewClick(v);
//            }
//        };
//        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onSwipeableViewContainerClick(v);
//            }
//        };
//
//        // DraggableItemAdapter and SwipeableItemAdapter require stable ID, and also
//        // have to implement the getItemId() method appropriately.
//        setHasStableIds(true);
//    }
//
//    private void onItemViewClick(View v) {
//        if (mEventListener != null) {
//            mEventListener.onItemViewClicked(v, true); // true --- pinned
//        }
//    }
//
//    private void onSwipeableViewContainerClick(View v) {
//        if (mEventListener != null) {
//            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v));  // false --- not pinned
//        }
//    }

    @Override
    public long getItemId(int position) {
//        return mProvider.getItem(position).getId();
        return mData.get(position).getIdPosizione();
    }

//    @Override
//    public int getItemViewType(int position) {
//        return mProvider.getItem(position).getViewType();
//    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.position_list_item_light, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
//        final AbstractDataProvider.Data item = mProvider.getItem(position);
        final DraggableItem item = mData.get(position);

        // set listeners
        // (if the item is *not pinned*, click event comes to the itemView)
//        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        // (if the item is *pinned*, click event comes to the mContainer)
//        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null) {
                    mEventListener.onItemViewLongClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(view));
                }
                return true;
            }
        });

        Drawable drawable = DrawableCompat.wrap(holder.mDragImage.getBackground());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(activity, R.color.icon_ative_black));

        // set text
        holder.mTextView.setText(item.getTitolo());

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        final int swipeState = holder.getSwipeStateFlags();

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0)) {
//            int bgResId;

            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
//                bgResId = R.drawable.bg_item_dragging_active_state;
                setBackgroundGeneric(holder.mContainer, ((ThemeableActivity) activity).getThemeUtils().accentColorLight());
            } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
//                bgResId = R.drawable.bg_item_dragging_state;
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.mContainer.setBackgroundResource(typedValue.resourceId);
            } else if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_ACTIVE) != 0) {
//                bgResId = R.drawable.bg_item_swiping_active_state;
                setBackgroundGeneric(holder.mContainer, ((ThemeableActivity) activity).getThemeUtils().accentColorLight());
            } else if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_SWIPING) != 0) {
//                bgResId = R.drawable.bg_item_swiping_state;
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.mContainer.setBackgroundResource(typedValue.resourceId);
            } else {
//                bgResId = R.drawable.bg_item_normal_state;
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.mContainer.setBackgroundResource(typedValue.resourceId);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent));
            }

//            holder.mContainer.setBackgroundResource(bgResId);
        }

        // set swiping properties
//        holder.setSwipeItemSlideAmount(
//                item.isPinnedToSwipeLeft() ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
    }

    @Override
    public int getItemCount() {
//        return mProvider.getCount();
        return mData.size();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }

//        mProvider.moveItem(fromPosition, toPosition);
        mData.add(toPosition, mData.remove(fromPosition));

        notifyItemMoved(fromPosition, toPosition);

        if (mEventListener != null) {
            mEventListener.onItemMoved(fromPosition, toPosition);
        }
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
//        if (onCheckCanStartDrag(holder, position, x, y)) {
//            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
//        } else {
//            return mProvider.getItem(position).getSwipeReactionType();
//        }
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
    }

    @Override
    public void onSetSwipeBackground(MyViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
//                bgRes = R.drawable.bg_swipe_item_neutral;
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                holder.itemView.setBackgroundResource(typedValue.resourceId);
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
//                bgRes = R.drawable.bg_swipe_item_left;
                holder.itemView.setBackgroundResource(R.drawable.bg_swipe_item_left);
//                setBackgroundGeneric(holder.itemView, ((ThemeableActivity)activity).getThemeUtils().accentColorDark());
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
//                bgRes = R.drawable.bg_swipe_item_right;
                holder.itemView.setBackgroundResource(R.drawable.bg_swipe_item_right);
//                setBackgroundGeneric(holder.itemView, ((ThemeableActivity) activity).getThemeUtils().accentColorDark());
                break;
        }

//        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, int result) {
        Log.d(TAG, "onSwipeItem(result = " + result + ")");

        switch (result) {
            // swipe right
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
//                if (mProvider.getItem(position).isPinnedToSwipeLeft()) {
//                    // pinned --- back to default position
//                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
//                } else {
//                    // not pinned --- remove
//                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
//                }
//                // swipe left -- pin
                return new SwipeRightResultAction(this , position);
//                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return new SwipeRightResultAction(this , position);
//                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            // other --- do nothing
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return new UnpinResultAction();
//                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
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

//    @Override
//    public void onPerformAfterSwipeReaction(MyViewHolder holder, int position, int result, int reaction) {
//        Log.d(TAG, "onPerformAfterSwipeReaction(result = " + result + ", reaction = " + reaction + ")");
//
////        final AbstractDataProvider.Data item = mProvider.getItem(position);
////        final DraggableItem item = mData.get(position);
//
//        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
////            mProvider.removeItem(position);
//            mData.remove(position);
//            notifyItemRemoved(position);
//
//            if (mEventListener != null) {
//                mEventListener.onItemRemoved(position);
//            }
//        }
////        else if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION) {
////            item.setPinnedToSwipeLeft(true);
////            notifyItemChanged(position);
////
////            if (mEventListener != null) {
////                mEventListener.onItemPinned(position);
////            }
////        } else {
////            item.setPinnedToSwipeLeft(false);
////        }
//    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setBackgroundGeneric(View v, int color) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN))
            v.setBackground(new ColorDrawable(color));
        else
            v.setBackgroundDrawable(new ColorDrawable(color));
    }
}
