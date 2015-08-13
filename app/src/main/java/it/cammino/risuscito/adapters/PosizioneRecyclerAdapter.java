package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.objects.PosizioneTitleItem;
import it.cammino.risuscito.ui.ThemeableActivity;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class PosizioneRecyclerAdapter extends RecyclerView.Adapter {

    //    private List<PosizioneItem> dataItems;
    private List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private Activity context;

    // Adapter constructor 1
    public PosizioneRecyclerAdapter(Activity activity, List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.context = activity;
    }

    // Adapter constructor 2
    public PosizioneRecyclerAdapter(Activity activity, List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems
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
        return new TitleViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        PosizioneTitleItem dataItem = dataItems.get(position).first;

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        TitleViewHolder titleHolder = (TitleViewHolder) viewHolder;

        List<PosizioneItem> listItems = dataItems.get(position).second;
        titleHolder.list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView;

        if (listItems.size() > 0) {
            if (dataItem.isMultiple()) {
                titleHolder.addCanto.setVisibility(View.VISIBLE);
                if (clickListener != null)
                    titleHolder.addCanto.setOnClickListener(clickListener);
            }
            else
                titleHolder.addCanto.setVisibility(View.GONE);
            for (int i = 0; i < listItems.size(); i++) {
                PosizioneItem canto = listItems.get(i);
                itemView = inflater.inflate(R.layout.generic_card_item, null, false);

                TextView cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
                TextView cantoPage = (TextView) itemView.findViewById(R.id.text_page);
                TextView idCanto = (TextView) itemView.findViewById(R.id.text_id_canto);
                TextView sourceCanto = (TextView) itemView.findViewById(R.id.text_source_canto);
                TextView timestamp = (TextView) itemView.findViewById(R.id.text_timestamp);
                View cantoView = itemView.findViewById(R.id.cantoGenericoContainer);
                TextView itemTag = (TextView) itemView.findViewById(R.id.item_tag);

                cantoTitle.setText(canto.getTitolo());
                cantoPage.setText(String.valueOf(canto.getPagina()));
                idCanto.setText(String.valueOf(canto.getIdCanto()));
                sourceCanto.setText(canto.getSource());
                timestamp.setText(canto.getTimestamp());
                itemTag.setText(String.valueOf(i));
                if (canto.getColore().equalsIgnoreCase(Utility.GIALLO))
                    cantoPage.setBackgroundResource(R.drawable.bkg_round_yellow);
                if (canto.getColore().equalsIgnoreCase(Utility.GRIGIO))
                    cantoPage.setBackgroundResource(R.drawable.bkg_round_grey);
                if (canto.getColore().equalsIgnoreCase(Utility.VERDE))
                    cantoPage.setBackgroundResource(R.drawable.bkg_round_green);
                if (canto.getColore().equalsIgnoreCase(Utility.AZZURRO))
                    cantoPage.setBackgroundResource(R.drawable.bkg_round_blue);
                if (canto.getColore().equalsIgnoreCase(Utility.BIANCO))
                    cantoPage.setBackgroundResource(R.drawable.bkg_round_white);
                if (context != null) {
                    if (canto.ismSelected())
                        cantoView.setBackgroundColor(((ThemeableActivity) context).getThemeUtils().accentColorLight());
                    else {
                        TypedValue typedValue = new TypedValue();
                        Resources.Theme theme = context.getTheme();
                        theme.resolveAttribute(R.attr.customSelector, typedValue, true);
                        cantoView.setBackgroundResource(typedValue.resourceId);
                    }

                }
                if (clickListener != null)
                    cantoView.setOnClickListener(clickListener);
                if (longClickListener != null)
                    cantoView.setOnLongClickListener(longClickListener);
                titleHolder.list.addView(itemView);
            }
        }
        else {
            titleHolder.addCanto.setVisibility(View.VISIBLE);
//            titleHolder.canto.setVisibility(View.GONE);
            if (clickListener != null)
                titleHolder.addCanto.setOnClickListener(clickListener);
        }

        titleHolder.idLista.setText(String.valueOf(dataItem.getIdLista()));
        titleHolder.idPosizione.setText(String.valueOf(dataItem.getIdPosizione()));
        titleHolder.nomePosizione.setText(dataItem.getTitoloPosizione());
        titleHolder.tag.setText(String.valueOf(dataItem.getTag()));

    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {

        public TextView idLista;
        public TextView idPosizione;
        public TextView nomePosizione;
        public View addCanto;
        //        public View canto;
        public View cardView;
        public TextView tag;
        public LinearLayout list;

        public TitleViewHolder(View itemView) {
            super(itemView);
            idLista = (TextView) itemView.findViewById(R.id.text_id_lista);
            idPosizione = (TextView) itemView.findViewById(R.id.text_id_posizione);
            nomePosizione = (TextView) itemView.findViewById(R.id.titoloPosizioneGenerica);
            addCanto = itemView.findViewById(R.id.addCantoGenerico);
            cardView = itemView.findViewById(R.id.cardView);
            tag = (TextView) itemView.findViewById(R.id.tag);
            list = (LinearLayout) itemView.findViewById(R.id.list);
        }

    }

//    public static class CantoViewHolder extends RecyclerView.ViewHolder {
//
//        public TextView cantoTitle;
//        public TextView cantoPage;
//        public TextView idCanto;
//        public TextView sourceCanto;
//        public TextView timestamp;
//        public View canto;
//
//        public CantoViewHolder(View itemView) {
//            super(itemView);
//            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
//            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
//            idCanto = (TextView) itemView.findViewById(R.id.text_id_canto);
//            sourceCanto = (TextView) itemView.findViewById(R.id.text_source_canto);
//            timestamp = (TextView) itemView.findViewById(R.id.text_timestamp);
//            canto = itemView.findViewById(R.id.cantoGenericoContainer);
//        }
//
//    }
}
