package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.objects.CantoHistory;
import it.cammino.risuscito.ui.ThemeableActivity;

public class CantoHistoryRecyclerAdapter extends RecyclerView.Adapter {

    private List<CantoHistory> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    private Activity context;

    // Adapter constructor 1
    public CantoHistoryRecyclerAdapter(Activity activity, List<CantoHistory> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.createContextMenuListener = null;
        this.context = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.row_item_history, viewGroup, false);
        return new CantoViewHolder(layoutView, clickListener, longClickListener, createContextMenuListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CantoHistory dataItem = dataItems.get(position);

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;
        cantoHolder.cantoTitle.setText(dataItem.getTitolo());
        cantoHolder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
        cantoHolder.idCanto.setText(String.valueOf(dataItem.getIdCanto()));

        //FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
        DateFormat df = DateFormat.getDateTimeInstance(
                DateFormat.SHORT
                , DateFormat.MEDIUM
                , context.getResources().getConfiguration().locale);
        String timestamp;

        if (df instanceof SimpleDateFormat)
        {
//                Log.i(getClass().toString(), "is Simple");
            SimpleDateFormat sdf = (SimpleDateFormat) df;
            // To show Locale specific short date expression with full year
            String pattern = sdf.toPattern().replaceAll("y+","yyyy");
            sdf.applyPattern(pattern);
            timestamp = sdf.format(Timestamp.valueOf(dataItem.getTimestamp()));
        }
        else {
//                Log.i(getClass().toString(), "is NOT Simple");
            timestamp = df.format(Timestamp.valueOf(dataItem.getTimestamp()));
        }
//        cantoHolder.timestamp.setText(context.getString(R.string.last_open_date) + " " + timestamp);
        cantoHolder.timestamp.setText(context.getString(R.string.last_open_date, timestamp));
        cantoHolder.sourceCanto.setText(dataItem.getSource());
//        cantoHolder.cantoPage.setBackgroundResource(
//                context.getResources().getIdentifier("page_oval_border_bkg_" + dataItem.getColore().substring(1).toLowerCase()
//                        , "drawable"
//                        , context.getPackageName()));
        GradientDrawable bgShape = (GradientDrawable)cantoHolder.cantoPage.getBackground();
        bgShape.setColor(Color.parseColor(dataItem.getColore()));

        if (context != null) {
            if (dataItem.ismSelected())
                cantoHolder.itemView.setBackgroundColor(((ThemeableActivity) context).getThemeUtils().accentColorLight());
            else {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                cantoHolder.itemView.setBackgroundResource(typedValue.resourceId);
            }

        }
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class CantoViewHolder extends RecyclerView.ViewHolder {

        public TextView cantoTitle;
        public TextView cantoPage;
        public TextView timestamp;
        public TextView idCanto;
        public TextView sourceCanto;

        public CantoViewHolder(View itemView
                , View.OnClickListener onClickListener
                , View.OnLongClickListener onLongClickListener
                , View.OnCreateContextMenuListener onCreateContextMenuListener) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            timestamp = (TextView) itemView.findViewById(R.id.text_timestamp);
            idCanto = (TextView) itemView.findViewById(R.id.text_id_canto);
            sourceCanto = (TextView) itemView.findViewById(R.id.text_source_canto);
            if (onClickListener != null)
                itemView.setOnClickListener(onClickListener);
            if (onLongClickListener != null)
                itemView.setOnLongClickListener(onLongClickListener);
            if (onCreateContextMenuListener != null)
                itemView.setOnCreateContextMenuListener(onCreateContextMenuListener);
        }



    }
}
