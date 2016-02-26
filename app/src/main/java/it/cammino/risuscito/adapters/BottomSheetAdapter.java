package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.cammino.risuscito.R;

public class BottomSheetAdapter extends RecyclerView.Adapter {

    private List<ResolveInfo> dataItems;
    private View.OnClickListener clickListener;
    private Activity context;

    // Adapter constructor 1
    public BottomSheetAdapter(Activity activity, List<ResolveInfo> dataItems, View.OnClickListener clickListener) {
        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.context = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.bottom_item, viewGroup, false);
        return new BottomItemViewHolder(layoutView, clickListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        ResolveInfo dataItem = dataItems.get(position);
        // Casting the viewHolder to MyViewHolder so I could interact with the views
        BottomItemViewHolder cantoHolder = (BottomItemViewHolder) viewHolder;
        PackageManager pm = context.getPackageManager();
        cantoHolder.appIcon.setImageDrawable(dataItem.loadIcon(pm));
        cantoHolder.appLabel.setText(dataItem.loadLabel(pm));
        cantoHolder.appName.setText(dataItem.activityInfo.name);
        cantoHolder.appPackage.setText(dataItem.activityInfo.packageName);
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class BottomItemViewHolder extends RecyclerView.ViewHolder {

        public ImageView appIcon;
        public TextView appLabel;
        public TextView appName;
        public TextView appPackage;

        public BottomItemViewHolder(View itemView
                , View.OnClickListener onClickListener) {
            super(itemView);
            appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            appLabel = (TextView) itemView.findViewById(R.id.app_label);
            appName = (TextView) itemView.findViewById(R.id.app_name);
            appPackage = (TextView) itemView.findViewById(R.id.app_package);
            if (onClickListener != null)
                itemView.setOnClickListener(onClickListener);
        }

    }

}
