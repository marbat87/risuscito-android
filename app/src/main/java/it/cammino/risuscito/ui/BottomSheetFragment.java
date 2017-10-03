package it.cammino.risuscito.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;
import it.cammino.risuscito.items.BottomSheetItem;

public class BottomSheetFragment extends BottomSheetDialogFragment {

//    AlertDialogListener mListener;

    public static BottomSheetFragment newInstance(Intent intent) {
        BottomSheetFragment frag = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putBoolean("showTitle", false);
        args.putParcelable("intent", intent);
        frag.setArguments(args);
        return frag;
    }

    public static BottomSheetFragment newInstance(@StringRes int title, Intent intent) {
        BottomSheetFragment frag = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putBoolean("showTitle", true);
        args.putParcelable("intent", intent);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @BindView(R.id.sheet_title) TextView titleView;
    @BindView(R.id.sheet_title_area) View titleArea;
    @BindView(R.id.shareList) RecyclerView mRecyclerView;

//    @Override
//    public void onAttach(Context activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (AlertDialogListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement AlertDialogListener");
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        ButterKnife.bind(this, view);

        Boolean showTitle = getArguments().getBoolean("showTitle");
        //noinspection ConstantConditions
        titleView.setText(showTitle? getArguments().getInt("title"): null);
        titleArea.setVisibility(showTitle? View.VISIBLE: View.GONE);
//        TextView titleView = (TextView) view.findViewById(R.id.sheet_title);
//        if (showTitle) {
////            int title = getArguments().getInt("title");
//            titleView.setText(getArguments().getInt("title"));
////            view.findViewById(R.id.sheet_title_area).setVisibility(View.VISIBLE);
//            titleArea.setVisibility(View.VISIBLE);
//        }
//        else {
////            view.findViewById(R.id.sheet_title_area).setVisibility(View.GONE);
//            titleArea.setVisibility(View.GONE);
//        }

        final Intent intent = getArguments().getParcelable("intent");
        PackageManager pm = getActivity().getPackageManager();

        final List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        final List<BottomSheetItem> mList = new ArrayList<>();

//        Log.i("BottomSheetHelper", "list size: " + list.size());

        String lastApp = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getString("ULTIMA_APP_USATA", "");
        ResolveInfo lastAppInfo = null;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).activityInfo.applicationInfo.packageName.equals(lastApp)) {
                lastAppInfo = list.remove(i);
                break;
            }
        }

//        Log.i("BottomSheetHelper", "list size: " + list.size());

        if (lastAppInfo != null)
            list.add(0, lastAppInfo);

        for (ResolveInfo item: list)
            mList.add(new BottomSheetItem().withItem(item));

//        Log.i("BottomSheetHelper", "list size: " + list.size());

//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor = PreferenceManager
//                        .getDefaultSharedPreferences(getActivity())
//                        .edit();
//                editor.putString("ULTIMA_APP_USATA", ((TextView) v.findViewById(R.id.app_package)).getText().toString());
//                editor.apply();
//
//                ComponentName name = new ComponentName(((TextView) v.findViewById(R.id.app_package)).getText().toString(),
//                        ((TextView) v.findViewById(R.id.app_name)).getText().toString());
//                if (intent != null) {
//                    Intent newIntent = (Intent) intent.clone();
//                    newIntent.setComponent(name);
//                    getActivity().startActivity(newIntent);
//                    getDialog().dismiss();
//                }
//            }
//        };

        FastAdapter.OnClickListener<BottomSheetItem> mOnClickListener = new FastAdapter.OnClickListener<BottomSheetItem>() {
            @Override
            public boolean onClick(View view, IAdapter<BottomSheetItem> iAdapter, BottomSheetItem item, int i) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putString("ULTIMA_APP_USATA", item.getItem().activityInfo.packageName);
                editor.apply();

                ComponentName name = new ComponentName(item.getItem().activityInfo.packageName, item.getItem().activityInfo.name);
                if (intent != null) {
                    Intent newIntent = (Intent) intent.clone();
                    newIntent.setComponent(name);
                    getActivity().startActivity(newIntent);
                    getDialog().dismiss();
                }
                return true;
            }
        };

//        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.shareList);
//        BottomSheetAdapter adapter = new BottomSheetAdapter(getActivity(), list, clickListener);
        FastItemAdapter<BottomSheetItem> adapter = new FastItemAdapter<>();
        adapter.add(mList);
        adapter.withOnClickListener(mOnClickListener);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

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
            if (win != null)
                win.setLayout(mMaxWidth, -1);
        }
    }

//    public interface AlertDialogListener {
//        public void onAlertDialogPositiveClick(int dialogType, String id);
//        public void onAlertDialogNegativeClick(int dialogType, String id);
//    }

}