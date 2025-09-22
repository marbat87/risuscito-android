package it.cammino.risuscito.ui.dialog

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.BottomSheetItem
import it.cammino.risuscito.ui.composable.BottomSheetTitle
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.queryIntentActivities

class BottomSheetFragment : BottomSheetDialogFragment() {

    private var titleText: String = "prova"
    private var showTitle: Boolean = false

    private var appList: List<ResolveInfo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
                RisuscitoTheme {
                    Column(Modifier.fillMaxSize()) {
                        if (showTitle) {
                            BottomSheetTitle(titleText)
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 0.dp, 24.dp, 24.dp),
                        ) {
                            items(appList) { app ->
                                BottomSheetItem(
                                    app,
                                    requireActivity().packageManager,
                                    onItemClick = { selectedProduct ->
                                        // Handle item click
                                        // You can navigate to a detail screen, show a dialog, etc.
                                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                                            .edit {
                                                putString(
                                                    Utility.ULTIMA_APP_USATA,
                                                    selectedProduct.activityInfo?.packageName
                                                )
                                            }

                                        val name = ComponentName(
                                            selectedProduct.activityInfo?.packageName.orEmpty(),
                                            selectedProduct.activityInfo?.name.orEmpty()
                                        )

                                        @Suppress("DEPRECATION") val mIntent =
                                            if (OSUtils.hasT()) arguments?.getParcelable(
                                                "intent",
                                                Intent::class.java
                                            ) else arguments?.getParcelable("intent")

                                        val newIntent = mIntent?.clone() as? Intent
                                        newIntent?.component = name
                                        activity?.startActivity(newIntent)
                                        dialog?.dismiss()
                                        true
                                    })
                            }
                        }
                    }


                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showTitle = arguments?.getBoolean("showTitle") == true

        titleText =
            if (showTitle)
                ContextCompat.getString(requireContext(), arguments?.getInt("title") ?: 0)
            else
                StringUtils.EMPTY

        @Suppress("DEPRECATION") val intent = if (OSUtils.hasT()) arguments?.getParcelable(
            "intent",
            Intent::class.java
        ) else arguments?.getParcelable("intent")
        val pm = requireActivity().packageManager

        intent?.let { mIntent ->
            val list = pm.queryIntentActivities(mIntent)

            val lastApp = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getString(Utility.ULTIMA_APP_USATA, StringUtils.EMPTY)
            val lastAppInfo: ResolveInfo? = list.indices
                .firstOrNull { list[it].activityInfo.applicationInfo.packageName == lastApp }
                ?.let { list.removeAt(it) }

            lastAppInfo?.let { list.add(it) }

            appList = list

        }
    }

    override fun onResume() {
        super.onResume()
        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        val mLimited = requireContext().resources.getBoolean(R.bool.is_bottom_sheet_limited)
        if (mLimited) {
            val mMaxWidth =
                requireActivity().resources.getDimension(R.dimen.max_bottomsheet_width).toInt()
            val win = dialog?.window
            win?.setLayout(mMaxWidth, -1)
        }
    }

    companion object {

        fun newInstance(@StringRes title: Int, intent: Intent): BottomSheetFragment {
            val frag = BottomSheetFragment()
            val args = Bundle()
            args.putInt("title", title)
            args.putBoolean("showTitle", true)
            args.putParcelable("intent", intent)
            frag.arguments = args
            return frag
        }
    }

}