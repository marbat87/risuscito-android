package it.cammino.risuscito;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vansuita.materialabout.builder.AboutBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class AboutFragment extends Fragment {

    private MainActivity mMainActivity;
    @BindView(R.id.about) NestedScrollView mScrollView;

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_layout, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        mMainActivity.setupToolbarTitle(R.string.title_activity_about);
        mMainActivity.enableFab(false);
        if (!mMainActivity.isOnTablet()) {
//            mMainActivity.enableFab(false);
            mMainActivity.enableBottombar(false);
        }
        mMainActivity.mTabLayout.setVisibility(View.GONE);

        View.OnClickListener mDonateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mMainActivity, DonateActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
            }
        };

        View.OnClickListener mChangeLogClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mMainActivity, ChangelogActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
            }
        };

        mScrollView.addView(
                AboutBuilder.with(mMainActivity)
                        .setAppIcon(R.drawable.ic_launcher_144dp)
                        .setAppName(R.string.app_name)
                        .setPhoto(R.drawable.ic_brand_icon)
                        .setCover(R.mipmap.profile_cover)
//                        .setLinksAnimated(false)
//                        .setShowDivider(false)
//                        .setName("MARBAT87")
//                        .setSubTitle("Mobile Developer")
                        .setLinksColumnsCount(1)
//                        .setBrief(R.string.promotional_text)
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
                        .addChangeLogAction(mChangeLogClickListener)
//                        .addRemoveAdsAction((Intent) null)
                        .addDonateAction(mDonateClickListener)
                        .addPrivacyPolicyAction("http://marbat87.altervista.org/privacy_policy.html")
                        .setShowAsCard(false)
                        .build());

        return rootView;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
