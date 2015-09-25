package it.cammino.risuscito.ui;

import android.content.Context;
import android.util.AttributeSet;

import it.cammino.risuscito.objects.CantoRecycled;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

/**
 * Indicator for sections of type {@link CantoRecycled}
 */
public class RisuscitoSectionTitleIndicator extends SectionTitleIndicator<String> {

    public RisuscitoSectionTitleIndicator(Context context) {
        super(context);
    }

    public RisuscitoSectionTitleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RisuscitoSectionTitleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSection(String iniziale) {
        setTitleText(iniziale);
    }

}
