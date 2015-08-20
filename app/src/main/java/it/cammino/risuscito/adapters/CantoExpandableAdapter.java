package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.wnafee.vector.MorphButton;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.morph.MorphButtonCompat;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.objects.ExpandableGroup;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class CantoExpandableAdapter
        extends AbstractExpandableItemAdapter<CantoExpandableAdapter.GroupViewHolder, CantoExpandableAdapter.CantoViewHolder> {

    private List<Pair<ExpandableGroup, List<CantoRecycled>>> mData;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    private Activity activity;

    // Adapter constructor 1
    public CantoExpandableAdapter(Activity activity, List<Pair<ExpandableGroup, List<CantoRecycled>>> dataItems
            , View.OnClickListener clickListener
            , View.OnCreateContextMenuListener createContextMenuListener) {
        this.activity = activity;
        this.mData = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.createContextMenuListener = createContextMenuListener;
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return ((List)mData.get(groupPosition).second).size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return (mData.get(groupPosition).first).getIdGruppo();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return ((CantoRecycled)((List) mData.get(groupPosition).second).get(childPosition)).getIdCanto();
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_group_item, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public CantoViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.row_item, parent, false);
        return new CantoViewHolder(v, clickListener, longClickListener, createContextMenuListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, int groupPosition, int viewType) {
        // child item
        ExpandableGroup dataItem = mData.get(groupPosition).first;

        // set text
        holder.groupTitle.setText(dataItem.getTitolo());
        holder.groupId.setText(String.valueOf(dataItem.getIdGruppo()));

        // mark as clickable
        holder.itemView.setClickable(true);

        // set background resource (target view ID: container)
        final int expandState = holder.getExpandStateFlags();

        if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) {
//            int bgResId;
            MorphButton.MorphState indicatorState;

            if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
//                bgResId = R.drawable.bg_group_item_expanded_state;
                indicatorState = MorphButton.MorphState.END;
            } else {
//                bgResId = R.drawable.bg_group_item_normal_state;
                indicatorState = MorphButton.MorphState.START;
            }

//            holder.mContainer.setBackgroundResource(bgResId);

            if (holder.mMorphButton.getState() != indicatorState) {
                holder.mMorphButton.setState(indicatorState, true);
            }
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
                Drawable drawable;
                if (indicatorState == MorphButton.MorphState.END)
                    drawable = activity.getResources().getDrawable(R.drawable.ic_expand_less_black_24dp);
//                    holder.mMorphButtonOld.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                else
                    drawable = activity.getResources().getDrawable(R.drawable.ic_expand_more_black_24dp);
//                    holder.mMorphButtonOld.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, activity.getResources().getColor(R.color.icon_ative_black));
                holder.mMorphButtonOld.setBackgroundDrawable(drawable);
            }
        }
    }

    @Override
    public void onBindChildViewHolder(CantoViewHolder holder, int groupPosition, int childPosition, int viewType) {
        // group item
        CantoRecycled dataItem = (CantoRecycled) ((List) mData.get(groupPosition).second).get(childPosition);

        holder.cantoTitle.setText(dataItem.getTitolo());
        holder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
        holder.cantoId.setText(String.valueOf(dataItem.getIdCanto()));
        holder.cantoSource.setText(dataItem.getSource());
        if (dataItem.getColore().equalsIgnoreCase(Utility.GIALLO))
            holder.cantoPage.setBackgroundResource(R.drawable.bkg_round_yellow);
        if (dataItem.getColore().equalsIgnoreCase(Utility.GRIGIO))
            holder.cantoPage.setBackgroundResource(R.drawable.bkg_round_grey);
        if (dataItem.getColore().equalsIgnoreCase(Utility.VERDE))
            holder.cantoPage.setBackgroundResource(R.drawable.bkg_round_green);
        if (dataItem.getColore().equalsIgnoreCase(Utility.AZZURRO))
            holder.cantoPage.setBackgroundResource(R.drawable.bkg_round_blue);
        if (dataItem.getColore().equalsIgnoreCase(Utility.BIANCO))
            holder.cantoPage.setBackgroundResource(R.drawable.bkg_round_white);
    }

    public static class CantoViewHolder extends AbstractExpandableItemViewHolder {

        public TextView cantoTitle;
        public TextView cantoPage;
        public TextView cantoId;
        public TextView cantoSource;

        public CantoViewHolder(View itemView
                , View.OnClickListener onClickListener
                , View.OnLongClickListener onLongClickListener
                , View.OnCreateContextMenuListener onCreateContextMenuListener) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            cantoId = (TextView) itemView.findViewById(R.id.text_id_canto);
            cantoSource = (TextView) itemView.findViewById(R.id.text_source_canto);
            if (onClickListener != null)
                itemView.setOnClickListener(onClickListener);
            if (onLongClickListener != null)
                itemView.setOnLongClickListener(onLongClickListener);
            if (onCreateContextMenuListener != null)
                itemView.setOnCreateContextMenuListener(onCreateContextMenuListener);
        }

    }

    public static class GroupViewHolder extends AbstractExpandableItemViewHolder {

        public TextView groupTitle;
        public TextView groupId;
        public MorphButtonCompat mMorphButton;
        public ImageView mMorphButtonOld;
        public ViewGroup mContainer;

        public GroupViewHolder(View itemView) {
            super(itemView);
            mMorphButton = new MorphButtonCompat(itemView.findViewById(R.id.indicator));
            mMorphButtonOld = (ImageView) itemView.findViewById(R.id.indicatorOld);
            groupTitle = (TextView) itemView.findViewById(android.R.id.text1);
            groupId = (TextView) itemView.findViewById(R.id.text_id_gruppo);
            mContainer = (ViewGroup) itemView.findViewById(R.id.container);
        }

    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // check the item is *not* pinned
//        if (mProvider.getGroupItem(groupPosition).isPinnedToSwipeLeft()) {
//            // return false to raise View.OnClickListener#onClick() event
//            return false;
//        }

        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

//        final View containerView = holder.itemView;
////        final View dragHandleView = holder.mDragHandle;
//
//        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
//        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);
//
////        return !ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
        return true;
    }

}
