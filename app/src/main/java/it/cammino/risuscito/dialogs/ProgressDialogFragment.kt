package it.cammino.risuscito.dialogs


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.indeterminate_progressbar.view.*
import kotlinx.android.synthetic.main.linear_progressbar.view.*
import java.io.Serializable
import java.text.NumberFormat

@Suppress("unused")
class ProgressDialogFragment : DialogFragment() {

    private var mCallback: ProgressCallback? = null
    private val progressPercentFormat = NumberFormat.getPercentInstance()
    private val progressNumberFormat = "%1d/%2d"

    private val builder: Builder?
        get() = if (arguments?.containsKey("builder") != true) null else arguments?.getSerializable("builder") as? Builder

    override fun onDestroyView() {
        if (dialog != null && retainInstance)
            dialog.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
                ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        if (mCallback == null)
            mCallback = mBuilder.mListener

        val dialog = MaterialDialog(requireContext())

        if (mBuilder.mTitle != 0)
            dialog.title(res = mBuilder.mTitle)

        if (!mBuilder.mAutoDismiss)
            dialog.noAutoDismiss()

        mBuilder.mPositiveButton?.let {
            dialog.positiveButton(text = it) {
                mCallback?.onPositive(mBuilder.mTag)
            }
        }

        mBuilder.mNegativeButton?.let {
            dialog.negativeButton(text = it) {
                mCallback?.onNegative(mBuilder.mTag)
            }
        }

        if (mBuilder.mProgressIndeterminate) {
            dialog.customView(R.layout.indeterminate_progressbar)
            dialog.getCustomView().md_content_indeterminate.visibility = if (mBuilder.mContent != null) View.VISIBLE else View.GONE
            dialog.getCustomView().md_content_indeterminate.text = mBuilder.mContent ?: ""
        } else {
            dialog.customView(R.layout.linear_progressbar)
            dialog.getCustomView().working_progress.max = mBuilder.mProgressMax
            dialog.getCustomView().md_minMax.visibility = if (mBuilder.mShowMinMax) View.VISIBLE else View.GONE
            dialog.getCustomView().md_content_linear.visibility = if (mBuilder.mContent != null) View.VISIBLE else View.GONE
            dialog.getCustomView().md_content_linear.text = mBuilder.mContent ?: ""
        }

        dialog.setCancelable(mBuilder.mCanceable)

        dialog.setOnKeyListener(DialogInterface.OnKeyListener
        { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                arg0.cancel()
                return@OnKeyListener true
            }
            false
        })

        return dialog
    }

    @SuppressLint("CheckResult")
    fun setContent(@StringRes res: Int) {
        (dialog as? MaterialDialog)?.getCustomView()?.md_content_indeterminate?.setText(res)
    }

    fun setmCallback(callback: ProgressCallback) {
        mCallback = callback
    }

    fun setProgress(progress: Int) {
        (dialog as? MaterialDialog)?.getCustomView()?.working_progress?.progress = progress
        (dialog as? MaterialDialog)?.getCustomView()?.md_label?.text =
                progressPercentFormat.format(
                        progress.toFloat() / (builder?.mProgressMax?.toFloat() ?: Float.MIN_VALUE))
        (dialog as? MaterialDialog)?.getCustomView()?.md_minMax?.text =
                String.format(progressNumberFormat, progress, builder?.mProgressMax)
    }

    fun cancel() {
        dialog.cancel()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    class Builder(context: AppCompatActivity, @field:Transient internal var mListener: ProgressCallback?, internal val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context
        internal var mTitle = 0
        internal var mProgressIndeterminate: Boolean = false
        internal var mProgressMax: Int = 0
        internal var mContent: CharSequence? = null
        internal var mPositiveButton: CharSequence? = null
        internal var mNegativeButton: CharSequence? = null
        internal var mCanceable = false
        internal var mAutoDismiss = true
        internal var mShowMinMax = false

        fun title(@StringRes text: Int): Builder {
            mTitle = text
            return this
        }

        fun content(@StringRes content: Int): Builder {
            mContent = this.mContext.resources.getText(content)
            return this
        }

        fun content(content: String): Builder {
            mContent = content
            return this
        }

        fun progressIndeterminate(progressIndeterminate: Boolean): Builder {
            mProgressIndeterminate = progressIndeterminate
            return this
        }

        fun progressMax(progressMax: Int): Builder {
            mProgressMax = progressMax
            return this
        }

        fun positiveButton(@StringRes text: Int): Builder {
            mPositiveButton = this.mContext.resources.getText(text)
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            mNegativeButton = this.mContext.resources.getText(text)
            return this
        }

        fun setCanceable(): Builder {
            mCanceable = true
            return this
        }

        fun showMinMax(): Builder {
            mShowMinMax = true
            return this
        }

        fun build(): ProgressDialogFragment {
            val dialog = ProgressDialogFragment()
            val args = Bundle()
            args.putSerializable("builder", this)
            dialog.arguments = args
            return dialog
        }

        fun show(): ProgressDialogFragment {
            val dialog = build()
            if (!mContext.isFinishing)
                dialog.show(mContext)
            return dialog
        }
    }

    private fun dismissIfNecessary(context: AppCompatActivity, tag: String) {
        val frag = context.supportFragmentManager.findFragmentByTag(tag)
        frag?.let {
            (it as? DialogFragment)?.dismiss()
            context.supportFragmentManager.beginTransaction()
                    .remove(it).commit()
        }
    }

    fun show(context: AppCompatActivity): ProgressDialogFragment {
        val builder = builder
        builder?.let {
            dismissIfNecessary(context, it.mTag)
            show(context.supportFragmentManager, it.mTag)
        }
        return this
    }

    interface ProgressCallback {
        fun onPositive(tag: String)
        fun onNegative(tag: String)
    }

    companion object {
        fun findVisible(context: AppCompatActivity?, tag: String): ProgressDialogFragment? {
            context?.let {
                val frag = it.supportFragmentManager.findFragmentByTag(tag)
                return if (frag != null && frag is ProgressDialogFragment) frag else null
            }
            return null
        }
    }

}
