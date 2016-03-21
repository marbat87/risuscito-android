package it.cammino.risuscito.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.cammino.risuscito.LUtils;
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

//    // Adapter constructor 2
//    public CantoRecyclerAdapter(List<CantoRecycled> dataItems
//            , View.OnClickListener clickListener
//            , View.OnLongClickListener longClickListener) {
//
//        this.dataItems = dataItems;
//        this.clickListener = clickListener;
//        this.longClickListener = longClickListener;
//        this.createContextMenuListener = null;
//    }

    // Adapter constructor 3
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
        Drawable drawable = ContextCompat.getDrawable(context,
                context.getResources().getIdentifier("page_oval__border_bkg_" + dataItem.getColore().substring(1).toLowerCase(), "drawable", context.getPackageName()));
//        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.page_oval_bkg));
//        DrawableCompat.setTint(drawable, Color.parseColor(dataItem.getColore()));
        if (LUtils.hasJB())
            cantoHolder.cantoPage.setBackground(drawable);
        else
            cantoHolder.cantoPage.setBackgroundDrawable(drawable);

//        if (dataItem.getColore().equalsIgnoreCase(Utility.GIALLO))
//            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_yellow);
//        if (dataItem.getColore().equalsIgnoreCase(Utility.GRIGIO))
//            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_grey);
//        if (dataItem.getColore().equalsIgnoreCase(Utility.VERDE))
//            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_green);
//        if (dataItem.getColore().equalsIgnoreCase(Utility.AZZURRO))
//            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_blue);
//        if (dataItem.getColore().equalsIgnoreCase(Utility.BIANCO))
//            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_white);

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
