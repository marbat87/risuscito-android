package it.cammino.risuscito.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rey.material.widget.CheckBox;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.objects.Canto;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class CantoSelezionabileAdapter extends RecyclerView.Adapter {

    private List<Canto> dataItems;

    // Adapter constructor 1
    public CantoSelezionabileAdapter(List<Canto> dataItems) {
        this.dataItems = dataItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.checkable_row_item, viewGroup, false);
        return new CantoViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        Canto dataItem = dataItems.get(position);
        final int pos = position;

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        final CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;
        cantoHolder.cantoTitle.setText(dataItem.getTitolo());
        cantoHolder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
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

        cantoHolder.checkBox.setCheckedImmediately(dataItem.isSelected());
        cantoHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataItems.get(pos).setSelected(cantoHolder.checkBox.isChecked());
            }
        });

        cantoHolder.wholeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cantoHolder.checkBox.setChecked(!cantoHolder.checkBox.isChecked());
                dataItems.get(pos).setSelected(cantoHolder.checkBox.isChecked());
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class CantoViewHolder extends RecyclerView.ViewHolder {

        public TextView cantoTitle;
        public TextView cantoPage;
        public CheckBox checkBox;
        private View wholeView;


        public CantoViewHolder(View itemView) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
            wholeView = itemView.findViewById(R.id.checkable_bck);
        }

    }

    // method to access in activity after updating selection
    public List<Canto> getCantiChoose() {
        return dataItems;
    }
}
