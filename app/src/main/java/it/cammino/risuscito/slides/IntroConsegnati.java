package it.cammino.risuscito.slides;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;

import it.cammino.risuscito.MainActivity;
import it.cammino.risuscito.R;

/**
 * Created by marcello.battain on 14/07/2015.
 */
public class IntroConsegnati extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.title_activity_consegnati
                , R.string.showcase_consegnati_desc
                , R.drawable.intro_consegnati_0));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.title_activity_consegnati
                , R.string.showcase_consegnati_howto
                , R.drawable.intro_consegnati_1));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.title_activity_consegnati
                , R.string.showcase_consegnati_confirm
                , R.drawable.intro_consegnati_2));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.title_activity_consegnati
                , R.string.showcase_consegnati_cancel
                , R.drawable.intro_consegnati_3));
//        addSlide(DefaultSlide.newInstance(R.layout.intro_paginarender_1));
//        addSlide(DefaultSlide.newInstance(R.layout.intro_paginarender_2));
//        addSlide(DefaultSlide.newInstance(R.layout.intro_paginarender_3));
//        addSlide(DefaultSlide.newInstance(R.layout.intro_paginarender_4));
//        addSlide(DefaultSlide.newInstance(R.layout.intro_paginarender_5));
    }

    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed() {
//        loadMainActivity();
        finish();
    }

    public void getStarted(View v){
//        loadMainActivity();
    }
}
