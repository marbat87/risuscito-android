package it.cammino.risuscito.ui

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import it.cammino.risuscito.R

@Suppress("unused")
class BottomSheetFabListe : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_fablist, container, false)

        val showTitle = arguments!!.getBoolean("showTitle")
        val titleView = view.findViewById<TextView>(R.id.sheet_title)
        if (showTitle) {
            titleView.setText(arguments!!.getInt("title"))
            view.findViewById<View>(R.id.sheet_title_area).visibility = View.VISIBLE
        } else {
            view.findViewById<View>(R.id.sheet_title_area).visibility = View.GONE
        }

        val iconColorId = R.color.text_color_secondary

        var mView = view.findViewById<View>(R.id.fab_pulisci)
        var mImage = mView.findViewById<ImageView>(R.id.app_icon)
        var icon = IconicsDrawable(activity!!)
                .icon(CommunityMaterial.Icon.cmd_eraser_variant)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        var mTextView = mView.findViewById<TextView>(R.id.app_label)
        mTextView.setText(R.string.button_clean_list)
        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + CLEAN)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, CLEAN)
            activity!!.sendBroadcast(intentBroadcast)
        }

        mView = view.findViewById(R.id.fab_condividi)
        mImage = mView.findViewById(R.id.app_icon)
        icon = IconicsDrawable(activity!!)
                .icon(CommunityMaterial.Icon.cmd_share_variant)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        mTextView = mView.findViewById(R.id.app_label)
        mTextView.setText(R.string.action_share)
        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + SHARE_TEXT)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, SHARE_TEXT)
            activity!!.sendBroadcast(intentBroadcast)
        }

        mView = view.findViewById(R.id.fab_add_lista)
        mImage = mView.findViewById(R.id.app_icon)
        icon = IconicsDrawable(activity!!)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        mTextView = mView.findViewById(R.id.app_label)
        mTextView.setText(R.string.action_add_list)
        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + ADD_LIST)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, ADD_LIST)
            activity!!.sendBroadcast(intentBroadcast)
        }

        val customList = arguments!!.getBoolean("customList", false)

        if (customList) {
            view.findViewById<View>(R.id.custom_list_option).visibility = View.VISIBLE

            mView = view.findViewById(R.id.fab_condividi_file)
            mImage = mView.findViewById(R.id.app_icon)
            icon = IconicsDrawable(activity!!)
                    .icon(CommunityMaterial.Icon.cmd_attachment)
                    .color(ContextCompat.getColor(context!!, iconColorId))
                    .sizeDp(48)
                    .paddingDp(4)
            mImage.setImageDrawable(icon)
            mTextView = mView.findViewById(R.id.app_label)
            mTextView.setText(R.string.action_share_file)
            mView.setOnClickListener {
                dialog.dismiss()
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
                Log.d(TAG, "clicked id: " + SHARE_FILE)
                val intentBroadcast = Intent(CHOOSE_DONE)
                intentBroadcast.putExtra(DATA_ITEM_ID, SHARE_FILE)
                activity!!.sendBroadcast(intentBroadcast)
            }

            mView = view.findViewById(R.id.fab_edit_lista)
            mImage = mView.findViewById(R.id.app_icon)
            icon = IconicsDrawable(activity!!)
                    .icon(CommunityMaterial.Icon.cmd_pencil)
                    .color(ContextCompat.getColor(context!!, iconColorId))
                    .sizeDp(48)
                    .paddingDp(4)
            mImage.setImageDrawable(icon)
            mTextView = mView.findViewById(R.id.app_label)
            mTextView.setText(R.string.action_edit_list)
            mView.setOnClickListener {
                dialog.dismiss()
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
                Log.d(TAG, "clicked id: " + EDIT_LIST)
                val intentBroadcast = Intent(CHOOSE_DONE)
                intentBroadcast.putExtra(DATA_ITEM_ID, EDIT_LIST)
                activity!!.sendBroadcast(intentBroadcast)
            }

            mView = view.findViewById(R.id.fab_delete_lista)
            mImage = mView.findViewById(R.id.app_icon)
            icon = IconicsDrawable(activity!!)
                    .icon(CommunityMaterial.Icon.cmd_delete)
                    .color(ContextCompat.getColor(context!!, iconColorId))
                    .sizeDp(48)
                    .paddingDp(4)
            mImage.setImageDrawable(icon)
            mTextView = mView.findViewById(R.id.app_label)
            mTextView.setText(R.string.action_remove_list)
            mView.setOnClickListener {
                dialog.dismiss()
                Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
                Log.d(TAG, "clicked id: " + DELETE_LIST)
                val intentBroadcast = Intent(CHOOSE_DONE)
                intentBroadcast.putExtra(DATA_ITEM_ID, DELETE_LIST)
                activity!!.sendBroadcast(intentBroadcast)
            }
        }

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
        private val TAG = BottomSheetFabListe::class.java.canonicalName
        val CHOOSE_DONE = "choose_done"
        val DATA_ITEM_ID = "item_id"

        val CLEAN = 1
        val SHARE_TEXT = 3
        val SHARE_FILE = 4
        val ADD_LIST = 2
        val EDIT_LIST = 5
        val DELETE_LIST = 6

        fun newInstance(customList: Boolean): BottomSheetFabListe {
            val frag = BottomSheetFabListe()
            val args = Bundle()
            args.putBoolean("showTitle", false)
            args.putBoolean("customList", customList)
            frag.arguments = args
            return frag
        }

        fun newInstance(@StringRes title: Int, customList: Boolean): BottomSheetFabListe {
            val frag = BottomSheetFabListe()
            val args = Bundle()
            args.putInt("title", title)
            args.putBoolean("showTitle", true)
            args.putBoolean("customList", customList)
            frag.arguments = args
            return frag
        }
    }

}