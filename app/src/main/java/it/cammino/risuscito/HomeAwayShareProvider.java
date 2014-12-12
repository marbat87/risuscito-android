package it.cammino.risuscito;
 
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.ShareActionProvider;
import android.view.View;
 
/**
 * Created by briangriffey on 4/22/14.
 */
public class HomeAwayShareProvider extends ShareActionProvider {
 
    private final Context mContext;
 
    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public HomeAwayShareProvider(Context context) {
        super(context);
        mContext = context;
    }
 
    @Override
    public View onCreateActionView() {
        View chooserView =
                super.onCreateActionView();
 
//        int[] attrs = new int[] { R.attr.customShare /* index 0 */};
//        TypedArray ta = mContext.obtainStyledAttributes(attrs);
        // Set your drawable here
        Drawable icon = 
                mContext.getResources().getDrawable(R.drawable.ic_action_share);
        		
//        		ta.getDrawable(0 /* index */);       
//        ta.recycle();
        
        Class clazz = chooserView.getClass();
 
        //reflect all of this shit so that I can change the icon
        try {
            Method method = clazz.getMethod("setExpandActivityOverflowButtonDrawable", Drawable.class);
            method.invoke(chooserView, icon);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
 
        return chooserView;
    }
}