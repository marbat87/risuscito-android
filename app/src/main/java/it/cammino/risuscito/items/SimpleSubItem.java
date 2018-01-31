package it.cammino.risuscito.items;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISubItem;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;

public class SimpleSubItem<Parent extends IItem & IExpandable & ISubItem & IClickable>
    extends AbstractExpandableItem<Parent, SimpleSubItem.ViewHolder, SimpleSubItem<Parent>> {

  private StringHolder title;
  private StringHolder page;
  private StringHolder source;
  private ColorHolder color;
  private int id;
  private boolean hasDivider = false;

  private View.OnCreateContextMenuListener createContextMenuListener;

  public SimpleSubItem<Parent> withTitle(String title) {
    this.title = new StringHolder(title);
    return this;
  }

  public SimpleSubItem<Parent> withTitle(@StringRes int titleRes) {
    this.title = new StringHolder(titleRes);
    return this;
  }

  public SimpleSubItem<Parent> withPage(String page) {
    this.page = new StringHolder(page);
    return this;
  }

  public SimpleSubItem<Parent> withPage(@StringRes int pageRes) {
    this.page = new StringHolder(pageRes);
    return this;
  }

  public SimpleSubItem<Parent> withSource(String src) {
    this.source = new StringHolder(src);
    return this;
  }

  public SimpleSubItem<Parent> withSource(@StringRes int srcRes) {
    this.source = new StringHolder(srcRes);
    return this;
  }

  public SimpleSubItem<Parent> withColor(String color) {
    this.color = ColorHolder.fromColor(Color.parseColor(color));
    return this;
  }

  public SimpleSubItem<Parent> withColor(@ColorRes int colorRes) {
    this.color = ColorHolder.fromColorRes(colorRes);
    return this;
  }

  public SimpleSubItem<Parent> withId(int id) {
    this.id = id;
    return this;
  }

  public SimpleSubItem<Parent> withContextMenuListener(View.OnCreateContextMenuListener listener) {
    this.createContextMenuListener = listener;
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public SimpleSubItem<Parent> withHasDivider(boolean hasDivider) {
    this.hasDivider = hasDivider;
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

  @SuppressWarnings("unused")
  public boolean isHasDivider() {
    return hasDivider;
  }

  /**
   * defines the type defining this item. must be unique. preferably an id
   *
   * @return the type
   */
  @Override
  public int getType() {
    return R.id.fastadapter_sub_item_id;
  }

  /**
   * defines the layout which will be used for this item in the list
   *
   * @return the layout for this item
   */
  @Override
  public int getLayoutRes() {
    return R.layout.simple_sub_item;
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
    StringHolder.applyTo(title, viewHolder.mTitle);
    // set the text for the description or hide
    StringHolder.applyToOrHide(page, viewHolder.mPage);

    ViewCompat.setBackground(
        viewHolder.view,
        FastAdapterUIUtils.getSelectableBackground(
            ctx,
            ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color),
            true));

    if (isSelected()) {
      viewHolder.mPage.setVisibility(View.INVISIBLE);
      viewHolder.mPageSelected.setVisibility(View.VISIBLE);
    } else {
      GradientDrawable bgShape = (GradientDrawable) viewHolder.mPage.getBackground();
      bgShape.setColor(color.getColorInt());
      viewHolder.mPage.setVisibility(View.VISIBLE);
      viewHolder.mPageSelected.setVisibility(View.INVISIBLE);
    }

    viewHolder.mId.setText(String.valueOf(id));

    viewHolder.mItemDivider.setVisibility(hasDivider ? View.VISIBLE : View.INVISIBLE);

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

    @BindView(R.id.item_divider)
    View mItemDivider;

    public ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
      this.view = view;
    }
  }
}
