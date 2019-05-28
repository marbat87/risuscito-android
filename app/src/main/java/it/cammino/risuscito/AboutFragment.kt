package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.vansuita.materialabout.builder.AboutBuilder
import kotlinx.android.synthetic.main.about_layout.*


class AboutFragment : Fragment() {

    private var mMainActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.about_layout, container, false)

        mMainActivity = activity as? MainActivity

        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)
        mMainActivity?.enableBottombar(false)
        mMainActivity?.setTabVisible(false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setupToolbarTitle(R.string.title_activity_about)

        val mChangeLogClickListener = View.OnClickListener {
            startActivity(Intent(mMainActivity, ChangelogActivity::class.java))
            Animatoo.animateSlideUp(activity)
        }

        about?.addView(
                AboutBuilder.with(mMainActivity)
                        .setAppIcon(R.drawable.ic_launcher_144dp)
                        .setAppName(R.string.app_name)
                        .setPhoto(R.drawable.ic_brand_icon)
                        .setCover(R.mipmap.profile_cover)
                        .setLinksColumnsCount(1)
                        .addEmailLink("marbat87@outlook.it", getString(R.string.app_name), null)
                        .addFiveStarsAction(BuildConfig.APPLICATION_ID)
                        .setVersionNameAsAppSubTitle()
                        .addShareAction(R.string.app_name)
                        .addUpdateAction(BuildConfig.APPLICATION_ID)
                        .setActionsColumnsCount(2)
                        .addChangeLogAction(mChangeLogClickListener)
                        .addPrivacyPolicyAction("http://marbat87.altervista.org/privacy_policy.html")
                        .setShowAsCard(false)
                        .build())
    }
}
