package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.elevation.ElevationOverlayProvider
import com.vansuita.materialabout.builder.AboutBuilder
import it.cammino.risuscito.databinding.AboutLayoutBinding


class AboutFragment : Fragment() {

    private var mMainActivity: MainActivity? = null

    private var _binding: AboutLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = AboutLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_about)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        val mChangeLogClickListener = View.OnClickListener {
            startActivity(Intent(mMainActivity, ChangelogActivity::class.java))
            Animatoo.animateSlideUp(activity)
        }

        binding.about.addView(
                AboutBuilder.with(mMainActivity).apply {
                    setAppIcon(R.drawable.ic_launcher_144dp)
                    setAppName(R.string.app_name)
                    setPhoto(R.drawable.ic_brand_icon)
                    setCover(R.mipmap.profile_cover)
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
                    backgroundColor = ElevationOverlayProvider(requireContext()).compositeOverlayWithThemeSurfaceColorIfNeeded(resources.getDimension(R.dimen.mtrl_card_elevation))
                }.build())
    }
}
