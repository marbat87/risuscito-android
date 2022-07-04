package it.cammino.risuscito.ui.dialog

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.databinding.BottomSheetBinding
import it.cammino.risuscito.items.BottomSheetItem
import it.cammino.risuscito.utils.StringUtils

class BottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showTitle = arguments?.getBoolean("showTitle") ?: false

        if (showTitle)
            binding.sheetTitle.setText(arguments?.getInt("title") ?: 0)
        else
            binding.sheetTitle.text = StringUtils.EMPTY
        binding.sheetTitleArea.isVisible = showTitle

        val intent = arguments?.getParcelable<Intent>("intent")
        val pm = requireActivity().packageManager

        intent?.let { mIntent ->
            val list = pm.queryIntentActivities(mIntent, 0)

            val lastApp = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getString(Utility.ULTIMA_APP_USATA, StringUtils.EMPTY)
            val lastAppInfo: ResolveInfo? = list.indices
                .firstOrNull { list[it].activityInfo.applicationInfo.packageName == lastApp }
                ?.let { list.removeAt(it) }

            lastAppInfo?.let { list.add(0, it) }

            val mList = list.map { BottomSheetItem().withItem(it) }

            val mOnClickListener =
                { _: View?, _: IAdapter<BottomSheetItem>, item: BottomSheetItem, _: Int ->
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
                        putString(
                            Utility.ULTIMA_APP_USATA,
                            item.infoItem?.activityInfo?.packageName
                        )
                    }

                    val name = ComponentName(
                        item.infoItem?.activityInfo?.packageName.orEmpty(),
                        item.infoItem?.activityInfo?.name.orEmpty()
                    )
                    val newIntent = mIntent.clone() as? Intent
                    newIntent?.component = name
                    activity?.startActivity(newIntent)
                    dialog?.dismiss()
                    true
                }

            val adapter = FastItemAdapter<BottomSheetItem>()
            adapter.add(mList)
            adapter.onClickListener = mOnClickListener
            binding.shareList.adapter = adapter
            binding.shareList.layoutManager = GridLayoutManager(activity, 3)
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