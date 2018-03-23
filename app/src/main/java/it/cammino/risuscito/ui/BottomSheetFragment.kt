package it.cammino.risuscito.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.DialogFragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.content.edit
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.OnClickListener
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.items.BottomSheetItem
import kotlinx.android.synthetic.main.bottom_sheet.*

@Suppress("unused")
class BottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet, container, false)

        val showTitle = arguments!!.getBoolean("showTitle")

        if (showTitle)
            sheet_title.setText(arguments!!.getInt("title"))
        else
            sheet_title.text = ""
        sheet_title_area.visibility = if (showTitle) View.VISIBLE else View.GONE

        val intent = arguments!!.getParcelable<Intent>("intent")
        val pm = activity!!.packageManager

        val list = pm.queryIntentActivities(intent, 0)

        val lastApp = PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getString(Utility.ULTIMA_APP_USATA, "")
        val lastAppInfo: ResolveInfo? = list.indices
                .firstOrNull { list[it].activityInfo.applicationInfo.packageName == lastApp }
                ?.let { list.removeAt(it) }

        if (lastAppInfo != null)
            list.add(0, lastAppInfo)

        val mList = list.map { BottomSheetItem().withItem(it) }

        val mOnClickListener = OnClickListener<BottomSheetItem> { _, _, item, _ ->
            PreferenceManager.getDefaultSharedPreferences(activity).edit { putString(Utility.ULTIMA_APP_USATA, item.item!!.activityInfo.packageName) }

            val name = ComponentName(item.item!!.activityInfo.packageName, item.item!!.activityInfo.name)
            if (intent != null) {
                val newIntent = intent.clone() as Intent
                newIntent.component = name
                activity!!.startActivity(newIntent)
                dialog.dismiss()
            }
            true
        }

        val adapter = FastItemAdapter<BottomSheetItem>()
        adapter.add(mList)
        adapter.withOnClickListener(mOnClickListener)
        shareList.adapter = adapter
        shareList.layoutManager = GridLayoutManager(activity, 3)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        val mLimited = activity!!.resources.getBoolean(R.bool.is_bottom_sheet_limited)
        if (mLimited) {
            val mMaxWidth = activity!!.resources.getDimension(R.dimen.max_bottomsheet_width).toInt()
            val win = dialog.window
            win?.setLayout(mMaxWidth, -1)
        }
    }

    companion object {

        fun newInstance(intent: Intent): BottomSheetFragment {
            val frag = BottomSheetFragment()
            val args = Bundle()
            args.putBoolean("showTitle", false)
            args.putParcelable("intent", intent)
            frag.arguments = args
            return frag
        }

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