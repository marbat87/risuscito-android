package it.cammino.risuscito.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.adapters.BottomSheetAdapter;

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

    public static BottomSheetFragment newInstance(String title, Intent intent) {
        BottomSheetFragment frag = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
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

        Boolean showTitle = getArguments().getBoolean("showTitle");
        TextView titleView = (TextView) view.findViewById(R.id.sheet_title);
        if (showTitle) {
            String title = getArguments().getString("title");
            titleView.setText(title);
            titleView.setVisibility(View.VISIBLE);
        }
        else {
            titleView.setVisibility(View.GONE);
        }

        final Intent intent = getArguments().getParcelable("intent");
        PackageManager pm = getActivity().getPackageManager();

        final List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);

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

//        Log.i("BottomSheetHelper", "list size: " + list.size());

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putString("ULTIMA_APP_USATA", ((TextView) v.findViewById(R.id.app_package)).getText().toString());
                editor.apply();

                ComponentName name = new ComponentName(((TextView) v.findViewById(R.id.app_package)).getText().toString(),
                        ((TextView) v.findViewById(R.id.app_name)).getText().toString());
                Intent newIntent = (Intent) intent.clone();
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                newIntent.setComponent(name);
                getActivity().startActivity(newIntent);
                getDialog().dismiss();
            }
        };

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.shareList);
        BottomSheetAdapter adapter = new BottomSheetAdapter(getActivity(), list, clickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        return view;
    }

//    public interface AlertDialogListener {
//        public void onAlertDialogPositiveClick(int dialogType, String id);
//        public void onAlertDialogNegativeClick(int dialogType, String id);
//    }

}