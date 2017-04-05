package it.cammino.risuscito.adapters;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import it.cammino.risuscito.items.CantoItem;

public class CantoBubbleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

    int mBubble;

    public CantoBubbleAdapter(List<AbstractFlexibleItem> items, Object listeners, int bubble) {
        super(items, listeners, true);
        this.mBubble = bubble;
    }

    @Override
    public String onCreateBubbleText(int position) {
        if (getItem(position) instanceof CantoItem) {
            CantoItem mItem = (CantoItem) getItem(position);
            switch (mBubble) {
                case 0:
                    return mItem.getTitle().toUpperCase().substring(0,1);
                case 1:
                    return mItem.getPage();
                case 2:
                    int salmo = mItem.getNumeroSalmo();
                    return String.valueOf(salmo);
                default:
                    return mItem.getTitle().toUpperCase().substring(0,1);
            }
        }
        return super.onCreateBubbleText(position);
    }
}
