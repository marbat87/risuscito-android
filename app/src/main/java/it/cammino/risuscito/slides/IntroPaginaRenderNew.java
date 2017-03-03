package it.cammino.risuscito.slides;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import it.cammino.risuscito.R;

public class IntroPaginaRenderNew extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
//                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultDescriptionTypefacePath("fonts/Roboto-Regular.ttf")
                .defaultBackgroundColor(R.color.intro_background)
                .page(new BasicPage(R.drawable.intro_paginarender_0, getString(R.string.sc_pagina_render_title), getString(R.string.sc_pagina_render_desc)))
                .page(new BasicPage(R.drawable.intro_paginarender_1, getString(R.string.action_tonalita), getString(R.string.sc_tonalita_desc)))
                .page(new BasicPage(R.drawable.intro_paginarender_2, getString(R.string.action_barre), getString(R.string.sc_barre_desc)))
                .page(new BasicPage(R.drawable.intro_paginarender_3, getString(R.string.sc_audio_title), getString(R.string.sc_audio_desc)))
                .page(new BasicPage(R.drawable.intro_paginarender_4, getString(R.string.sc_scroll_title), getString(R.string.sc_scroll_desc)))
                .page(new BasicPage(R.drawable.intro_paginarender_5, getString(R.string.showcase_end_title), getString(R.string.showcase_help_general)))
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroPaginaRenderNew";
    }

}
