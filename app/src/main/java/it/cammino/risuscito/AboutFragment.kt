package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vansuita.materialabout.builder.AboutBuilder
import kotlinx.android.synthetic.main.about_layout.*
import kotlinx.android.synthetic.main.activity_main.*


class AboutFragment : Fragment() {

    private var mMainActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.about_layout, container, false)

        mMainActivity = activity as MainActivity?

        mMainActivity!!.enableFab(false)
//        if (!mMainActivity!!.isOnTablet) {
            mMainActivity!!.enableBottombar(false)
//        }
        activity!!.material_tabs.visibility = View.GONE

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity!!.setupToolbarTitle(R.string.title_activity_about)
        val mDonateClickListener = View.OnClickListener {
            startActivity(Intent(mMainActivity, DonateActivity::class.java))
            activity!!.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on)
        }

        val mChangeLogClickListener = View.OnClickListener {
            startActivity(Intent(mMainActivity, ChangelogActivity::class.java))
            activity!!.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on)
        }

        about!!.addView(
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
                        .build())
    }
}
