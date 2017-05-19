package it.cammino.risuscito.items;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.R;

public class InsertItem extends AbstractItem<InsertItem, InsertItem.ViewHolder> {

    private StringHolder title;
    private StringHolder page;
    private StringHolder source;
    private ColorHolder color;
    private int numSalmo;
    private String normalizedTitle;
    private String filter;
    private int id;

    public InsertItem withTitle(String title) {
        this.title = new StringHolder(title);
        return this;
    }

    public InsertItem withTitle(@StringRes int titleRes) {
        this.title = new StringHolder(titleRes);
        return this;
    }

    public InsertItem withPage(String page) {
        this.page = new StringHolder(page);
        return this;
    }

    public InsertItem withPage(@StringRes int pageRes) {
        this.page = new StringHolder(pageRes);
        return this;
    }

    public InsertItem withSource(String src) {
        this.source = new StringHolder(src);
        return this;
    }

    public InsertItem withSource(@StringRes int srcRes) {
        this.source = new StringHolder(srcRes);
        return this;
    }

    public InsertItem withColor(String color) {
        this.color = ColorHolder.fromColor(Color.parseColor(color));
        return this;
    }

    public InsertItem withColor(@ColorRes int colorRes) {
        this.color = ColorHolder.fromColorRes(colorRes);
        return this;
    }

    public InsertItem withId(int id) {
        this.id = id;
        super.withIdentifier(id);
        return this;
    }

    public InsertItem withNumSalmo(String numSalmo) {
        int numeroTemp = 0;
        try {
            numeroTemp = Integer.valueOf(numSalmo.substring(0, 3));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            Log.e(getClass().getName(), e.getLocalizedMessage(), e);
        }
        this.numSalmo = numeroTemp;
        return this;
    }

    public InsertItem withNormalizedTitle(String normTitle) {
        this.normalizedTitle = normTitle;
        return this;
    }

    public InsertItem withFilter(String filter) {
        this.filter = filter;
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

    public int getNumSalmo() {
        return numSalmo;
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    @Override
    public int getType() {
        return R.id.fastadapter_insert_item_id;
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    @Override
    public int getLayoutRes() {
        return R.layout.row_item_to_insert;
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        //set the text for the name
        if (filter != null && !filter.isEmpty()) {
            int mPosition = normalizedTitle.indexOf(filter);
            if (mPosition >= 0) {
                String highlighted = title.getText().replaceAll("(?i)(" + title.getText().substring(mPosition, mPosition + filter.length()) + ")", "<b>$1</b>");
                viewHolder.mTitle.setText(LUtils.fromHtmlWrapper(highlighted));
            }
            else
                StringHolder.applyTo(title, viewHolder.mTitle);
        }
        else
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
    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View view;
        @BindView(R.id.text_title) TextView mTitle;
        @BindView(R.id.text_page) TextView mPage;
        public @BindView(R.id.preview) View mPreview;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }

}