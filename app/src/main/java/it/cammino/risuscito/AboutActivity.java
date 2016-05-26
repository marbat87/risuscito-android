package it.cammino.risuscito;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.cammino.risuscito.utils.ThemeUtils;


public class AboutActivity extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.activity_about, container, false);
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar));

//		Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.risuscito_toolbar);
//		toolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
//		Drawable drawable = DrawableCompat.wrap(toolbar.getNavigationIcon());
//		DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), android.R.color.white));
////		toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
//		((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

		CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsingToolbarLayout);
//		collapsingToolbarLayout.setTitle(getResources().getString(R.string.title_activity_about));
		collapsingToolbarLayout.setContentScrimColor(getThemeUtils().primaryColor());

		return rootView;
	}

	private ThemeUtils getThemeUtils() {
		return ((MainActivity)getActivity()).getThemeUtils();
	}

}
