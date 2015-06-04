package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.objects.CantoHistory;

/**
 * Created by marcello.battain on 12/01/2015.
 */
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
        String timestamp = "";

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
        cantoHolder.timestamp.setText(context.getString(R.string.last_open_date) + " " + timestamp);

        cantoHolder.sourceCanto.setText(dataItem.getSource());

        if (dataItem.getColore().equalsIgnoreCase(Utility.GIALLO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_yellow);
        if (dataItem.getColore().equalsIgnoreCase(Utility.GRIGIO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_grey);
        if (dataItem.getColore().equalsIgnoreCase(Utility.VERDE))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_green);
        if (dataItem.getColore().equalsIgnoreCase(Utility.AZZURRO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_blue);
        if (dataItem.getColore().equalsIgnoreCase(Utility.BIANCO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_white);
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
