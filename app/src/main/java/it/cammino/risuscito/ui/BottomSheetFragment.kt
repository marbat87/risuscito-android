package it.cammino.risuscito.ui

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
import it.cammino.risuscito.Utility
import it.cammino.risuscito.items.BottomSheetItem
import kotlinx.android.synthetic.main.bottom_sheet.*

class BottomSheetFragment : BottomSheetDialogFragment() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showTitle = arguments?.getBoolean("showTitle") ?: false

        if (showTitle)
            sheet_title.setText(arguments?.getInt("title") ?: 0)
        else
            sheet_title.text = ""
        sheet_title_area.isVisible = showTitle

        val intent = arguments?.getParcelable<Intent>("intent")
        val pm = requireActivity().packageManager

        intent?.let { mIntent ->
            val list = pm.queryIntentActivities(mIntent, 0)

            val lastApp = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString(Utility.ULTIMA_APP_USATA, "")
            val lastAppInfo: ResolveInfo? = list.indices
                    .firstOrNull { list[it].activityInfo.applicationInfo.packageName == lastApp }
                    ?.let { list.removeAt(it) }

            lastAppInfo?.let { list.add(0, it) }

            val mList = list.map { BottomSheetItem().withItem(it) }

            val mOnClickListener = { _: View?, _: IAdapter<BottomSheetItem>, item: BottomSheetItem, _: Int ->
                PreferenceManager.getDefaultSharedPreferences(context).edit { putString(Utility.ULTIMA_APP_USATA, item.item?.activityInfo?.packageName) }

                val name = ComponentName(item.item?.activityInfo?.packageName
                        ?: "", item.item?.activityInfo?.name ?: "")
                val newIntent = mIntent.clone() as? Intent
                newIntent?.component = name
                activity?.startActivity(newIntent)
                dialog?.dismiss()
                true
            }

            val adapter = FastItemAdapter<BottomSheetItem>()
            adapter.add(mList)
            adapter.onClickListener = mOnClickListener
            shareList.adapter = adapter
            shareList.layoutManager = GridLayoutManager(activity, 3)
        }
    }

    override fun onResume() {
        super.onResume()
        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        val mLimited = requireContext().resources.getBoolean(R.bool.is_bottom_sheet_limited)
        if (mLimited) {
            val mMaxWidth = requireActivity().resources.getDimension(R.dimen.max_bottomsheet_width).toInt()
            val win = dialog?.window
            win?.setLayout(mMaxWidth, -1)
        }
    }

    companion object {

//        fun newInstance(intent: Intent): BottomSheetFragment {
//            val frag = BottomSheetFragment()
//            val args = Bundle()
//            args.putBoolean("showTitle", false)
//            args.putParcelable("intent", intent)
//            frag.arguments = args
//            return frag
//        }

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