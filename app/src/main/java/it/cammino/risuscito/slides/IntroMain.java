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
public class IntroMain extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(DefaultSlide.newInstance(R.layout.intro_defaut_layout
                , R.string.help_new_menu_title
                , R.string.help_new_menu_desc
                , R.drawable.intro_drawer));
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
