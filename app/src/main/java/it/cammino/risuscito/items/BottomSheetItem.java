package it.cammino.risuscito.items;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.holder.StringHolder;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;

public class BottomSheetItem extends AbstractItem<BottomSheetItem, BottomSheetItem.ViewHolder> {

    private ResolveInfo item;

    public BottomSheetItem withItem(ResolveInfo item) {
        this.item = item;
        return  this;
    }

    public ResolveInfo getItem() {
        return item;
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    @Override
    public int getType() {
        return R.id.fastadapter_bottom_item_id;
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    @Override
    public int getLayoutRes() {
        return R.layout.bottom_item;
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        PackageManager pm = viewHolder.itemView.getContext().getPackageManager();
        viewHolder.mIcon.setImageDrawable(item.loadIcon(pm));
        viewHolder.mLabel.setText(item.loadLabel(pm));

    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.mLabel.setText(null);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    /**
     * our ViewHolder
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected View view;
        @BindView(R.id.app_icon) ImageView mIcon;
        @BindView(R.id.app_label) TextView mLabel;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}