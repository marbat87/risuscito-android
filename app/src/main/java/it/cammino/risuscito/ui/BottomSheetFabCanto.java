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
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import it.cammino.risuscito.R;

public class BottomSheetFabCanto extends BottomSheetDialogFragment {

    public final static String CHOOSE_DONE = "canto_choose_done";
    public final static String DATA_ITEM_ID = "item_id";

    public final static int FULLSCREEN = 1;
    public final static int SOUND = 2;
    public final static int SAVE_FILE = 3;
    public final static int FAVORITE = 4;

    //    AlertDialogListener mListener;
    private String TAG  = getClass().getCanonicalName();

    public static BottomSheetFabCanto newInstance(boolean sound, boolean download, boolean favorite) {
        BottomSheetFabCanto frag = new BottomSheetFabCanto();
        Bundle args = new Bundle();
        args.putBoolean("showTitle", false);
        args.putBoolean("sound", sound);
        args.putBoolean("download", download);
        args.putBoolean("favorite", favorite);
        frag.setArguments(args);
        return frag;
    }

    public static BottomSheetFabCanto newInstance(@StringRes int title, boolean sound, boolean download, boolean favorite) {
        BottomSheetFabCanto frag = new BottomSheetFabCanto();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putBoolean("showTitle", true);
        args.putBoolean("sound", sound);
        args.putBoolean("download", download);
        args.putBoolean("favorite", favorite);
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
        View view = inflater.inflate(R.layout.bottom_sheet_fabcanto, container, false);

        Boolean showTitle = getArguments().getBoolean("showTitle");
        Boolean mSound = getArguments().getBoolean("sound");
        Boolean mDownload = getArguments().getBoolean("download");
        Boolean mFavorite = getArguments().getBoolean("favorite");

        TextView titleView = (TextView) view.findViewById(R.id.sheet_title);
        if (showTitle) {
//            int title = getArguments().getInt("title");
            titleView.setText(getArguments().getInt("title"));
            view.findViewById(R.id.sheet_title_area).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.sheet_title_area).setVisibility(View.GONE);
        }

        View mView = view.findViewById(R.id.fab_fullscreen_on);
        ImageView mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_fullscreen_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        IconicsDrawable icon = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_fullscreen)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        TextView mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(R.string.fullscreen);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + FULLSCREEN);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, FULLSCREEN);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        mView = view.findViewById(R.id.fab_sound_off);
        mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(),mSound ? R.drawable.ic_queue_music_off_white_48dp: R.drawable.ic_queue_music_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        icon = new IconicsDrawable(getActivity())
                .icon(mSound ? CommunityMaterial.Icon.cmd_headset_off : CommunityMaterial.Icon.cmd_headset)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(mSound ? R.string.audio_off: R.string.audio_on);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + SOUND);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, SOUND);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        mView = view.findViewById(R.id.save_file);
        mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(),mDownload ? R.drawable.ic_delete_48dp: R.drawable.ic_file_download_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        icon = new IconicsDrawable(getActivity())
                .icon(mDownload ? CommunityMaterial.Icon.cmd_delete : CommunityMaterial.Icon.cmd_download)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(mDownload? R.string.fab_delete_unlink: R.string.save_file);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + SAVE_FILE);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, SAVE_FILE);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        mView = view.findViewById(R.id.fab_favorite);
        mImage = (ImageView) mView.findViewById(R.id.app_icon);
//        drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(),mFavorite? R.drawable.ic_favorite_48dp: R.drawable.ic_favorite_outline_48dp));
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
//        mImage.setImageDrawable(drawable);
        icon = new IconicsDrawable(getActivity())
                .icon(mFavorite ? CommunityMaterial.Icon.cmd_heart : CommunityMaterial.Icon.cmd_heart_outline)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(48)
                .paddingDp(4);
        mImage.setImageDrawable(icon);
        mTextView = (TextView) mView.findViewById(R.id.app_label);
        mTextView.setText(mFavorite? R.string.favorite_off: R.string.favorite_on);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE);
                Log.d(TAG, "clicked id: " + FAVORITE);
                Intent intentBroadcast = new Intent(CHOOSE_DONE);
                intentBroadcast.putExtra(DATA_ITEM_ID, FAVORITE);
                getActivity().sendBroadcast(intentBroadcast);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        boolean mLimited = getActivity().getResources().getBoolean(R.bool.is_bottom_sheet_limited);
        if (mLimited) {
            int mMaxWidth = (int) getActivity().getResources().getDimension(R.dimen.max_bottomsheet_width);
//            getDialog().getWindow().setLayout(mMaxWidth, -1);
            Window win = getDialog().getWindow();
            if (null != win)
                win.setLayout(mMaxWidth, -1);
        }
    }

}