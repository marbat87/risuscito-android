package it.cammino.risuscito.items;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISubItem;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;

public class SimpleSubExpandableItem<Parent extends IItem & IExpandable, SubItem extends IItem & ISubItem> extends AbstractExpandableItem<SimpleSubExpandableItem<Parent, SubItem>, SimpleSubExpandableItem.ViewHolder, SubItem> {

    StringHolder title;
    StringHolder subTitle;
    ColorHolder color;

    private FastAdapter.OnClickListener<SimpleSubExpandableItem> mOnClickListener;

    public SimpleSubExpandableItem<Parent, SubItem> withTitle(String title) {
        this.title = new StringHolder(title);
        return this;
    }

    public SimpleSubExpandableItem<Parent, SubItem> withTitle(@StringRes int titleRes) {
        this.title = new StringHolder(titleRes);
        return this;
    }

    public SimpleSubExpandableItem<Parent, SubItem> withSubTitle(String subTitle) {
        this.subTitle = new StringHolder(subTitle);
        return this;
    }

    public SimpleSubExpandableItem<Parent, SubItem> withSubTitle(@StringRes int subTitleRes) {
        this.subTitle = new StringHolder(subTitleRes);
        return this;
    }

    public SimpleSubExpandableItem<Parent, SubItem> withColor(@ColorInt int color) {
        this.color = ColorHolder.fromColor(color);
        return this;
    }

    public StringHolder getTitle() {
        return title;
    }

    public FastAdapter.OnClickListener<SimpleSubExpandableItem> getOnClickListener() {
        return mOnClickListener;
    }

    public SimpleSubExpandableItem<Parent, SubItem> withOnClickListener(FastAdapter.OnClickListener<SimpleSubExpandableItem> mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
        return this;
    }

    //we define a clickListener in here so we can directly animate
    final private FastAdapter.OnClickListener<SimpleSubExpandableItem<Parent, SubItem>> onClickListener = new FastAdapter.OnClickListener<SimpleSubExpandableItem<Parent, SubItem>>() {
        @Override
        public boolean onClick(View v, IAdapter adapter, SimpleSubExpandableItem item, int position) {
            if (item.getSubItems() != null) {
                if (!item.isExpanded()) {
                    ViewCompat.animate(v.findViewById(R.id.indicator)).rotation(180).start();
                } else {
                    ViewCompat.animate(v.findViewById(R.id.indicator)).rotation(0).start();
                }
                return mOnClickListener == null || mOnClickListener.onClick(v, adapter, item, position);
            }
            return mOnClickListener != null && mOnClickListener.onClick(v, adapter, item, position);
        }
    };

    /**
     * we overwrite the item specific click listener so we can automatically animate within the item
     *
     * @return
     */
    @Override
    public FastAdapter.OnClickListener<SimpleSubExpandableItem<Parent, SubItem>> getOnItemClickListener() {
        return onClickListener;
    }

    @Override
    public boolean isSelectable() {
        //this might not be true for your application
        return getSubItems() == null;
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    @Override
    public int getType() {
        return R.id.fastadapter_expandable_item_id;
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    @Override
    public int getLayoutRes() {
        return R.layout.list_group_item;
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        //get the context
        Context ctx = viewHolder.itemView.getContext();

        //set the background for the item
        Drawable drawable = FastAdapterUIUtils.getRippleDrawable(color.getColorInt(), ContextCompat.getColor(ctx, R.color.ripple_color), 10);
        UIUtils.setBackground(viewHolder.view, drawable);
        //set the text for the name
        StringHolder.applyTo(title, viewHolder.mTitle);
        StringHolder.applyToOrHide(subTitle, viewHolder.mSubTitle);

        if (isExpanded()) {
            ViewCompat.setRotation(viewHolder.mIndicator, 0);
        } else {
            ViewCompat.setRotation(viewHolder.mIndicator, 180);
        }

    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.mTitle.setText(null);
        holder.mSubTitle.setText(null);
        //make sure all animations are stopped
        holder.mIndicator.clearAnimation();
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    /**
     * our ViewHolder
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        @BindView(R.id.title) TextView mTitle;
        @BindView(R.id.subtitle) TextView mSubTitle;
        @BindView(R.id.indicator) ImageView mIndicator;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}