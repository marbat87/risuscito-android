package it.cammino.risuscito.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.objects.CantoRecycled;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class CantoSalmoAdapter extends RecyclerView.Adapter implements SectionIndexer {

    private List<CantoRecycled> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    private AppCompatActivity appCompatActivity;

    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;

    // Adapter constructor 1
    public CantoSalmoAdapter(AppCompatActivity activity, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        createContextMenuListener = null;
        init();
    }

    // Adapter constructor 2
    public CantoSalmoAdapter(AppCompatActivity activity, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.createContextMenuListener = null;
        init();
    }

    // Adapter constructor 3
    public CantoSalmoAdapter(AppCompatActivity activity, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnCreateContextMenuListener createContextMenuListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.createContextMenuListener = createContextMenuListener;
        init();
    }

    private void init() {
        alphaIndexer = new HashMap<String, Integer>();
        int size = dataItems.size();
        String prevLetter = " ";

        for (int x = 0; x < size; x++) {
            // get the first letter of the store
//            String ch = "";
//            if (appCompatActivity.getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk")) {
//                try {
//                    ch = String.valueOf(Integer.valueOf(dataItems.get(x).getTitolo().substring(7, 10)));
//                } catch (NumberFormatException | IndexOutOfBoundsException e) {
//                    try {
//                        ch = String.valueOf(Integer.valueOf(dataItems.get(x).getTitolo().substring(7, 9)));
//                    } catch (NumberFormatException | IndexOutOfBoundsException d) {
//                        ch = String.valueOf(Integer.valueOf(dataItems.get(x).getTitolo().substring(7, 8)));
//                    }
//                }
//            }
//            else {
//                try {
//                    ch = String.valueOf(Integer.valueOf(dataItems.get(x).getTitolo().substring(6, 9)));
//                } catch (NumberFormatException | IndexOutOfBoundsException e) {
//                    try {
//                        ch = String.valueOf(Integer.valueOf(dataItems.get(x).getTitolo().substring(6, 8)));
//                    } catch (NumberFormatException | IndexOutOfBoundsException d) {
//                        ch = String.valueOf(Integer.valueOf(dataItems.get(x).getTitolo().substring(6, 7)));
//                    }
//                }
//            }
            String ch = String.valueOf(dataItems.get(x).getNumeroSalmo());

            if (!ch.equals(prevLetter)) {
                // HashMap will prevent duplicates
                alphaIndexer.put(ch, x);
                prevLetter = ch;
            }
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList, new CustomComparator());
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.row_item, viewGroup, false);
        return new CantoViewHolder(layoutView, clickListener, longClickListener, createContextMenuListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CantoRecycled dataItem = dataItems.get(position);

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;
        cantoHolder.cantoTitle.setText(dataItem.getTitolo());
        cantoHolder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
        cantoHolder.cantoId.setText(String.valueOf(dataItem.getIdCanto()));
        cantoHolder.cantoSource.setText(dataItem.getSource());
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
        public TextView cantoId;
        public TextView cantoSource;

        public CantoViewHolder(View itemView
                , View.OnClickListener onClickListener
                , View.OnLongClickListener onLongClickListener
                , View.OnCreateContextMenuListener onCreateContextMenuListener) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            cantoId = (TextView) itemView.findViewById(R.id.text_id_canto);
            cantoSource = (TextView) itemView.findViewById(R.id.text_source_canto);
            if (onClickListener != null)
                itemView.setOnClickListener(onClickListener);
            if (onLongClickListener != null)
                itemView.setOnLongClickListener(onLongClickListener);
            if (onCreateContextMenuListener != null)
                itemView.setOnCreateContextMenuListener(onCreateContextMenuListener);
        }

    }

    @Override
    public String[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        return alphaIndexer.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
//        int minusPosition = dataItems.get(position).getTitolo().indexOf(" - ");
//        String first = dataItems.get(position).getTitolo().substring(6, minusPosition);
        String first = String.valueOf(dataItems.get(position).getNumeroSalmo());
        for (int i = 0; i < sections.length; i++) {
            if (first.equals(sections[i]))
                return i;
        }
        return 0;
    }

    private class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
        }
    }

}
