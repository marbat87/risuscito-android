package it.cammino.risuscito.ui.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.color.MaterialColors
import com.vansuita.materialabout.builder.AboutBuilder
import it.cammino.risuscito.BuildConfig
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.AboutLayoutBinding
import it.cammino.risuscito.ui.activity.ChangelogActivity
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.Utility.CLICK_DELAY
import it.cammino.risuscito.utils.extension.slideInRight


class AboutFragment : AccountMenuFragment() {

    private var _binding: AboutLayoutBinding? = null

    private var mLastClickTime: Long = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AboutLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setupToolbarTitle(R.string.title_activity_about)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        val mChangeLogClickListener = View.OnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime >= CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                if (OSUtils.isObySamsung()) {
                    startActivity(Intent(mMainActivity, ChangelogActivity::class.java))
                    mMainActivity?.slideInRight()
                } else {
                    it.transitionName = "shared_element_about"
                    val options = ActivityOptions.makeSceneTransitionAnimation(
                        mMainActivity,
                        it,
                        "shared_element_about" // The transition name to be matched in Activity B.
                    )
                    startActivity(
                        Intent(mMainActivity, ChangelogActivity::class.java),
                        options.toBundle()
                    )
                }
            }
        }

        context?.let {

            val builder = AboutBuilder.with(it).apply {
                setAppIcon(R.drawable.ic_launcher_144dp)
                setAppName(R.string.app_name)
                setPhoto(R.drawable.ic_brand_icon)
                setCover(R.mipmap.profile_cover)
                backgroundColor = MaterialColors.getColor(
                    requireContext(), android.R.attr.colorBackground,
                    TAG
                )
                linksColumnsCount = 1
                addEmailLink("marbat87@outlook.it", getString(R.string.app_name), null)
                addFiveStarsAction(BuildConfig.APPLICATION_ID)
                setVersionNameAsAppSubTitle()
                addShareAction(R.string.app_name)
                addUpdateAction(BuildConfig.APPLICATION_ID)
                actionsColumnsCount = 2
                addChangeLogAction(mChangeLogClickListener)
                addPrivacyPolicyAction("https://marbat87.altervista.org/privacy_policy.html")
                isShowAsCard = false
            }
            val builderView = builder.build()
            builderView.findItem(builder.lastLink).findViewById<AppCompatImageView>(R.id.icon)
                .setImageResource(R.drawable.mail_24px)
            val actions = builder.actions
            builderView.findItem(actions[0]).findViewById<AppCompatImageView>(R.id.icon)
                .setImageResource(R.drawable.star_24px)
            builderView.findItem(actions[1]).findViewById<AppCompatImageView>(R.id.icon)
                .setImageResource(R.drawable.share_24px)
            builderView.findItem(actions[2]).findViewById<AppCompatImageView>(R.id.icon)
                .setImageResource(R.drawable.file_download_24px)
            builderView.findItem(actions[3]).findViewById<AppCompatImageView>(R.id.icon)
                .setImageResource(R.drawable.list_alt_24px)
            builderView.findItem(actions[4]).findViewById<AppCompatImageView>(R.id.icon)
                .setImageResource(R.drawable.policy_24px)
            binding.about.addView(builderView)
        }
    }

    companion object {
        private val TAG = AboutFragment::class.java.canonicalName
    }

}
