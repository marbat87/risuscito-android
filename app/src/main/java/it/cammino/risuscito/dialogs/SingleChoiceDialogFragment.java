package it.cammino.risuscito.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.Serializable;

public class SingleChoiceDialogFragment extends DialogFragment {

    private final String TAG = getClass().getCanonicalName();
    
    protected SingleChoiceCallback mCallback;
    protected AppCompatActivity mContext;

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: context - " + context);
        super.onAttach(context);
        mContext = (AppCompatActivity) context;
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
////        if (!(activity instanceof SingleChoiceCallback))
////            throw new IllegalStateException("SingleChoiceDialogFragment needs to be shown from an Activity implementing SingleChoiceCallback.");
////        mCallback = (SingleChoiceCallback) activity;
//        mContext = (AppCompatActivity) activity;
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
                .items(getBuilder().mItems)
                .itemsCallbackSingleChoice(getBuilder().mDefaultIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        return mCallback.onSelection(getBuilder().mTag, mContext, dialog, view, which, text);
                    }
                });


        if (getBuilder().mTitle != 0)
            dialogBuilder.title(getBuilder().mTitle);

        if (getBuilder().mNegativeButton != null) {
            dialogBuilder.negativeText(getBuilder().mNegativeButton);
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

        return dialog;
    }

    public void setContent(@StringRes int res) {
        ((MaterialDialog) getDialog()).setContent(res);
    }

    public void setmCallback(@NonNull SingleChoiceCallback callback) {
        mCallback = callback;
    }

    public void cancel() {
        getDialog().cancel();
    }

    public static class Builder implements Serializable {

        @NonNull
        protected final transient AppCompatActivity mContext;
        @StringRes
        protected int mTitle = 0;
        @NonNull
        protected final String mTag;
        @StringRes
        protected CharSequence mNegativeButton;
        protected boolean mCanceable = false;
        @ArrayRes
        protected int mItems;
        protected int mDefaultIndex;
        @NonNull
        protected transient SingleChoiceCallback mListener;

        public <ActivityType extends AppCompatActivity> Builder(@NonNull ActivityType context, @NonNull SingleChoiceCallback listener, @NonNull String tag) {
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
        public Builder negativeButton(@StringRes int text) {
            mNegativeButton = this.mContext.getResources().getText(text);
            return this;
        }

        @NonNull
        public Builder setCanceable(boolean canceable) {
            mCanceable = canceable;
            return this;
        }

        @NonNull
        public Builder defaultIndex(int index) {
            mDefaultIndex = index;
            return this;
        }

        @NonNull
        public Builder items(@ArrayRes int items) {
            mItems = items;
            return this;
        }

        @NonNull
        public SingleChoiceDialogFragment build() {
            SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable("builder", this);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        public SingleChoiceDialogFragment show() {
            SingleChoiceDialogFragment dialog = build();
            dialog.show(mContext);
            return dialog;
        }
    }

    private Builder getBuilder() {
        if (getArguments() == null || !getArguments().containsKey("builder")) return null;
        return (Builder) getArguments().getSerializable("builder");
    }

    @Nullable
    public static SingleChoiceDialogFragment findVisible(@NonNull AppCompatActivity context, String tag) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(tag);
        if (frag != null && frag instanceof SingleChoiceDialogFragment)
            return (SingleChoiceDialogFragment) frag;
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
    public SingleChoiceDialogFragment show(AppCompatActivity context) {
        Builder builder = getBuilder();
        dismissIfNecessary(context, builder.mTag);
        show(context.getSupportFragmentManager(), builder.mTag);
        return this;
    }

    public interface SingleChoiceCallback {
        boolean onSelection(@NonNull String tag, @NonNull AppCompatActivity context, MaterialDialog dialog, View view, int which, CharSequence text);
    }

}
