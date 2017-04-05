package it.cammino.risuscito.items;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;
import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class CantoItem extends AbstractFlexibleItem<CantoItem.ChildViewHolder>
        implements ISectionable<CantoItem.ChildViewHolder, IHeader>, IFilterable {

    protected String id;
    protected String title;
    protected String page;
    protected String source;
    protected String color;
    protected int cantoId;
    protected int numeroSalmo;
    protected int activeColor;
    protected String subtitle = "";

    /**
     * The header of this item
     */
    IHeader header;

    public CantoItem(String id, String titolo) {
        this.id = id;
        this.title = titolo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCantoId() {
        return cantoId;
    }

    public void setCantoId(int cantoId) {
        this.cantoId = cantoId;
    }

    public int getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
    }

    public int getNumeroSalmo() {
        return numeroSalmo;
    }

    public void setNumeroSalmo(int numeroSalmo) {
        this.numeroSalmo = numeroSalmo;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof CantoItem) {
            CantoItem inItem = (CantoItem) inObject;
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
    public IHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(IHeader header) {
        this.header = header;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.simple_row_item;
    }

    @Override
    public ChildViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ChildViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ChildViewHolder holder, int position, List payloads) {
        //In case of searchText matches with Title or with an SimpleItem's field
        // this will be highlighted
        if (adapter.hasSearchText()) {
            Context context = holder.itemView.getContext();
            Utils.highlightText(context, holder.mTitle, getTitle(), adapter.getSearchText(), getActiveColor());
        } else {
            holder.mTitle.setText(getTitle());
        }

        if (getHeader() != null) {
            setSubtitle("Header " + getHeader().toString());
        }
        holder.mPage.setText(getPage());
        GradientDrawable bgShape = (GradientDrawable)holder.mPage.getBackground();
        bgShape.setColor(Color.parseColor(getColor()));
//        holder.mPage.setBackgroundResource(
//                holder.itemView.getContext().getResources().getIdentifier("page_oval_border_bkg_" + getColor().substring(1).toLowerCase()
//                        , "drawable"
//                        , holder.itemView.getContext().getPackageName()));
    }

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint);
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static final class ChildViewHolder extends FlexibleViewHolder {

        TextView mTitle;
        TextView mPage;
//        View mPageContainer;


        public ChildViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mTitle = (TextView) view.findViewById(R.id.text_title);
            this.mPage = (TextView) view.findViewById(R.id.text_page);
//            this.mPageContainer = view.findViewById(R.id.page_container);
        }

        @Override
        public float getActivationElevation() {
            return Utility.dpToPx(itemView.getContext(), 4f);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0f);
        }
    }

    @Override
    public String toString() {
        return "SubItem[id=" + this.id + ", title=" + getTitle() + "]";
    }

}