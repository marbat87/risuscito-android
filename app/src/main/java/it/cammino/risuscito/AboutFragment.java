package it.cammino.risuscito;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vansuita.materialabout.builder.AboutBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AboutFragment extends Fragment {

    private MainActivity mMainActivity;
    @BindView(R.id.about) NestedScrollView mScrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
//        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.PreferenceFixTheme_NoActionBar);

        // clone the inflater using the ContextThemeWrapper
//        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View rootView = inflater.inflate(R.layout.about_layout, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        mMainActivity.setupToolbarTitle(R.string.title_activity_about);
        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(false);
            mMainActivity.enableBottombar(false);
        }
        mMainActivity.mTabLayout.setVisibility(View.GONE);

        mScrollView.addView(
                AboutBuilder.with(mMainActivity)
                        .setAppIcon(R.mipmap.ic_launcher)
                        .setAppName(R.string.app_name)
                        .setPhoto(R.drawable.ic_brand_icon)
                        .setCover(R.mipmap.profile_cover)
//                        .setLinksAnimated(false)
//                        .setShowDivider(false)
//                        .setName("MARBAT87")
//                        .setSubTitle("Mobile Developer")
                        .setLinksColumnsCount(1)
                        .setBrief(R.string.promotional_text)
//                        .addGooglePlayStoreLink("8002078663318221363")
//                        .addGitHubLink("jrvansuita")
//                        .addBitbucketLink("jrvansuita")
//                        .addFacebookLink("user")
//                        .addTwitterLink("user")
//                        .addInstagramLink("jnrvans")
//                        .addGooglePlusLink("103588649850838411440")
//                        .addYoutubeChannelLink("103588649850838411440")
//                        .addDribbleLink("user")
//                        .addLinkedinLink("arleu-cezar-vansuita-júnior-83769271")
                        .addEmailLink("marbat87@outlook.it", getString(R.string.app_name), null)
//                        .addWhatsappLink("Jr", "+554799650629")
//                        .addSkypeLink("user")
//                        .addGoogleLink("user")
//                        .addAndroidLink("user")
//                        .addWebsiteLink("site")
                        .addFiveStarsAction(BuildConfig.APPLICATION_ID)
//                        .addMoreFromMeAction("Vansuita")
                        .setVersionNameAsAppSubTitle()
                        .addShareAction(R.string.app_name)
                        .addUpdateAction(BuildConfig.APPLICATION_ID)
                        .setActionsColumnsCount(2)
//                        .addFeedbackAction("vansuita.jr@gmail.com")
//                        .addIntroduceAction((Intent) null)
//                        .addHelpAction((Intent) null)
                        .addChangeLogAction(new Intent(mMainActivity, ChangelogActivity.class))
//                        .addRemoveAdsAction((Intent) null)
//                        .addDonateAction((Intent) null)
                        .build());

        return rootView;

    }

}
