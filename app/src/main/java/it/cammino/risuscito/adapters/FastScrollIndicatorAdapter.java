package it.cammino.risuscito.adapters;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import it.cammino.risuscito.items.SimpleItem;

public class FastScrollIndicatorAdapter<Item extends IItem> extends FastItemAdapter<Item> implements ICustomAdapter {

    private int mIndicator;

    public FastScrollIndicatorAdapter(int indicator) {
        super();
        this.mIndicator = indicator;
    }

    @Override
    public String getCustomStringForElement(int position) {
        IItem item = getItem(position);
        if (item instanceof SimpleItem && ((SimpleItem) item).getTitle() != null) {
            //based on the position we set the headers text
            switch (mIndicator) {
                case 0:
                    return ((SimpleItem) item).getTitle().getText().toString().substring(0,1).toUpperCase();
                case 1:
                    return ((SimpleItem) item).getPage().getText().toString();
                case 2:
                    return String.valueOf(((SimpleItem) item).getNumSalmo());
                default:
                    return ((SimpleItem) item).getTitle().getText().toString().substring(0,1).toUpperCase();
            }
        }
        return "";
    }
}