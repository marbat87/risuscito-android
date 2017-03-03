package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.cammino.risuscito.R;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.ui.ThemeableActivity;

public class CantoRecyclerAdapter extends RecyclerView.Adapter {

    private List<CantoRecycled> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    private Activity context;

    // Adapter constructor 1
    public CantoRecyclerAdapter(Activity activity, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        createContextMenuListener = null;
        this.context = activity;
    }

    // Adapter constructor 2
    public CantoRecyclerAdapter(Activity activity, List<CantoRecycled> dataItems
            , View.OnClickListener clickListener
            , View.OnCreateContextMenuListener createContextMenuListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = null;
        this.createContextMenuListener = createContextMenuListener;
        this.context = activity;
    }

    // Adapter constructor 4
    public CantoRecyclerAdapter(Activity activity
            , List<CantoRecycled> dataItems
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
        cantoHolder.cantoPage.setBackgroundResource(
                context.getResources().getIdentifier("page_oval_border_bkg_" + dataItem.getColore().substring(1).toLowerCase()
                        , "drawable"
                        , context.getPackageName()));

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
}
