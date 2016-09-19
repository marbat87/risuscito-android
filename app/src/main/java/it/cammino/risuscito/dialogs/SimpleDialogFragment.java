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

public class SimpleDialogFragment extends DialogFragment {

    protected SimpleCallback mCallback;

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        if (!(activity instanceof SimpleCallback))
//            throw new IllegalStateException("SimpleDialogFragment needs to be shown from an Activity implementing SimpleCallback.");
//        mCallback = (SimpleCallback) activity;
//    }

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
        if (getArguments() == null || !getArguments().containsKey("builder"))
            throw new IllegalStateException("SimpleDialogFragment should be created using its Builder interface.");

        if (mCallback == null)
            mCallback = getBuilder().mListener;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getActivity())
//                .title(getBuilder().mTitle)
                .autoDismiss(getBuilder().mAutoDismiss);
//                .content(getBuilder().mContent);

        if (getBuilder().mShowProgress)
            dialogBuilder.progress(getBuilder().mProgressIndeterminate, getBuilder().mProgressMax, false);

        if (getBuilder().mTitle != 0)
            dialogBuilder.title(getBuilder().mTitle);

        if (getBuilder().mContent != null)
            dialogBuilder.content(getBuilder().mContent);

        if (getBuilder().mPositiveButton != null) {
            dialogBuilder.positiveText(getBuilder().mPositiveButton)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onPositive(getBuilder().mTag);
                        }
                    });
        }

        if (getBuilder().mNegativeButton != null) {
            dialogBuilder.negativeText(getBuilder().mNegativeButton)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onNegative(getBuilder().mTag);
                        }
                    });
        }

        if (getBuilder().mNeutralButton != null) {
            dialogBuilder.neutralText(getBuilder().mNeutralButton)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onNeutral(
                                    getBuilder().mTag);
                        }
                    });
        }

        if (getBuilder().mCustomView != 0) {
            dialogBuilder.customView(getBuilder().mCustomView, false);
        }

        MaterialDialog dialog = dialogBuilder.build();

        dialog.setCancelable(getBuilder().mCanceable);

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

        if (getBuilder().mCanceListener) {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mCallback.onPositive(getBuilder().mTag);
                }
            });
        }
//        dialog.setCancelable(false);

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

    public static class Builder implements Serializable {

        @NonNull
        protected final transient AppCompatActivity mContext;
        protected int mTitle = 0;
        protected boolean mShowProgress = false;
        protected boolean mProgressIndeterminate;
        protected int mProgressMax;
        @Nullable
        protected CharSequence mContent;
        @NonNull
        protected final String mTag;
        @StringRes
        protected CharSequence mPositiveButton;
        @StringRes
        protected CharSequence mNegativeButton;
        @StringRes
        protected CharSequence mNeutralButton;
        protected boolean mCanceable = false;
        protected boolean mAutoDismiss = true;
        protected boolean mCanceListener = false;
        protected int mCustomView = 0;
        @NonNull
        protected transient SimpleCallback mListener;

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
        public Builder showProgress(boolean showProgress) {
            mShowProgress = showProgress;
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
        public Builder cancelListener(boolean hasIt) {
            mCanceListener = hasIt;
            return this;
        }

        @NonNull
        public Builder setCanceable(boolean canceable) {
            mCanceable = canceable;
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
        dismissIfNecessary(context, builder.mTag);
        show(context.getSupportFragmentManager(), builder.mTag);
        return this;
    }

    public interface SimpleCallback {
        void onPositive(@NonNull String tag);
        void onNegative(@NonNull String tag);
        void onNeutral(@NonNull String tag);
    }

}
