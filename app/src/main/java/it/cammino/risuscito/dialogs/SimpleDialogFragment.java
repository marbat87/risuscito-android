package it.cammino.risuscito.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.Serializable;

@SuppressWarnings("unused")
public class SimpleDialogFragment extends DialogFragment {

    protected SimpleCallback mCallback;

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain thi
        // s fragment
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder mBuilder = getBuilder();
        if (mBuilder == null)
            throw new IllegalStateException("SimpleDialogFragment should be created using its Builder interface.");

        if (mCallback == null)
            mCallback = mBuilder.mListener;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getActivity())
                .autoDismiss(mBuilder.mAutoDismiss);

        if (mBuilder.mShowProgress)
            dialogBuilder.progress(mBuilder.mProgressIndeterminate, mBuilder.mProgressMax, false);

        if (mBuilder.mTitle != 0)
            dialogBuilder.title(mBuilder.mTitle);

        if (mBuilder.mContent != null)
            dialogBuilder.content(mBuilder.mContent);

        if (mBuilder.mPositiveButton != null) {
            dialogBuilder.positiveText(mBuilder.mPositiveButton)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onPositive(mBuilder.mTag);
                        }
                    });
        }

        if (mBuilder.mNegativeButton != null) {
            dialogBuilder.negativeText(mBuilder.mNegativeButton)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onNegative(mBuilder.mTag);
                        }
                    });
        }

        if (mBuilder.mNeutralButton != null) {
            dialogBuilder.neutralText(mBuilder.mNeutralButton)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onNeutral(
                                    mBuilder.mTag);
                        }
                    });
        }

        if (mBuilder.mCustomView != 0) {
            dialogBuilder.customView(mBuilder.mCustomView, false);
        }

        MaterialDialog dialog = dialogBuilder.build();

        dialog.setCancelable(mBuilder.mCanceable);

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    arg0.cancel();
                    return true;
                }
                return false;
            }
        });

        return dialog;
    }

    public void setContent(@StringRes int res) {
        ((MaterialDialog) getDialog()).setContent(res);
    }

    public void setmCallback(@NonNull SimpleCallback callback) {
        mCallback = callback;
    }

    public void setProgress(int progress) {
        ((MaterialDialog) getDialog()).setProgress(progress);
    }

    public void cancel() {
        getDialog().cancel();
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        getDialog().setOnCancelListener(listener);
    }

    @Override
    public void dismiss() {
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Builder mBuilder = getBuilder();
        if(mBuilder != null && mBuilder.mCanceListener)
            mCallback.onPositive(mBuilder.mTag);
    }

    public static class Builder implements Serializable {

        @NonNull
        final transient AppCompatActivity mContext;
        int mTitle = 0;
        boolean mShowProgress = false;
        boolean mProgressIndeterminate;
        int mProgressMax;
        @Nullable
        CharSequence mContent;
        @NonNull
        final String mTag;
        CharSequence mPositiveButton;
        CharSequence mNegativeButton;
        CharSequence mNeutralButton;
        boolean mCanceable = false;
        boolean mAutoDismiss = true;
        boolean mCanceListener = false;
        int mCustomView = 0;
        @NonNull
        transient SimpleCallback mListener;

        public <ActivityType extends AppCompatActivity> Builder(@NonNull ActivityType context, @NonNull SimpleCallback listener, @NonNull String tag) {
            mContext = context;
            mTag = tag;
            mListener = listener;
        }

        @NonNull
        public Builder title(@StringRes int text) {
            mTitle = text;
            return this;
        }

        @NonNull
        public Builder content(@StringRes int content) {
            mContent = this.mContext.getResources().getText(content);
            return this;
        }

        @NonNull
        public Builder content(@NonNull String content) {
            mContent = content;
            return this;
        }

        @NonNull
        public Builder showProgress() {
            mShowProgress = true;
            return this;
        }

        @NonNull
        public Builder progressIndeterminate(boolean progressIndeterminate) {
            mProgressIndeterminate = progressIndeterminate;
            return this;
        }

        @NonNull
        public Builder progressMax(int progressMax) {
            mProgressMax = progressMax;
            return this;
        }

        @NonNull
        public Builder positiveButton(@StringRes int text) {
            mPositiveButton = this.mContext.getResources().getText(text);
            return this;
        }

        @NonNull
        public Builder negativeButton(@StringRes int text) {
            mNegativeButton = this.mContext.getResources().getText(text);
            return this;
        }
        @NonNull
        public Builder neutralButton(@StringRes int text) {
            mNeutralButton = this.mContext.getResources().getText(text);
            return this;
        }

        @NonNull
        public Builder setHasCancelListener() {
            mCanceListener = true;
            return this;
        }

        @NonNull
        public Builder setCanceable() {
            mCanceable = true;
            return this;
        }

        @NonNull
        public Builder setAutoDismiss(boolean autoDismiss) {
            mAutoDismiss = autoDismiss;
            return this;
        }

        @NonNull
        public Builder setCustomView(@LayoutRes int customView) {
            mCustomView = customView;
            return this;
        }

        @NonNull
        public SimpleDialogFragment build() {
            SimpleDialogFragment dialog = new SimpleDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable("builder", this);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        public SimpleDialogFragment show() {
            SimpleDialogFragment dialog = build();
            dialog.show(mContext);
            return dialog;
        }
    }

    private Builder getBuilder() {
        if (getArguments() == null || !getArguments().containsKey("builder")) return null;
        return (Builder) getArguments().getSerializable("builder");
    }

    @Nullable
    public static SimpleDialogFragment findVisible(@NonNull AppCompatActivity context, String tag) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(tag);
        if (frag != null && frag instanceof SimpleDialogFragment)
            return (SimpleDialogFragment) frag;
        return null;
    }

    private void dismissIfNecessary(AppCompatActivity context, String tag) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(tag);
        if (frag != null) {
            ((DialogFragment) frag).dismiss();
            context.getSupportFragmentManager().beginTransaction()
                    .remove(frag).commit();
        }
    }

    @NonNull
    public SimpleDialogFragment show(AppCompatActivity context) {
        Builder builder = getBuilder();
        if (builder != null) {
            dismissIfNecessary(context, builder.mTag);
            show(context.getSupportFragmentManager(), builder.mTag);
        }
        return this;
    }

    public interface SimpleCallback {
        void onPositive(@NonNull String tag);
        void onNegative(@NonNull String tag);
        void onNeutral(@NonNull String tag);
    }

}
