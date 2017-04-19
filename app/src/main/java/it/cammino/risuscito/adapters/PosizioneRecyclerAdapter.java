package it.cammino.risuscito.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.materialize.holder.ColorHolder;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.R;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.objects.PosizioneTitleItem;

public class PosizioneRecyclerAdapter extends RecyclerView.Adapter {

    //    private List<PosizioneItem> dataItems;
    private List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    //    private Activity context;
    private ColorHolder mColor;


    // Adapter constructor 1
//    public PosizioneRecyclerAdapter(Activity activity, List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems
    public PosizioneRecyclerAdapter(@ColorInt int selectedColor, List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
//        this.context = activity;
        this.mColor = ColorHolder.fromColor(selectedColor);

    }

    // Adapter constructor 2
//    public PosizioneRecyclerAdapter(Activity activity, List<Pair<PosizioneTitleItem, List<PosizioneItem>>> dataItems
//            , View.OnClickListener clickListener) {
//
//        this.dataItems = dataItems;
//        this.clickListener = clickListener;
//        this.longClickListener = null;
//        this.context = activity;
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.generic_list_item, viewGroup, false);
        return new TitleViewHolder(layoutView);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        //get the context
        Context context = viewHolder.itemView.getContext();

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
                itemView = inflater.inflate(R.layout.generic_card_item, titleHolder.list, false);

                TextView cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
                TextView cantoPage = (TextView) itemView.findViewById(R.id.text_page);
                View cantoPageSelected = itemView.findViewById(R.id.selected_mark);
                TextView idCanto = (TextView) itemView.findViewById(R.id.text_id_canto_card);
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
                UIUtils.setBackground(cantoView, FastAdapterUIUtils.getSelectableBackground(context, ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.ripple_color), true));
//                cantoPage.setBackgroundResource(
//                        context.getResources().getIdentifier("page_oval_border_bkg_" + canto.getColore().substring(1).toLowerCase()
//                                , "drawable"
//                                , context.getPackageName()));
//                GradientDrawable bgShape = (GradientDrawable) cantoPage.getBackground();
//                bgShape.setColor(Color.parseColor(canto.getColore()));
//                if (context != null) {
                if (canto.ismSelected()) {
                    //                        cantoView.setBackgroundColor(((ThemeableActivity) context).getThemeUtils().accentColorLight());
                    cantoPage.setVisibility(View.INVISIBLE);
                    cantoPageSelected.setVisibility(View.VISIBLE);
                    GradientDrawable bgShape = (GradientDrawable) cantoPageSelected.getBackground();
                    bgShape.setColor(mColor.getColorInt());
                    cantoView.setSelected(true);
//                        cantoView.setBackgroundColor(ContextCompat.getColor(context, R.color.ripple_color));
//                        cantoView.setSelected();
                }
                else {
                    GradientDrawable bgShape = (GradientDrawable) cantoPage.getBackground();
                    bgShape.setColor(Color.parseColor(canto.getColore()));
                    cantoPage.setVisibility(View.VISIBLE);
                    cantoPageSelected.setVisibility(View.INVISIBLE);
//                        TypedValue typedValue = new TypedValue();
//                        Resources.Theme theme = context.getTheme();
//                        theme.resolveAttribute(R.attr.customSelector, typedValue, true);
//                        cantoView.setBackgroundResource(typedValue.resourceId);
//                        Drawable drawable = FastAdapterUIUtils.getRippleDrawable(Color.TRANSPARENT, ContextCompat.getColor(context, R.color.ripple_color), 10);
//                        UIUtils.setBackground(cantoView, drawable);
                    cantoView.setSelected(false);
                }

//                }
                if (clickListener != null)
                    cantoView.setOnClickListener(clickListener);
                if (longClickListener != null)
                    cantoView.setOnLongClickListener(longClickListener);
                titleHolder.list.addView(itemView);
            }
        }
        else {
            titleHolder.addCanto.setVisibility(View.VISIBLE);
//            Drawable drawable = DrawableCompat.wrap(titleHolder.plusImage.getDrawable());
//            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.icon_ative_black));
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

    static class TitleViewHolder extends RecyclerView.ViewHolder {

//        public TextView idLista;
//        public TextView idPosizione;
//        public TextView nomePosizione;
//        public View addCanto;
        //        public View canto;
//        public View cardView;
//        public TextView tag;
//        public LinearLayout list;
//        public ImageView plusImage;

        @BindView(R.id.text_id_lista) TextView idLista;
        @BindView(R.id.text_id_posizione) TextView idPosizione;
        @BindView(R.id.titoloPosizioneGenerica) TextView nomePosizione;
        @BindView(R.id.addCantoGenerico) View addCanto;
        @BindView(R.id.tag) TextView tag;
        @BindView(R.id.list) LinearLayout list;

        TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            idLista = (TextView) itemView.findViewById(R.id.text_id_lista);
//            idPosizione = (TextView) itemView.findViewById(R.id.text_id_posizione);
//            nomePosizione = (TextView) itemView.findViewById(R.id.titoloPosizioneGenerica);
//            addCanto = itemView.findViewById(R.id.addCantoGenerico);
//            cardView = itemView.findViewById(R.id.cardView);
//            tag = (TextView) itemView.findViewById(R.id.tag);
//            list = (LinearLayout) itemView.findViewById(R.id.list);
//            plusImage = (ImageView) itemView.findViewById(R.id.imageViewGenerica);
        }

    }

}
