package it.cammino.risuscito.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    // Adapter constructor 1
    public CantoHistoryRecyclerAdapter(List<CantoHistory> dataItems
            , View.OnClickListener clickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        createContextMenuListener = null;
    }

    // Adapter constructor 2
    public CantoHistoryRecyclerAdapter(List<CantoHistory> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.createContextMenuListener = null;
    }

    // Adapter constructor 3
    public CantoHistoryRecyclerAdapter(List<CantoHistory> dataItems
            , View.OnClickListener clickListener
            , View.OnCreateContextMenuListener createContextMenuListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.createContextMenuListener = createContextMenuListener;
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
        cantoHolder.cantoPage.setText(dataItem.getPagina());
        cantoHolder.idCanto.setText(dataItem.getIdCanto());
        cantoHolder.sourceCanto.setText(dataItem.getSource());
        cantoHolder.timestamp.setText(dataItem.getTimestamp());

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
