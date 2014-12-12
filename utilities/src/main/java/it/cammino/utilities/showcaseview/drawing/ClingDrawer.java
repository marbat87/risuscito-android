package it.cammino.utilities.showcaseview.drawing;

import android.graphics.Canvas;

import it.cammino.utilities.showcaseview.utils.ShowcaseAreaCalculator;

/**
 * Created by curraa01 on 13/10/2013.
 */
public interface ClingDrawer extends ShowcaseAreaCalculator {

    //BATM - proper scaling on "<= HONEYCOMB"
//    void drawShowcase(Canvas canvas, float x, float y, float scaleMultiplier, float radius);
    void drawShowcase(Canvas canvas, float x, float y, float radius);

    int getShowcaseWidth();

    int getShowcaseHeight();

}
