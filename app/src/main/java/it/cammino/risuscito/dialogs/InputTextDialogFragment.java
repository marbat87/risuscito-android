package it.cammino.risuscito.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.Serializable;

public class InputTextDialogFragment extends DialogFragment {

    protected SimpleInputCallback mCallback;

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        if (!(activity instanceof SimpleInputCallback))
//            throw new IllegalStateException("InputTextDialogFragment needs to be shown from an Activity implementing SimpleInputCallback.");
//        mCallback = (SimpleInputCallback) activity;
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
                .input("", getBuilder().mPrefill != null ? getBuilder().mPrefill : "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .autoDismiss(getBuilder().mAutoDismiss);
//                .content(getBuilder().mContent);

        if (getBuilder().mTitle != 0)
            dialogBuilder.title(getBuilder().mTitle);

        if (getBuilder().mPositiveButton != null) {
            dialogBuilder.positiveText(getBuilder().mPositiveButton)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Log.d(getClass().getName(), "onClick: mCallback " + mCallback);
                            mCallback.onPositive(getBuilder().mTag, dialog);
                        }
                    });
        }

        if (getBuilder().mNegativeButton != null) {
            dialogBuilder.negativeText(getBuilder().mNegativeButton)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onNegative(getBuilder().mTag, dialog);
                        }
                    });
        }

        if (getBuilder().mNeutralButton != null) {
            dialogBuilder.negativeText(getBuilder().mNeutralButton)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mCallback.onNeutral(
                                    getBuilder().mTag, dialog);
                        }
                    });
        }

        MaterialDialog dialog = dialogBuilder.build();

        if (getBuilder().mPrefill != null)
            dialog.getInputEditText().selectAll();

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

        dialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        return dialog;
    }

    public void setContent(@StringRes int res) {
        ((MaterialDialog) getDialog()).setContent(res);
    }

    public void setmCallback(@NonNull SimpleInputCallback callback) {
        mCallback = callback;
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
        @StringRes
        protected int mTitle = 0;
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
        @NonNull
        protected transient SimpleInputCallback mListener;
        @Nullable
        protected CharSequence mPrefill = null;

        public <ActivityType extends AppCompatActivity> Builder(@NonNull ActivityType context, @NonNull SimpleInputCallback listener, @NonNull String tag) {
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
        public Builder prefill(@StringRes int text) {
            mPrefill = this.mContext.getResources().getText(text);
            return this;
        }

        @NonNull
        public Builder prefill(@NonNull String text) {
            mPrefill = text;
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
        public InputTextDialogFragment build() {
            InputTextDialogFragment dialog = new InputTextDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable("builder", this);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        public InputTextDialogFragment show() {
            InputTextDialogFragment dialog = build();
            dialog.show(mContext);
            return dialog;
        }
    }

    private Builder getBuilder() {
        if (getArguments() == null || !getArguments().containsKey("builder")) return null;
        return (Builder) getArguments().getSerializable("builder");
    }

    @Nullable
    public static InputTextDialogFragment findVisible(@NonNull AppCompatActivity context, String tag) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(tag);
        if (frag != null && frag instanceof InputTextDialogFragment)
            return (InputTextDialogFragment) frag;
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
    public InputTextDialogFragment show(AppCompatActivity context) {
        Builder builder = getBuilder();
        dismissIfNecessary(context, builder.mTag);
        show(context.getSupportFragmentManager(), builder.mTag);
        return this;
    }

    public interface SimpleInputCallback {
        void onPositive(@NonNull String tag, @NonNull MaterialDialog dialog);
        void onNegative(@NonNull String tag, @NonNull MaterialDialog dialog);
        void onNeutral(@NonNull String tag, @NonNull MaterialDialog dialog);
    }

}
