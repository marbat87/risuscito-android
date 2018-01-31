package it.cammino.risuscito.items;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.R;

public class SimpleItem extends AbstractItem<SimpleItem, SimpleItem.ViewHolder> {

  private StringHolder title;
  private StringHolder page;
  private StringHolder source;
  private ColorHolder color;
  private int numSalmo;
  private ColorHolder selectedColor;
  private String normalizedTitle;
  private String filter;
  private int id;

  private View.OnCreateContextMenuListener createContextMenuListener;

  public SimpleItem withTitle(String title) {
    this.title = new StringHolder(title);
    return this;
  }

  public SimpleItem withTitle(@StringRes int titleRes) {
    this.title = new StringHolder(titleRes);
    return this;
  }

  public SimpleItem withPage(String page) {
    this.page = new StringHolder(page);
    return this;
  }

  public SimpleItem withPage(@StringRes int pageRes) {
    this.page = new StringHolder(pageRes);
    return this;
  }

  public SimpleItem withSource(String src) {
    this.source = new StringHolder(src);
    return this;
  }

  public SimpleItem withSource(@StringRes int srcRes) {
    this.source = new StringHolder(srcRes);
    return this;
  }

  public SimpleItem withColor(String color) {
    this.color = ColorHolder.fromColor(Color.parseColor(color));
    return this;
  }

  public SimpleItem withColor(@ColorRes int colorRes) {
    this.color = ColorHolder.fromColorRes(colorRes);
    return this;
  }

  public SimpleItem withSelectedColor(String selectedColor) {
    this.selectedColor = ColorHolder.fromColor(Color.parseColor(selectedColor));
    return this;
  }

  public SimpleItem withSelectedColor(@ColorInt int selectedColor) {
    this.selectedColor = ColorHolder.fromColor(selectedColor);
    return this;
  }

  public SimpleItem withSelectedColorRes(@ColorRes int selectedColorRes) {
    this.selectedColor = ColorHolder.fromColorRes(selectedColorRes);
    return this;
  }

  public SimpleItem withNormalizedTitle(String normTitle) {
    this.normalizedTitle = normTitle;
    return this;
  }

  public SimpleItem withFilter(String filter) {
    this.filter = filter;
    return this;
  }

  public SimpleItem withId(int id) {
    this.id = id;
    super.withIdentifier(id);
    return this;
  }

  public SimpleItem withNumSalmo(String numSalmo) {
    int numeroTemp = 0;
    try {
      numeroTemp = Integer.valueOf(numSalmo.substring(0, 3));
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      Log.e(getClass().getName(), e.getLocalizedMessage(), e);
    }
    this.numSalmo = numeroTemp;
    return this;
  }

  public SimpleItem withContextMenuListener(View.OnCreateContextMenuListener listener) {
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

  @Override
  public long getIdentifier() {
    return id;
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
    return R.id.fastadapter_simple_item_id;
  }

  /**
   * defines the layout which will be used for this item in the list
   *
   * @return the layout for this item
   */
  @Override
  public int getLayoutRes() {
    return R.layout.simple_row_item;
  }

  /**
   * binds the data of this item onto the viewHolder
   *
   * @param viewHolder the viewHolder of this item
   */
  @Override
  public void bindView(ViewHolder viewHolder, List<Object> payloads) {
    super.bindView(viewHolder, payloads);

    // get the context
    Context ctx = viewHolder.itemView.getContext();

    // set the text for the name
    if (filter != null && !filter.isEmpty()) {
      int mPosition = normalizedTitle.indexOf(filter);
      if (mPosition >= 0) {
        String highlighted =
            title
                .getText()
                .toString()
                .replaceAll(
                    "(?i)("
                        + title
                            .getText()
                            .toString()
                            .substring(mPosition, mPosition + filter.length())
                        + ")",
                    "<b>$1</b>");
        viewHolder.mTitle.setText(LUtils.fromHtmlWrapper(highlighted));
      } else StringHolder.applyTo(title, viewHolder.mTitle);
    } else StringHolder.applyTo(title, viewHolder.mTitle);
    // set the text for the description or hide
    StringHolder.applyToOrHide(page, viewHolder.mPage);
    //        Drawable drawable = FastAdapterUIUtils.getRippleDrawable(Color.WHITE,
    // ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color), 10);
    //        UIUtils.setBackground(viewHolder.view, drawable);
    //        UIUtils.setBackground(viewHolder.view, FastAdapterUIUtils.getSelectableBackground(ctx,
    // ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color), true));
    ViewCompat.setBackground(
        viewHolder.view,
        FastAdapterUIUtils.getSelectableBackground(
            ctx,
            ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color),
            true));

    if (isSelected()) {
      viewHolder.mPage.setVisibility(View.INVISIBLE);
      viewHolder.mPageSelected.setVisibility(View.VISIBLE);
      GradientDrawable bgShape = (GradientDrawable) viewHolder.mPageSelected.getBackground();
      bgShape.setColor(selectedColor.getColorInt());
    } else {
      GradientDrawable bgShape = (GradientDrawable) viewHolder.mPage.getBackground();
      bgShape.setColor(color.getColorInt());
      viewHolder.mPage.setVisibility(View.VISIBLE);
      viewHolder.mPageSelected.setVisibility(View.INVISIBLE);
    }

    viewHolder.mId.setText(String.valueOf(id));

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

  @NonNull
  @Override
  public ViewHolder getViewHolder(View v) {
    return new ViewHolder(v);
  }

  /** our ViewHolder */
  protected static class ViewHolder extends RecyclerView.ViewHolder {
    protected View view;

    @BindView(R.id.text_title)
    TextView mTitle;

    @BindView(R.id.text_page)
    TextView mPage;

    @BindView(R.id.selected_mark)
    View mPageSelected;

    @BindView(R.id.text_id_canto)
    TextView mId;

    public ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
      this.view = view;
    }
  }
}
