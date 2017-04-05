package it.cammino.risuscito.items;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.flexibleadapter.utils.Utils;
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
        Context mContext = holder.itemView.getContext();
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads + " - " + getTitle());
        } else {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem NoPayload - " + getTitle());
            holder.mTitle.setText(getTitle());
        }
        if (getSubtitle() != null && !getSubtitle().isEmpty()) {
            holder.mSubtitle.setText(getSubtitle());
            holder.mSubtitle.setVisibility(View.VISIBLE);
        }

        Drawable drawable = getSelectableBackgroundCompat(
                getActiveColor(), ContextCompat.getColor(mContext, R.color.ripple_color), //Same color of divider
                ContextCompat.getColor(mContext, R.color.ripple_color));
        DrawableUtils.setBackgroundCompat(holder.getContentView(), drawable);

//        DrawableUtils.setBackgroundCompat(holder.getContentView(), DrawableUtils.getRippleDrawable(
//                DrawableUtils.getColorDrawable(getActiveColor()),
//                DrawableUtils.getColorControlHighlight(holder.getContentView().getContext()))
//        );
//        holder.getContentView().setBackgroundColor(getActiveColor());
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

    private static StateListDrawable getStateListDrawable(@ColorInt int normalColor, @ColorInt int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{16843518}, getColorDrawable(pressedColor));
        states.addState(new int[0], getColorDrawable(normalColor));
        if(!Utils.hasLollipop() || Utils.hasNougat()) {
            short duration = 200;
            states.setEnterFadeDuration(duration);
            states.setExitFadeDuration(duration);
        }

        return states;
    }

    public static ColorDrawable getColorDrawable(@ColorInt int color) {
        return new ColorDrawable(color);
    }

    @SuppressLint("NewApi")
    public static Drawable getSelectableBackgroundCompat(@ColorInt int normalColor, @ColorInt int pressedColor, @ColorInt int rippleColor) {
        return (Utils.hasLollipop()?new RippleDrawable(ColorStateList.valueOf(rippleColor), getStateListDrawable(normalColor, pressedColor), getRippleMask(normalColor)):getStateListDrawableLegacy(normalColor, pressedColor));
    }

    private static Drawable getRippleMask(@ColorInt int color) {
        float[] outerRadii = new float[8];
        Arrays.fill(outerRadii, 3.0F);
        RoundRectShape r = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private static StateListDrawable getStateListDrawableLegacy(@ColorInt int normalColor, @ColorInt int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{16843518}, getColorDrawable(pressedColor));
        states.addState(new int[]{16842919}, getColorDrawable(pressedColor));
        states.addState(new int[0], getColorDrawable(normalColor));
        if(!Utils.hasLollipop() || Utils.hasNougat()) {
            short duration = 200;
            states.setEnterFadeDuration(duration);
            states.setExitFadeDuration(duration);
        }

        return states;
    }

}