package it.cammino.risuscito.ui;

import android.content.Context;
import android.util.AttributeSet;

import it.cammino.risuscito.objects.CantoRecycled;
import xyz.danoz.recyclerviewfastscroller.sample.data.ColorGroup;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

/**
 * Indicator for sections of type {@link it.cammino.risuscito.objects.CantoRecycled}
 */
public class ColorGroupSectionTitleIndicator extends SectionTitleIndicator<CantoRecycled> {

    public ColorGroupSectionTitleIndicator(Context context) {
        super(context);
    }

    public ColorGroupSectionTitleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorGroupSectionTitleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSection(CantoRecycled canto) {
        // Example of using a single character
        setTitleText(canto.getTitolo().charAt(0) + "");

        // Example of using a longer string
        // setTitleText(colorGroup.getName());

//        setIndicatorTextColor(colorGroup.getAsColor());
    }

}
