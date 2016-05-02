package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.objects.CantoInsert;

public class CantoInsertRecyclerAdapter extends RecyclerView.Adapter {

    private List<CantoInsert> dataItems;
    private View.OnClickListener clickListener;
    private View.OnClickListener seeClickListener;
    private Activity context;

    // Adapter constructor 1
    public CantoInsertRecyclerAdapter(Activity context, List<CantoInsert> dataItems
            , View.OnClickListener clickListener) {

        this.context = context;
        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.seeClickListener = null;
    }

    // Adapter constructor 2
    public CantoInsertRecyclerAdapter(Activity context, List<CantoInsert> dataItems
            , View.OnClickListener clickListener
            , View.OnClickListener seeClickListener) {

        this.context = context;
        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.seeClickListener = seeClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.row_item_to_insert, viewGroup, false);
        return new CantoViewHolder(layoutView, clickListener, seeClickListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CantoInsert dataItem = dataItems.get(position);

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;
        cantoHolder.cantoTitle.setText(dataItem.getTitolo());
        cantoHolder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
        cantoHolder.idCanto.setText(String.valueOf(dataItem.getIdCanto()));
        cantoHolder.sourceCanto.setText(dataItem.getSource());

        Drawable drawable = DrawableCompat.wrap(cantoHolder.seeCanto.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.icon_ative_black));
        cantoHolder.cantoPage.setBackgroundResource(
                context.getResources().getIdentifier("page_oval_border_bkg_" + dataItem.getColore().substring(1).toLowerCase()
                        , "drawable"
                        , context.getPackageName()));

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
        public ImageView seeCanto;

        public CantoViewHolder(View itemView
                , View.OnClickListener onClickListener
                , View.OnClickListener seeOnClickListener) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            idCanto = (TextView) itemView.findViewById(R.id.text_id_canto);
            sourceCanto = (TextView) itemView.findViewById(R.id.text_source_canto);
            seeCanto = (ImageView) itemView.findViewById(R.id.see_canto);

            if (onClickListener != null)
                itemView.setOnClickListener(onClickListener);
            if (seeOnClickListener != null)
                itemView.findViewById(R.id.preview).setOnClickListener(seeOnClickListener);
        }

    }
}
