package it.cammino.risuscito.items;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.holder.StringHolder;
import com.mikepenz.materialize.util.UIUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;

public class SimpleHistoryItem extends AbstractItem<SimpleHistoryItem, SimpleHistoryItem.ViewHolder> {

    private StringHolder title;
    private StringHolder page;
    private StringHolder timestamp;
    private StringHolder source;
    private ColorHolder color;
    private ColorHolder selectedColor;
    private int id;

    private View.OnCreateContextMenuListener createContextMenuListener;

    public SimpleHistoryItem withTitle(String title) {
        this.title = new StringHolder(title);
        return this;
    }

    public SimpleHistoryItem withTitle(@StringRes int titleRes) {
        this.title = new StringHolder(titleRes);
        return this;
    }

    public SimpleHistoryItem withPage(String page) {
        this.page = new StringHolder(page);
        return this;
    }

    public SimpleHistoryItem withPage(@StringRes int pageRes) {
        this.page = new StringHolder(pageRes);
        return this;
    }

    public SimpleHistoryItem withTimestamp(String timestamp) {
        this.timestamp = new StringHolder(timestamp);
        return this;
    }

    public SimpleHistoryItem withTimestamp(@StringRes int timestampRes) {
        this.timestamp = new StringHolder(timestampRes);
        return this;
    }

    public SimpleHistoryItem withSource(String src) {
        this.source = new StringHolder(src);
        return this;
    }

    public SimpleHistoryItem withSource(@StringRes int srcRes) {
        this.source = new StringHolder(srcRes);
        return this;
    }

    public SimpleHistoryItem withColor(String color) {
        this.color = ColorHolder.fromColor(Color.parseColor(color));
        return this;
    }

    public SimpleHistoryItem withColor(@ColorRes int colorRes) {
        this.color = ColorHolder.fromColorRes(colorRes);
        return this;
    }

    public SimpleHistoryItem withId(int id) {
        this.id = id;
        return this;
    }

    public SimpleHistoryItem withSelectedColor(String selectedColor) {
        this.selectedColor = ColorHolder.fromColor(Color.parseColor(selectedColor));
        return this;
    }

    public SimpleHistoryItem withSelectedColor(@ColorInt int selectedColor) {
        this.selectedColor = ColorHolder.fromColor(selectedColor);
        return this;
    }

    public SimpleHistoryItem withSelectedColorRes(@ColorRes int selectedColorRes) {
        this.selectedColor = ColorHolder.fromColorRes(selectedColorRes);
        return this;
    }

    public SimpleHistoryItem withContextMenuListener(View.OnCreateContextMenuListener listener) {
        this.createContextMenuListener = listener;
        return this;
    }

    public StringHolder getTitle() {
        return title;
    }

    public StringHolder getPage() {
        return page;
    }

    public StringHolder getSource() {
        return source;
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
        return R.id.fastadapter_history_item_id;
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    @Override
    public int getLayoutRes() {
        return R.layout.row_item_history;
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @SuppressWarnings("deprecation")
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        //get the context
        Context ctx = viewHolder.itemView.getContext();

        //set the text for the name
        StringHolder.applyTo(title, viewHolder.mTitle);
        //set the text for the description or hide
        StringHolder.applyToOrHide(page, viewHolder.mPage);

//        Drawable drawable = FastAdapterUIUtils.getRippleDrawable(Color.WHITE, ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color), 10);
//        UIUtils.setBackground(viewHolder.view, drawable);
        UIUtils.setBackground(viewHolder.view, FastAdapterUIUtils.getSelectableBackground(ctx, ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color), true));

        if (isSelected()) {
            viewHolder.mPage.setVisibility(View.INVISIBLE);
            viewHolder.mPageSelected.setVisibility(View.VISIBLE);
            GradientDrawable bgShape = (GradientDrawable) viewHolder.mPageSelected.getBackground();
            bgShape.setColor(selectedColor.getColorInt());
        }
        else {
            GradientDrawable bgShape = (GradientDrawable) viewHolder.mPage.getBackground();
            bgShape.setColor(color.getColorInt());
            viewHolder.mPage.setVisibility(View.VISIBLE);
            viewHolder.mPageSelected.setVisibility(View.INVISIBLE);
        }

        viewHolder.mId.setText(String.valueOf(id));

        if (timestamp != null) {
            //FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
            DateFormat df = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT
                    , DateFormat.MEDIUM
                    , ctx.getResources().getConfiguration().locale);
            String tempTimestamp;

            if (df instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) df;
                String pattern = sdf.toPattern().replaceAll("y+", "yyyy");
                sdf.applyPattern(pattern);
                tempTimestamp = sdf.format(Timestamp.valueOf(timestamp.getText()));
            } else
                tempTimestamp = df.format(Timestamp.valueOf(timestamp.getText()));
//            viewHolder.mTimestamp.setText(ctx.getString(R.string.last_open_date, tempTimestamp));
            viewHolder.mTimestamp.setText(tempTimestamp);
            viewHolder.mTimestamp.setVisibility(View.VISIBLE);
        }
        else
            viewHolder.mTimestamp.setVisibility(View.GONE);

        if (createContextMenuListener != null) {
            ((Activity) viewHolder.itemView.getContext()).registerForContextMenu(viewHolder.itemView);
            viewHolder.itemView.setOnCreateContextMenuListener(createContextMenuListener);
        }

    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.mTitle.setText(null);
        holder.mPage.setText(null);
        holder.mId.setText(null);
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
        @BindView(R.id.selected_mark) View mPageSelected;
        @BindView(R.id.text_timestamp) TextView mTimestamp;
        @BindView(R.id.text_id_canto) TextView mId;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}