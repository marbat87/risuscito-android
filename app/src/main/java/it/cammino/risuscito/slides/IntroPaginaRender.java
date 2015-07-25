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
public class IntroPaginaRender extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.sc_pagina_render_title
                , R.string.sc_pagina_render_desc
                , R.drawable.intro_paginarender_0));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.action_tonalita
                , R.string.sc_tonalita_desc
                , R.drawable.intro_paginarender_1));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.action_barre
                , R.string.sc_barre_desc
                , R.drawable.intro_paginarender_2));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.sc_audio_title
                , R.string.sc_audio_desc
                , R.drawable.intro_paginarender_3));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.sc_scroll_title
                , R.string.sc_scroll_desc
                , R.drawable.intro_paginarender_4));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.showcase_end_title
                , R.string.showcase_help_general
                , R.drawable.intro_paginarender_5));
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
