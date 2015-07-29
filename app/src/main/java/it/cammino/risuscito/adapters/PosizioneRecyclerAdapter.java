package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.content.res.Resources;
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
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.objects.CantoHistory;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.ui.ThemeableActivity;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class PosizioneRecyclerAdapter extends RecyclerView.Adapter {

    private List<PosizioneItem> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private Activity context;

    // Adapter constructor 1
    public PosizioneRecyclerAdapter(Activity activity, List<PosizioneItem> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.context = activity;
    }

    // Adapter constructor 2
    public PosizioneRecyclerAdapter(Activity activity, List<PosizioneItem> dataItems
            , View.OnClickListener clickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.context = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.generic_list_item, viewGroup, false);
        return new CantoViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        PosizioneItem dataItem = dataItems.get(position);

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;

        if (dataItem.ismChoosen()) {
            cantoHolder.addCanto.setVisibility(View.GONE);
            cantoHolder.canto.setVisibility(View.VISIBLE);
            cantoHolder.cantoTitle.setText(dataItem.getTitolo());
            cantoHolder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
            cantoHolder.idCanto.setText(String.valueOf(dataItem.getIdCanto()));
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
            if (context != null) {
                if (dataItem.ismSelected())
                    cantoHolder.cardView.setBackgroundColor(((ThemeableActivity) context).getThemeUtils().accentColorLight());
                else {
                    TypedValue typedValue = new TypedValue();
                    Resources.Theme theme = context.getTheme();
                    theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                    cantoHolder.cardView.setBackgroundResource(typedValue.resourceId);
                }

            }
            if (clickListener != null)
                cantoHolder.canto.setOnClickListener(clickListener);
            if (longClickListener != null)
                cantoHolder.canto.setOnLongClickListener(longClickListener);
        }
        else {
            cantoHolder.addCanto.setVisibility(View.VISIBLE);
            cantoHolder.canto.setVisibility(View.GONE);
            if (clickListener != null)
                cantoHolder.addCanto.setOnClickListener(clickListener);
        }

        cantoHolder.idLista.setText(String.valueOf(dataItem.getIdLista()));
        cantoHolder.idPosizione.setText(String.valueOf(dataItem.getIdPosizione()));
        cantoHolder.nomePosizione.setText(dataItem.getTitoloPosizione());
        cantoHolder.tag.setText(String.valueOf(dataItem.getTag()));

    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class CantoViewHolder extends RecyclerView.ViewHolder {

        public TextView cantoTitle;
        public TextView cantoPage;
        public TextView idCanto;
        public TextView sourceCanto;
        public TextView timestamp;
        public TextView idLista;
        public TextView idPosizione;
        public TextView nomePosizione;
        public View addCanto;
        public View canto;
        public View cardView;
        public TextView tag;

        public CantoViewHolder(View itemView) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            idCanto = (TextView) itemView.findViewById(R.id.text_id_canto);
            sourceCanto = (TextView) itemView.findViewById(R.id.text_source_canto);
            timestamp = (TextView) itemView.findViewById(R.id.text_timestamp);
            idLista = (TextView) itemView.findViewById(R.id.text_id_lista);
            idPosizione = (TextView) itemView.findViewById(R.id.text_id_posizione);
            nomePosizione = (TextView) itemView.findViewById(R.id.titoloPosizioneGenerica);
            addCanto = itemView.findViewById(R.id.addCantoGenerico);
            canto = itemView.findViewById(R.id.cantoGenericoContainer);
            cardView = itemView.findViewById(R.id.cardView);
            tag = (TextView) itemView.findViewById(R.id.tag);
        }



    }
}
