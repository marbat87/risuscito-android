package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.ui.activity.ChangelogActivity
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.ui.composable.views.AppInfoScreen
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.startActivityWithTransition
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.WebViewDialogManagerViewModel


class AboutFragment : Fragment() {

    private val webViewDialogManagerViewModel: WebViewDialogManagerViewModel by activityViewModels()

    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()

    private var mMainActivity: ThemeableActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "Fragment: ${this::class.java.canonicalName}")
        Firebase.crashlytics.log("Fragment: ${this::class.java}")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mMainActivity = activity as? ThemeableActivity

        mMainActivity?.setTabVisible(false)
        mMainActivity?.initFab(enable = false)
        mMainActivity?.createOptionsMenu(
            emptyList(),
            null
        )

        return ComposeView(requireContext()).apply {

            setContent {

                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: "1.0.0"
                val versionCode = OSUtils.getVersionCode(context)

                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                val listModifier = Modifier
                    .fillMaxSize()
                    .then(
                        scrollBehaviorFromSharedVM?.let {
                            Modifier.nestedScroll(
                                it.nestedScrollConnection
                            )
                        }
                            ?: Modifier
                    )

                AppInfoScreen(
                    versionName = versionName,
                    versionCode = versionCode,
                    modifier = listModifier,
                    onChangelogClick = {
                        mMainActivity?.startActivityWithTransition(
                            Intent(
                                context, ChangelogActivity::class.java
                            )
                        )
                    },
                    onPrivacyClick = {
                        webViewDialogManagerViewModel.showWebViewDialog.value = true
                    }
                )
            }
        }
    }

    companion object {
        internal val TAG = AboutFragment::class.java.canonicalName
    }

}
