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
public class IntroCreaLista extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.title_activity_nuova_lista
                , R.string.showcase_welcome_crea
                , R.drawable.intro_crealista_0));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.add_position
                , R.string.showcase_add_pos_desc
                , R.drawable.intro_crealista_1));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.posizione_reorder
                , R.string.showcase_reorder_desc
                , R.drawable.intro_crealista_2));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.posizione_rename
                , R.string.showcase_rename_desc
                , R.drawable.intro_crealista_3));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.posizione_delete
                , R.string.showcase_delete_desc
                , R.drawable.intro_crealista_4));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.list_save_exit
                , R.string.showcase_saveexit_desc
                , R.drawable.intro_crealista_5));
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.showcase_end_title
                , R.string.showcase_help_general
                , R.drawable.intro_crealista_6));
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
