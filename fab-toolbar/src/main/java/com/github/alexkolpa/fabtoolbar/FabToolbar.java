package com.github.alexkolpa.fabtoolbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

public class FabToolbar extends RevealFrameLayout {

	private static final int DEFAULT_ANIMATION_DURATION = 500;

	private LinearLayout container;
	private FloatingActionButton button;
	private float screenWidth;
	private int animationDuration = DEFAULT_ANIMATION_DURATION;
	private OnClickListener clickListener;
	private boolean mVisible;
	private final Interpolator mInterpolator;

	public FabToolbar(Context context) {
		super(context);
		this.mInterpolator = new AccelerateDecelerateInterpolator();
		init();
	}

	public FabToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mInterpolator = new AccelerateDecelerateInterpolator();
		init();
		loadAttributes(attrs);
	}

	public FabToolbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mInterpolator = new AccelerateDecelerateInterpolator();
		init();
		loadAttributes(attrs);
	}

	private void init() {
		this.mVisible = true;
		screenWidth = getResources().getDisplayMetrics().widthPixels;

		inflate(getContext(), R.layout.fab_toolbar, this);
		button = (FloatingActionButton) findViewById(R.id.button);
		button.setOnClickListener(new ButtonClickListener());
		container = ((LinearLayout) findViewById(R.id.container));
	}

	private void loadAttributes(AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.FabToolbar,
				0, 0);

		int containerGravity;
		int buttonGravity;
		try {
			setColor(a.getColor(R.styleable.FabToolbar_tb_color, getResources().getColor(R.color.blue)));
			animationDuration = a.getInteger(R.styleable.FabToolbar_tb_anim_duration, DEFAULT_ANIMATION_DURATION);
			containerGravity = a.getInteger(R.styleable.FabToolbar_tb_container_gravity, 1);
			buttonGravity = a.getInteger(R.styleable.FabToolbar_tb_button_gravity, 2);

		}
		finally {
			a.recycle();
		}

		container.setGravity(getGravity(containerGravity));

		FrameLayout.LayoutParams buttonParams = (LayoutParams) button.getLayoutParams();
		buttonParams.gravity = getGravity(buttonGravity);
	}

	private int getGravity(int gravityEnum) {
		return (gravityEnum == 0 ? Gravity.START : gravityEnum == 1 ? Gravity.CENTER_HORIZONTAL : Gravity.END)
				| Gravity.CENTER_VERTICAL;
	}

	public void setColor(int color) {
		button.setColorNormal(color);
		button.setColorPressed(color);
		container.setBackgroundColor(color);
	}

	public void setAnimationDuration(int duration) {
		animationDuration = duration;
	}

	public void setButtonOnClickListener(OnClickListener listener) {
		clickListener = listener;
	}

	public void attachToListView(AbsListView listView) {
		button.attachToListView(listView);
	}

	public void attachToRecyclerView(RecyclerView recyclerView) {
		button.attachToRecyclerView(recyclerView);
	}

	public void setButtonIcon(Drawable drawable) {
		button.setImageDrawable(drawable);
	}

	public void setButtonIcon(int resId) {
		button.setImageResource(resId);
	}

	public void show() {
		button.setOnClickListener(null);
		container.setVisibility(VISIBLE);
		animateCircle(0, screenWidth, null);
	}

	public void hide() {
		//If button was attached to list and got hidden, closing the toolbar should still show the button
		button.show(false);
		animateCircle(screenWidth, 0, new ToolbarCollapseListener());
	}

    public boolean isVisible() {
        return (container.getVisibility() == VISIBLE);
    }

	private void animateCircle(float startRadius, float endRadius, SupportAnimator.AnimatorListener listener) {
		int cx = (button.getLeft() + button.getRight()) / 2;
		int cy = (button.getTop() + button.getBottom()) / 2;

		SupportAnimator animator =
				ViewAnimationUtils.createCircularReveal(container, cx, cy, startRadius, endRadius);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(animationDuration);
		if (listener != null) {
			animator.addListener(listener);
		}
		animator.start();
	}

	public boolean isShowing() {
		return this.mVisible;
	}

	public void scrollUp() {
		this.scrollUp(true);
	}

	public void scrollDown() {
		this.scrollDown(true);
	}

	public void scrollUp(boolean animate) {
		this.toggle(true, animate, false);
	}

	public void scrollDown(boolean animate) {
		this.toggle(false, animate, false);
	}

	private void toggle(final boolean visible, final boolean animate, boolean force) {
		if(this.mVisible != visible || force) {
			this.mVisible = visible;
			int height = this.getHeight();
			if(height == 0 && !force) {
				ViewTreeObserver translationY = this.getViewTreeObserver();
				if(translationY.isAlive()) {
					translationY.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
						public boolean onPreDraw() {
							ViewTreeObserver currentVto = FabToolbar.this.getViewTreeObserver();
							if(currentVto.isAlive()) {
								currentVto.removeOnPreDrawListener(this);
							}

							FabToolbar.this.toggle(visible, animate, true);
							return true;
						}
					});
					return;
				}
			}

			int translationY1 = visible?0:height + this.getMarginBottom();
			if(animate) {
				ViewPropertyAnimator.animate(this).setInterpolator(this.mInterpolator).setDuration(200L).translationY((float)translationY1);
			} else {
				ViewHelper.setTranslationY(this, (float) translationY1);
			}

//			if(!this.hasHoneycombApi()) {
//				this.setClickable(visible);
//			}
		}

	}

//	private boolean hasHoneycombApi() {
//		return Build.VERSION.SDK_INT >= 11;
//	}

	private int getMarginBottom() {
		int marginBottom = 0;
		ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
		if(layoutParams instanceof MarginLayoutParams) {
			marginBottom = ((MarginLayoutParams)layoutParams).bottomMargin;
		}

		return marginBottom;
	}

	@Override
	public void addView(@NonNull View child) {
		if (canAddViewToContainer(child)) {
			container.addView(child);
		}
		else {
			super.addView(child);
		}
	}

	@Override
	public void addView(@NonNull View child, int width, int height) {
		if (canAddViewToContainer(child)) {
			container.addView(child, width, height);
		}
		else {
			super.addView(child, width, height);
		}
	}

	@Override
	public void addView(@NonNull View child, ViewGroup.LayoutParams params) {
		if (canAddViewToContainer(child)) {
			container.addView(child, params);
		}
		else {
			super.addView(child, params);
		}
	}

	@Override
	public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
		if (canAddViewToContainer(child)) {
			container.addView(child, index, params);
		}
		else {
			super.addView(child, index, params);
		}
	}

	private boolean canAddViewToContainer(View child) {
		return container != null && !(child instanceof FloatingActionButton);
	}

	private class ButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			show();

			if (clickListener != null) {
				clickListener.onClick(v);
			}
		}
	}

	private class ToolbarCollapseListener implements SupportAnimator.AnimatorListener {
		@Override
		public void onAnimationEnd() {
			container.setVisibility(GONE);
			button.setOnClickListener(new ButtonClickListener());
		}

		@Override public void onAnimationStart() {}

		@Override public void onAnimationCancel() {}

		@Override public void onAnimationRepeat() {}
	}

	public static class Behavior extends android.support.design.widget.CoordinatorLayout.Behavior<FabToolbar> {
		private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
		private Rect mTmpRect;
		private boolean mIsAnimatingOut;
		private float mTranslationY;

		public Behavior() {
		}

		public boolean layoutDependsOn(CoordinatorLayout parent, FabToolbar child, View dependency) {
			return (SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout)
					|| dependency instanceof AppBarLayout;
		}

		public boolean onDependentViewChanged(CoordinatorLayout parent, FabToolbar child, View dependency) {
//            Log.i(getClass().toString(), "ENTRO");
			if(dependency instanceof Snackbar.SnackbarLayout) {
				this.updateFabTranslationForSnackbar(parent, child, dependency);
			} else if(dependency instanceof AppBarLayout) {
//                AppBarLayout appBarLayout = (AppBarLayout)dependency;
				if(this.mTmpRect == null) {
					this.mTmpRect = new Rect();
				}
//                Rect rect = this.mTmpRect;
				int rect_bottom = this.mTmpRect.bottom;
//                Log.i(getClass().toString(), "this.mTmpRect prima: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom prima: " + rect_bottom);
				ButtonGroupUtils.getDescendantRect(parent, dependency, this.mTmpRect);
//                Log.i(getClass().toString(), "this.mTmpRect dopo: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom dopo: " + rect_bottom);
//                int topInset = this.mLastInsets != null?this.mLastInsets.getSystemWindowInsetTop():0;
//                int topInset = 0;
//                int result;
//                int minHeight = ViewCompat.getMinimumHeight(appBarLayout);
//                Log.i(getClass().toString(), "minHeight: " + minHeight);
//                if(minHeight != 0) {
//                    result = minHeight * 2 + topInset;
//                } else {
//                    int childCount = appBarLayout.getChildCount();
//                    result = childCount >= 1?ViewCompat.getMinimumHeight(appBarLayout.getChildAt(childCount - 1)) * 2 + topInset:0;
//                }
//                if(rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
//                    if(!this.mIsAnimatingOut && child.getVisibility() == VISIBLE) {
//                        this.animateOut(child);
//                    }
//                } else if(child.getVisibility() != VISIBLE) {
//                    this.animateIn(child);
//                }
				if(rect_bottom > mTmpRect.bottom) {
					if(child.isShowing()) {
						child.scrollDown();
					}
				} else if(!child.isShowing()) {
					child.scrollUp();
				}
			}

			return false;
		}

		private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FabToolbar fab, View snackbar) {
			float translationY = this.getFabTranslationYForSnackbar(parent, fab);
			if(translationY != this.mTranslationY) {
				ViewCompat.animate(fab).cancel();
				if(Math.abs(translationY - this.mTranslationY) == (float)snackbar.getHeight()) {
					ViewCompat.animate(fab).translationY(translationY).setInterpolator(new FastOutSlowInInterpolator()).setListener((ViewPropertyAnimatorListener)null);
				} else {
					ViewCompat.setTranslationY(fab, translationY);
				}

				this.mTranslationY = translationY;
			}

		}

		private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FabToolbar fab) {
			float minOffset = 0.0F;
			List dependencies = parent.getDependencies(fab);
			int i = 0;

			for(int z = dependencies.size(); i < z; ++i) {
				View view = (View)dependencies.get(i);
				if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
					minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
				}
			}

			return minOffset;
		}

		private void animateIn(FabToolbar button) {
			button.setVisibility(VISIBLE);
			if(Build.VERSION.SDK_INT >= 14) {
				ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener((ViewPropertyAnimatorListener)null).start();
			} else {
				Animation anim = android.view.animation.AnimationUtils.loadAnimation(button.getContext(), R.anim.fab_in);
				anim.setDuration(200L);
				anim.setInterpolator(new FastOutSlowInInterpolator());
				button.startAnimation(anim);
			}

		}

		private void animateOut(final FabToolbar button) {
			if(Build.VERSION.SDK_INT >= 14) {
				ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener(new ViewPropertyAnimatorListener() {
					public void onAnimationStart(View view) {
						Behavior.this.mIsAnimatingOut = true;
					}

					public void onAnimationCancel(View view) {
						Behavior.this.mIsAnimatingOut = false;
					}

					public void onAnimationEnd(View view) {
						Behavior.this.mIsAnimatingOut = false;
						view.setVisibility(GONE);
					}
				}).start();
			} else {
				Animation anim = android.view.animation.AnimationUtils.loadAnimation(button.getContext(), R.anim.fab_out);
				anim.setInterpolator(new FastOutSlowInInterpolator());
				anim.setDuration(200L);
				anim.setAnimationListener(new AnimationListenerAdapter() {
					public void onAnimationStart(Animation animation) {
						Behavior.this.mIsAnimatingOut = true;
					}

					public void onAnimationEnd(Animation animation) {
						Behavior.this.mIsAnimatingOut = false;
						button.setVisibility(GONE);
					}
				});
				button.startAnimation(anim);
			}

		}

		static {
			SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
		}
	}

	static class AnimationListenerAdapter implements Animation.AnimationListener {
		AnimationListenerAdapter() {
		}

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}
}
