package it.cammino.risuscito.items;

import android.support.v13.view.ViewCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.ExpandableViewHolder;
import it.cammino.risuscito.R;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractModelItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableHeaderItem
        extends AbstractFlexibleItem<ExpandableHeaderItem.ExpandableHeaderViewHolder>
        implements IExpandable<ExpandableHeaderItem.ExpandableHeaderViewHolder, SubItem>,
        IHeader<ExpandableHeaderItem.ExpandableHeaderViewHolder> {

    protected String id;
    protected String title;
    protected int activeColor;
    protected String subtitle = "";

    /* Flags for FlexibleAdapter */
    private boolean mExpanded = false;

    /* subItems list */
    private List<SubItem> mSubItems;


    public ExpandableHeaderItem(String id, String title) {
        this.id = id;
        this.title = title;
        //We start with header shown and expanded
        setHidden(false);
        setExpanded(false);
        //NOT selectable (otherwise ActionMode will be activated on long click)!
        setSelectable(false);
    }


    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof ExpandableHeaderItem) {
            ExpandableHeaderItem inItem = (ExpandableHeaderItem) inObject;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    /**
     * Override this method too, when using functionalities like StableIds, Filter or CollapseAll.
     * FlexibleAdapter is making use of HashSet to improve performance, especially in big list.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<SubItem> getSubItems() {
        return mSubItems;
    }

    public final boolean hasSubItems() {
        return mSubItems!= null && mSubItems.size() > 0;
    }

    public boolean removeSubItem(SubItem item) {
        return item != null && mSubItems.remove(item);
    }

    public boolean removeSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.remove(position);
            return true;
        }
        return false;
    }

    public void addSubItem(SubItem subItem) {
        if (mSubItems == null)
            mSubItems = new ArrayList<>();
        mSubItems.add(subItem);
    }

    public void addSubItem(int position, SubItem subItem) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.add(position, subItem);
        } else
            addSubItem(subItem);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.list_group_item;
    }

    @Override
    public ExpandableHeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ExpandableHeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ExpandableHeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads + " - " + getTitle());
        } else {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem NoPayload - " + getTitle());
            holder.mTitle.setText(getTitle());
        }
//        setSubtitle(String.valueOf(adapter.getCurrentChildren(this).size()) +
//                " subItems (" + (isExpanded() ? "expanded" : "collapsed") + ")");
        if (getSubtitle() != null && !getSubtitle().isEmpty()) {
            holder.mSubtitle.setText(getSubtitle());
            holder.mSubtitle.setVisibility(View.VISIBLE);
        }
//        IconicsDrawable drawable;
//        if (isExpanded()) {
//            drawable = new IconicsDrawable(holder.itemView.getContext())
//                    .icon(CommunityMaterial.Icon.cmd_chevron_up)
//                    .colorRes(android.R.color.white)
//                    .sizeDp(24)
//                    .paddingDp(5);
//        } else {
//            drawable = new IconicsDrawable(holder.itemView.getContext())
//                    .icon(CommunityMaterial.Icon.cmd_chevron_down)
//                    .colorRes(android.R.color.white)
//                    .sizeDp(24)
//                    .paddingDp(5);
//        }
//        holder.mIndicator.setImageDrawable(drawable);
        holder.itemView.setBackgroundColor(getActiveColor());
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static class ExpandableHeaderViewHolder extends ExpandableViewHolder {

        TextView mTitle;
        TextView mSubtitle;
        ImageView mIndicator;

        ExpandableHeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = (TextView) view.findViewById(R.id.title);
            mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            this.mIndicator = (ImageView) view.findViewById(R.id.indicator);
//            if (adapter.isHandleDragEnabled()) {
//                this.mHandleView.setVisibility(View.VISIBLE);
//                setDragHandleView(mHandleView);
//            } else {
//                this.mHandleView.setVisibility(View.GONE);
//            }

            //Support for StaggeredGridLayoutManager
            if (itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams()).setFullSpan(true);
            }
        }

        /**
         * Allows to expand or collapse child views of this itemView when {@link View.OnClickListener}
         * event occurs on the entire view.
         * <p>This method returns always true; Extend with "return false" to Not expand or collapse
         * this ItemView onClick events.</p>
         *
         * @return always true, if not overridden
         * @since 5.0.0-b1
         */
        @Override
        protected boolean isViewExpandableOnClick() {
            return true;//default=true
        }

        /**
         * Allows to collapse child views of this ItemView when {@link View.OnLongClickListener}
         * event occurs on the entire view.
         * <p>This method returns always true; Extend with "return false" to Not collapse this
         * ItemView onLongClick events.</p>
         *
         * @return always true, if not overridden
         * @since 5.0.0-b1
         */
        protected boolean isViewCollapsibleOnLongClick() {
            return true;//default=true
        }

        /**
         * Allows to notify change and rebound this itemView on expanding and collapsing events,
         * in order to update the content (so, user can decide to display the current expanding status).
         * <p>This method returns always false; Override with {@code "return true"} to trigger the
         * notification.</p>
         *
         * @return true to rebound the content of this itemView on expanding and collapsing events,
         * false to ignore the events
         * @see #expandView(int)
         * @see #collapseView(int)
         * @since 5.0.0-rc1
         */
        @Override
        protected boolean shouldNotifyParentOnClick() {
            return true;//default=false
        }

        /**
         * Expands or Collapses based on the current state.
         *
         * @see #shouldNotifyParentOnClick()
         * @see #expandView(int)
         * @see #collapseView(int)
         * @since 5.0.0-b1
         */
        @Override
        protected void toggleExpansion() {
            super.toggleExpansion(); //If overridden, you must call the super method
        }

        /**
         * Triggers expansion of this itemView.
         * <p>If {@link #shouldNotifyParentOnClick()} returns {@code true}, this view is rebound
         * with payload {@link Payload#EXPANDED}.</p>
         *
         * @see #shouldNotifyParentOnClick()
         * @since 5.0.0-b1
         */
        @Override
        protected void expandView(int position) {
            super.expandView(position); //If overridden, you must call the super method
            ViewCompat.animate(getContentView().findViewById(R.id.indicator)).rotation(180).start();
            // Let's notify the item has been expanded. Note: from 5.0.0-rc1 the next line becomes
            // obsolete, override the new method shouldNotifyParentOnClick() as showcased here
            //if (mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
        }

        /**
         * Triggers collapse of this itemView.
         * <p>If {@link #shouldNotifyParentOnClick()} returns {@code true}, this view is rebound
         * with payload {@link Payload#COLLAPSED}.</p>
         *
         * @see #shouldNotifyParentOnClick()
         * @since 5.0.0-b1
         */
        @Override
        protected void collapseView(int position) {
            super.collapseView(position); //If overridden, you must call the super method
            ViewCompat.animate(getContentView().findViewById(R.id.indicator)).rotation(0).start();
            // Let's notify the item has been collapsed. Note: from 5.0.0-rc1 the next line becomes
            // obsolete, override the new method shouldNotifyParentOnClick() as showcased here
            //if (!mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
        }

    }

    @Override
    public String toString() {
        return "ExpandableHeaderItem[id=" + this.id + ", title=" + getTitle() + "//SubItems" + mSubItems + "]";
    }

}