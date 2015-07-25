package it.cammino.risuscito.slides;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.cammino.risuscito.R;

/**
 * Created by marcello.battain on 14/07/2015.
 */
public class DefaultSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String ARG_TITLE_RES_ID = "titleResId";
    private static final String ARG_DESC_RES_ID = "descResId";
    private static final String ARG_IMAGE_RES_ID = "imageResId";

    public static DefaultSlide newInstance(int layoutResId, int titleResId, int descResId, int imageResId) {
        DefaultSlide sampleSlide = new DefaultSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putInt(ARG_TITLE_RES_ID, titleResId);
        args.putInt(ARG_DESC_RES_ID, descResId);
        args.putInt(ARG_IMAGE_RES_ID, imageResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    private int layoutResId;
    private int titleResId;
    private int descResId;
    private int imageResId;

    public DefaultSlide() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)
                && getArguments().containsKey(ARG_TITLE_RES_ID)
                && getArguments().containsKey(ARG_DESC_RES_ID)
                && getArguments().containsKey(ARG_IMAGE_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            titleResId = getArguments().getInt(ARG_TITLE_RES_ID);
            descResId = getArguments().getInt(ARG_DESC_RES_ID);
            imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(layoutResId, container, false);

        ((TextView) rootview.findViewById(R.id.intro_title)).setText(titleResId);
        ((TextView) rootview.findViewById(R.id.intro_desc)).setText(descResId);
        ((ImageView) rootview.findViewById(R.id.intro_image)).setImageResource(imageResId);

        return rootview;
    }

}
