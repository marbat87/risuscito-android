package it.cammino.risuscito.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.objects.CantoRecycled;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class CantoAdapter extends RecyclerView.Adapter implements ICustomAdapter {

    private List<CantoRecycled> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    private int mMode;

//    private HashMap<String, Integer> alphaIndexer;
//    private String[] sections;

    // Adapter constructor 1
    public CantoAdapter(int mMode, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        createContextMenuListener = null;
        this.mMode = mMode;
//        init();
    }

    // Adapter constructor 2
    public CantoAdapter(int mMode, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.createContextMenuListener = null;
        this.mMode = mMode;
//        init();
    }

    // Adapter constructor 3
    public CantoAdapter(int mMode, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnCreateContextMenuListener createContextMenuListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.createContextMenuListener = createContextMenuListener;
        this.mMode = mMode;
//        init();
    }

//    private void init() {
//        alphaIndexer = new HashMap<String, Integer>();
//        int size = dataItems.size();
//        String prevLetter = " ";
//
//        for (int x = 0; x < size; x++) {
//            // get the first letter of the store
//            // convert to uppercase otherwise lowercase a -z will be sorted after upper A-Z
//            String ch = dataItems.get(x).getTitolo().substring(0, 1).toUpperCase();
//
//            if (!ch.equals(prevLetter)) {
//                // HashMap will prevent duplicates
//                alphaIndexer.put(ch, x);
//                prevLetter = ch;
//            }
//        }
//
//        Set<String> sectionLetters = alphaIndexer.keySet();
//        // create a list from the set to sort
//        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);
//        Collections.sort(sectionList);
//        sections = new String[sectionList.size()];
//        sectionList.toArray(sections);
//    }

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

//    @Override
//    public String[] getSections() {
//        return sections;
//    }
//
//    @Override
//    public int getPositionForSection(int section) {
//        return alphaIndexer.get(sections[section]);
//    }
//
//    @Override
//    public int getSectionForPosition(int position) {
//        String first = dataItems.get(position).getTitolo().substring(0, 1).toUpperCase();
//        for (int i = 0; i < sections.length; i++) {
//            if (first.equals(sections[i]))
//                return i;
//        }
//        return 0;
//    }

    @Override
    public String getCustomStringForElement(int i) {
        switch (mMode) {
            case 0:
                return dataItems.get(i).getTitolo().toUpperCase().substring(0,1);
            case 1:
                int pagina = dataItems.get(i).getPagina();
                return String.valueOf(pagina);
            case 2:
                int salmo = dataItems.get(i).getNumeroSalmo();
                return String.valueOf(salmo);
            default:
                return dataItems.get(i).getTitolo().toUpperCase().substring(0,1);
        }
    }

}
