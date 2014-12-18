package it.cammino.risuscito;
 
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.ShareActionProvider;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 
public class HomeAwayShareProvider extends ShareActionProvider {
 
    private final Context mContext;
 
    public HomeAwayShareProvider(Context context) {
        super(context);
        mContext = context;
    }
 
    @Override
    public View onCreateActionView() {
        View chooserView =
                super.onCreateActionView();
 
        // Set your drawable here
        Drawable icon = 
                mContext.getResources().getDrawable(R.drawable.ic_action_share);
        		
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