package it.cammino.risuscito.items;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;

public class CheckableItem extends AbstractItem<CheckableItem, CheckableItem.ViewHolder> {

    private StringHolder title;
    private StringHolder page;
    private ColorHolder color;
    private int id;

    public CheckableItem withTitle(String title) {
        this.title = new StringHolder(title);
        return this;
    }

    public CheckableItem withTitle(@StringRes int titleRes) {
        this.title = new StringHolder(titleRes);
        return this;
    }

    public CheckableItem withPage(String page) {
        this.page = new StringHolder(page);
        return this;
    }

    public CheckableItem withPage(@StringRes int pageRes) {
        this.page = new StringHolder(pageRes);
        return this;
    }

    public CheckableItem withColor(String color) {
        this.color = ColorHolder.fromColor(Color.parseColor(color));
        return this;
    }

    public CheckableItem withColor(@ColorRes int colorRes) {
        this.color = ColorHolder.fromColorRes(colorRes);
        return this;
    }

    public CheckableItem withId(int id) {
        this.id = id;
        super.withIdentifier(id);
        return this;
    }

    public StringHolder getTitle() {
        return title;
    }

    public StringHolder getPage() {
        return page;
    }

    public ColorHolder getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    @Override
    public int getType() {
        return R.id.fastadapter_checkable_item_id;
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    @Override
    public int getLayoutRes() {
        return R.layout.checkable_row_item;
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.checkBox.setChecked(isSelected());

        //set the text for the name
        StringHolder.applyTo(title, viewHolder.mTitle);
        //set the text for the description or hide
        StringHolder.applyToOrHide(page, viewHolder.mPage);
        GradientDrawable bgShape = (GradientDrawable) viewHolder.mPage.getBackground();
        bgShape.setColor(color.getColorInt());
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.mTitle.setText(null);
        holder.mPage.setText(null);
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
        @BindView(R.id.text_title) TextView mTitle;
        @BindView(R.id.text_page) TextView mPage;
        @BindView(R.id.check_box) CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }

    public static class CheckBoxClickEvent extends ClickEventHook<CheckableItem> {
        @Override
        public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder instanceof CheckableItem.ViewHolder) {
                return ((CheckableItem.ViewHolder) viewHolder).checkBox;
            }
            return null;
        }

        @Override
        public void onClick(View v, int position, FastAdapter<CheckableItem> fastAdapter, CheckableItem item) {
            fastAdapter.toggleSelection(position);
        }
    }

}