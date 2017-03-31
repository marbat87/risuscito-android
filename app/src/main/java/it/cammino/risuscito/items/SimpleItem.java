package it.cammino.risuscito.items;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;
import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;

public class SimpleItem extends AbstractFlexibleItem<SimpleItem.SimpleViewHolder>
		implements IFilterable, Serializable {

	protected String id;
	protected  String titolo;
//	protected int idPosizione;

	public SimpleItem(String id, String titolo) {
		this.id = id;
		this.titolo = titolo;
		setDraggable(true);
		setSwipeable(true);
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

//	public int getIdPosizione() {
//		return idPosizione;
//	}
//
//	public void setIdPosizione(int idPosizione) {
//		this.idPosizione = idPosizione;
//	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SimpleItem) {
			SimpleItem inItem = (SimpleItem) o;
			return this.id.equals(inItem.id);
		}
		return false;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.position_list_item_light;
	}

	@Override
	public SimpleViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new SimpleViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public void bindViewHolder(final FlexibleAdapter adapter, SimpleViewHolder holder, int position, List payloads) {
		Context context = holder.itemView.getContext();

		// Background, when bound the first time
		if (payloads.size() == 0) {
			Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
					Color.WHITE, Color.parseColor("#dddddd"), //Same color of divider
					DrawableUtils.getColorControlHighlight(context));
			DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
			DrawableUtils.setBackgroundCompat(holder.frontView, drawable);
		}

		// In case of searchText matches with Title or with a field this will be highlighted
		if (adapter.hasSearchText()) {
			Utils.highlightText(holder.mTitle, getTitolo(), adapter.getSearchText());
		} else {
			holder.mTitle.setText(getTitolo());
		}
	}

	@Override
	public boolean filter(String constraint) {
		return getTitolo() != null && getTitolo().toLowerCase().trim().contains(constraint);
	}

	static final class SimpleViewHolder extends FlexibleViewHolder {

		TextView mTitle;
		View mHandleView;
		Context mContext;
		View frontView;
		View rearLeftView;
		View rearRightView;

		public boolean swiped = false;

		SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            Log.d("SimpleViewHolder", "SimpleViewHolder: ");
			this.mContext = view.getContext();
			this.mTitle = (TextView) view.findViewById(android.R.id.text1);
			this.mHandleView = view.findViewById(R.id.drag_image);
			setDragHandleView(mHandleView);

			this.frontView = view.findViewById(R.id.container);
			this.rearLeftView = view.findViewById(R.id.rear_left_view);
			this.rearRightView = view.findViewById(R.id.rear_right_view);
		}

		@Override
		protected void setDragHandleView(@NonNull View view) {
			if (mAdapter.isHandleDragEnabled()) {
				view.setVisibility(View.VISIBLE);
				super.setDragHandleView(view);
			} else {
				view.setVisibility(View.GONE);
			}
		}

//		@Override
//		public void onClick(View view) {
//			Toast.makeText(mContext, "Click on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
//			super.onClick(view);
//		}
////
//		@Override
//		public boolean onLongClick(View view) {
//			Toast.makeText(mContext, "LongClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
//			return super.onLongClick(view);
//		}
////
//		@Override
//		public void toggleActivation() {
//			super.toggleActivation();
//			// Here we use a custom Animation inside the ItemView
////			mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
//		}

		@Override
		public float getActivationElevation() {
			return Utility.dpToPx(itemView.getContext(), 4f);
		}

		@Override
		protected boolean shouldActivateViewWhileSwiping() {
			return false;//default=false
		}

		@Override
		protected boolean shouldAddSelectionInActionMode() {
			return false;//default=false
		}

		@Override
		public View getFrontView() {
			return frontView;
		}

		@Override
		public View getRearLeftView() {
			return rearLeftView;
		}

		@Override
		public View getRearRightView() {
			return rearRightView;
		}

		@Override
		public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
			if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager ||
					mAdapter.getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager) {
				if (position % 2 != 0)
					AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
				else
					AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
			} else {
				//Linear layout
				if (mAdapter.isSelected(position))
					AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
				else
					AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
			}
		}

		@Override
		public void onItemReleased(int position) {
			swiped = (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE);
			super.onItemReleased(position);
		}
	}

	@Override
	public String toString() {
		return "SimpleItem[title=" + getTitolo() + "]";
	}

}