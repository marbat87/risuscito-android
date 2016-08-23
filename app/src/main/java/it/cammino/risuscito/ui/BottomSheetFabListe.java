package it.cammino.risuscito.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import it.cammino.risuscito.R;

public class BottomSheetFabListe extends BottomSheetDialogFragment {

    public final static String CHOOSE_DONE = "choose_done";
    public final static String DATA_ITEM_ID = "item_id";

    public final static int CLEAN = 1;
    public final static int SHARE_TEXT = 3;
    public final static int SHARE_FILE = 4;
    public final static int ADD_LIST = 2;
    public final static int EDIT_LIST = 5;
    public final static int DELETE_LIST = 6;

    //    AlertDialogListener mListener;
    private String TAG  = getClass().getCanonicalName();

    public static BottomSheetFabListe newInstance(boolean customList) {
        BottomSheetFabListe frag = new BottomSheetFabListe();
        Bundle args = new Bundle();
        args.putBoolean("showTitle", false);
        args.putBoolean("customList", customList);
        frag.setArguments(args);
        return frag;
    }

    public static BottomSheetFabListe newInstance(@StringRes int title, boolean customList) {
        BottomSheetFabListe frag = new BottomSheetFabListe();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putBoolean("showTitle", true);
        args.putBoolean("customList", customList);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_fablist, container, false);

        Boolean showTitle = getArguments().getBoolean("showTitle");
        TextView titleView = (TextView) view.findViewById(R.id.sheet_title);
        if (showTitle) {
//            int title = getArguments().getInt("title");
            titleView.setText(getArguments().getInt("title"));
            view.findViewById(R.id.sheet_title_area).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.sheet_title_area).setVisibility(View.GONE);
        }

        View mView = view.findViewById(R.id.fab_pulisci);
        ImageView mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_eraser_white_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        IconicsDrawable icon = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_eraser)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        TextView mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(R.string.button_clean_list);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + CLEAN);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, CLEAN);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        mView = view.findViewById(R.id.fab_condividi);
        mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_share_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        icon = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_share_variant)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(R.string.action_share);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + SHARE_TEXT);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, SHARE_TEXT);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        mView = view.findViewById(R.id.fab_add_lista);
        mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_add_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        icon = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_plus)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(R.string.action_add_list);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + ADD_LIST);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, ADD_LIST);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        Boolean customList = getArguments().getBoolean("customList", false);

        if (customList) {
            view.findViewById(R.id.custom_list_option).setVisibility(View.VISIBLE);

            mView = view.findViewById(R.id.fab_condividi_file);
            mImage = (ImageView) mView.findViewById(R.id.app_icon);
//            drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_attachment_48dp));
//            DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//            mImage.setImageDrawable(drawable);
            icon = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_attachment)
                    .colorRes(R.color.icon_ative_black)
                    .sizeDp(48)
                    .paddingDp(4);
            mImage.setImageDrawable(icon);
            mTextView = (TextView) mView.findViewById(R.id.app_label);
            mTextView.setText(R.string.action_share_file);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                    Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                    Log.d(TAG, "clicked id: " + SHARE_FILE);
                    Intent intentBroadcast = new Intent(CHOOSE_DONE);
                    intentBroadcast.putExtra(DATA_ITEM_ID, SHARE_FILE);
                    getActivity().sendBroadcast(intentBroadcast);
                }
            });

            mView = view.findViewById(R.id.fab_edit_lista);
            mImage = (ImageView) mView.findViewById(R.id.app_icon);
//            drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_edit_48dp));
//            DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//            mImage.setImageDrawable(drawable);
            icon = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_pencil)
                    .colorRes(R.color.icon_ative_black)
                    .sizeDp(48)
                    .paddingDp(4);
            mImage.setImageDrawable(icon);
            mTextView = (TextView) mView.findViewById(R.id.app_label);
            mTextView.setText(R.string.action_edit_list);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                    Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                    Log.d(TAG, "clicked id: " + EDIT_LIST);
                    Intent intentBroadcast = new Intent(CHOOSE_DONE);
                    intentBroadcast.putExtra(DATA_ITEM_ID, EDIT_LIST);
                    getActivity().sendBroadcast(intentBroadcast);
                }
            });

            mView = view.findViewById(R.id.fab_delete_lista);
            mImage = (ImageView) mView.findViewById(R.id.app_icon);
//            drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_delete_48dp));
//            DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//            mImage.setImageDrawable(drawable);
            icon = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_delete)
                    .colorRes(R.color.icon_ative_black)
                    .sizeDp(48)
                    .paddingDp(4);
            mImage.setImageDrawable(icon);
            mTextView = (TextView) mView.findViewById(R.id.app_label);
            mTextView.setText(R.string.action_remove_list);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                    Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                    Log.d(TAG, "clicked id: " + DELETE_LIST);
                    Intent intentBroadcast = new Intent(CHOOSE_DONE);
                    intentBroadcast.putExtra(DATA_ITEM_ID, DELETE_LIST);
                    getActivity().sendBroadcast(intentBroadcast);
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        boolean mLimited = getActivity().getResources().getBoolean(R.bool.is_bottom_sheet_limited);
        if (mLimited) {
            int mMaxWidth = (int) getActivity().getResources().getDimension(R.dimen.max_bottomsheet_width);
            getDialog().getWindow().setLayout(mMaxWidth, -1);
        }
    }

}