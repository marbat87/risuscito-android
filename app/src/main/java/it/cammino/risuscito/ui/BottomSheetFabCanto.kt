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
class BottomSheetFabCanto : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_fabcanto, container, false)

        val showTitle = arguments!!.getBoolean("showTitle")
        val mSound = arguments!!.getBoolean("sound")
        val mDownload = arguments!!.getBoolean("download")
        val mFavorite = arguments!!.getBoolean("favorite")
        val mOnlineUrl = arguments!!.getBoolean("onlineUrl")
        val mPersonalUrl = arguments!!.getBoolean("personalUrl")

        val titleView = view.findViewById<TextView>(R.id.sheet_title)
        if (showTitle) {
            titleView.setText(arguments!!.getInt("title"))
            view.findViewById<View>(R.id.sheet_title_area).visibility = View.VISIBLE
        } else {
            view.findViewById<View>(R.id.sheet_title_area).visibility = View.GONE
        }

        val iconColorId = R.color.text_color_secondary

        var mView = view.findViewById<View>(R.id.fab_fullscreen_on)
        var mImage = mView.findViewById<ImageView>(R.id.app_icon)
        var icon = IconicsDrawable(activity!!)
                .icon(CommunityMaterial.Icon.cmd_fullscreen)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        var mTextView = mView.findViewById<TextView>(R.id.app_label)
        mTextView.setText(R.string.fullscreen)
        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + FULLSCREEN)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, FULLSCREEN)
            activity!!.sendBroadcast(intentBroadcast)
        }

        mView = view.findViewById(R.id.fab_sound_off)
        mImage = mView.findViewById(R.id.app_icon)
        icon = IconicsDrawable(activity!!)
                .icon(if (mSound) CommunityMaterial.Icon.cmd_headset_off else CommunityMaterial.Icon.cmd_headset)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        mTextView = mView.findViewById(R.id.app_label)
        mTextView.setText(if (mSound) R.string.audio_off else R.string.audio_on)
        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + SOUND)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, SOUND)
            activity!!.sendBroadcast(intentBroadcast)
        }

        mView = view.findViewById(R.id.save_file)
        mImage = mView.findViewById(R.id.app_icon)
        icon = IconicsDrawable(activity!!)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        mTextView = mView.findViewById(R.id.app_label)

        if (mDownload) {
            if (mPersonalUrl) {
                icon.icon(CommunityMaterial.Icon.cmd_link_variant_off)
                mTextView.setText(R.string.dialog_delete_link_title)
            } else {
                icon.icon(CommunityMaterial.Icon.cmd_delete)
                mTextView.setText(R.string.fab_delete_unlink)
            }
        } else {
            if (mOnlineUrl) {
                icon.icon(CommunityMaterial.Icon.cmd_download)
                mTextView.setText(R.string.save_file)
            } else {
                icon.icon(CommunityMaterial.Icon.cmd_link_variant)
                mTextView.setText(R.string.only_link_title)
            }
        }

        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + SAVE_FILE)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, SAVE_FILE)
            activity!!.sendBroadcast(intentBroadcast)
        }

        mView = view.findViewById(R.id.fab_favorite)
        mImage = mView.findViewById(R.id.app_icon)
        icon = IconicsDrawable(activity!!)
                .icon(if (mFavorite) CommunityMaterial.Icon.cmd_heart else CommunityMaterial.Icon.cmd_heart_outline)
                .color(ContextCompat.getColor(context!!, iconColorId))
                .sizeDp(48)
                .paddingDp(4)
        mImage.setImageDrawable(icon)
        mTextView = mView.findViewById(R.id.app_label)
        mTextView.setText(if (mFavorite) R.string.favorite_off else R.string.favorite_on)
        mView.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "Sending broadcast notification: " + CHOOSE_DONE)
            Log.d(TAG, "clicked id: " + FAVORITE)
            val intentBroadcast = Intent(CHOOSE_DONE)
            intentBroadcast.putExtra(DATA_ITEM_ID, FAVORITE)
            activity!!.sendBroadcast(intentBroadcast)
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
        private val TAG = BottomSheetFabCanto::class.java.canonicalName
        val CHOOSE_DONE = "canto_choose_done"
        val DATA_ITEM_ID = "item_id"

        val FULLSCREEN = 1
        val SOUND = 2
        val SAVE_FILE = 3
        val FAVORITE = 4

        fun newInstance(sound: Boolean, download: Boolean, favorite: Boolean, onlineUrl: Boolean, personalUrl: Boolean): BottomSheetFabCanto {
            val frag = BottomSheetFabCanto()
            val args = Bundle()
            args.putBoolean("showTitle", false)
            args.putBoolean("sound", sound)
            args.putBoolean("download", download)
            args.putBoolean("favorite", favorite)
            args.putBoolean("onlineUrl", onlineUrl)
            args.putBoolean("personalUrl", personalUrl)
            frag.arguments = args
            return frag
        }

        fun newInstance(@StringRes title: Int, sound: Boolean, download: Boolean, favorite: Boolean, onlineUrl: Boolean, personalUrl: Boolean): BottomSheetFabCanto {
            val frag = BottomSheetFabCanto()
            val args = Bundle()
            args.putInt("title", title)
            args.putBoolean("showTitle", true)
            args.putBoolean("sound", sound)
            args.putBoolean("download", download)
            args.putBoolean("favorite", favorite)
            args.putBoolean("onlineUrl", onlineUrl)
            args.putBoolean("personalUrl", personalUrl)
            frag.arguments = args
            return frag
        }
    }

}