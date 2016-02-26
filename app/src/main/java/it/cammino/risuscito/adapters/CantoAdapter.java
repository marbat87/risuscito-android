package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.List;

import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.R;
import it.cammino.risuscito.objects.CantoRecycled;

public class CantoAdapter extends RecyclerView.Adapter implements ICustomAdapter {

    private List<CantoRecycled> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    private int mMode;
    private Activity context;

//    private HashMap<String, Integer> alphaIndexer;
//    private String[] sections;

    // Adapter constructor 1
    public CantoAdapter(Activity activity, int mMode, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        createContextMenuListener = null;
        this.mMode = mMode;
        this.context = activity;
//        init();
    }

    // Adapter constructor 2
    public CantoAdapter(Activity activity, int mMode, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.createContextMenuListener = null;
        this.mMode = mMode;
        this.context = activity;
//        init();
    }

    // Adapter constructor 3
    public CantoAdapter(Activity activity, int mMode, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnCreateContextMenuListener createContextMenuListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.createContextMenuListener = createContextMenuListener;
        this.mMode = mMode;
        this.context = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.row_item, viewGroup, false);
        return new CantoViewHolder(layoutView, clickListener, longClickListener, createContextMenuListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CantoRecycled dataItem = dataItems.get(position);

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;
        cantoHolder.cantoTitle.setText(dataItem.getTitolo());
        cantoHolder.cantoPage.setText(String.valueOf(dataItem.getPagina()));
        cantoHolder.cantoId.setText(String.valueOf(dataItem.getIdCanto()));
        cantoHolder.cantoSource.setText(dataItem.getSource());
        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.page_oval_bkg));
        DrawableCompat.setTint(drawable, Color.parseColor(dataItem.getColore()));
        if (LUtils.hasJB())
            cantoHolder.cantoPage.setBackground(drawable);
        else
            cantoHolder.cantoPage.setBackgroundDrawable(drawable);
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
