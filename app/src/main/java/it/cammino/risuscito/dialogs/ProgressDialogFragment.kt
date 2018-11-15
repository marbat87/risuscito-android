package it.cammino.risuscito.dialogs


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import it.cammino.risuscito.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.io.Serializable
import java.text.NumberFormat

@Suppress("unused")
class ProgressDialogFragment : DialogFragment() {

    private var mCallback: ProgressCallback? = null
    private val progressPercentFormat = NumberFormat.getPercentInstance()
    private val progressNumberFormat = "%1d/%2d"

    private val builder: Builder?
        get() = if (arguments == null || !arguments!!.containsKey("builder")) null else arguments!!.getSerializable("builder") as Builder

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

        val dialog = MaterialDialog(activity!!)

        if (mBuilder.mTitle != 0)
            dialog.title(res = mBuilder.mTitle)

        if (!mBuilder.mAutoDismiss)
            dialog.noAutoDismiss()

//        if (mBuilder.mContent != null)
//            dialog.message(text = mBuilder.mContent!!)

        if (mBuilder.mPositiveButton != null && mCallback != null) {
            dialog.positiveButton(text = mBuilder.mPositiveButton) {
                mCallback!!.onPositive(mBuilder.mTag)
            }
        }

        if (mBuilder.mNegativeButton != null && mCallback != null) {
            dialog.negativeButton(text = mBuilder.mNegativeButton) {
                mCallback!!.onNegative(mBuilder.mTag)
            }
        }

//        dialog.customView(R.layout.linear_progressbar)

        if (mBuilder.mProgressIndeterminate)
            dialog.customView(R.layout.indeterminate_progressbar)
        else {
            dialog.customView(R.layout.linear_progressbar)
            dialog.getCustomView()?.findViewById<MaterialProgressBar>(R.id.working_progress)?.max = mBuilder.mProgressMax
            dialog.getCustomView()?.findViewById<TextView>(R.id.md_minMax)?.visibility = if (mBuilder.mShowMinMax) View.VISIBLE else View.GONE
        }

        if (mBuilder.mContent != null) {
            dialog.getCustomView()?.findViewById<TextView>(R.id.md_content)?.visibility = View.VISIBLE
            dialog.getCustomView()?.findViewById<TextView>(R.id.md_content)?.text = mBuilder.mContent
        } else
            dialog.getCustomView()?.findViewById<TextView>(R.id.md_content)?.visibility = View.GONE

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
        (dialog as MaterialDialog).getCustomView()?.findViewById<TextView>(R.id.md_content)?.setText(res)
    }

    fun setmCallback(callback: ProgressCallback) {
        mCallback = callback
    }

    fun setProgress(progress: Int) {
        (dialog as MaterialDialog).getCustomView()?.findViewById<MaterialProgressBar>(R.id.working_progress)?.progress = progress
        (dialog as MaterialDialog).getCustomView()?.findViewById<TextView>(R.id.md_label)?.text =
                progressPercentFormat.format(
                        progress.toFloat() / builder!!.mProgressMax.toFloat())
        (dialog as MaterialDialog).getCustomView()?.findViewById<TextView>(R.id.md_minMax)?.text =
                String.format(progressNumberFormat, progress, builder!!.mProgressMax)
    }

    fun cancel() {
        dialog.cancel()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

//    override fun onCancel(dialog: DialogInterface?) {
//        super.onCancel(dialog)
//        val mBuilder = builder
//        if (mBuilder != null && mBuilder.mCanceListener)
//            mCallback!!.onPositive(mBuilder.mTag)
//    }

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
//        internal var mCanceListener = false

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
            dialog.show(mContext)
            return dialog
        }
    }

    private fun dismissIfNecessary(context: AppCompatActivity, tag: String) {
        val frag = context.supportFragmentManager.findFragmentByTag(tag)
        if (frag != null) {
            (frag as DialogFragment).dismiss()
            context.supportFragmentManager.beginTransaction()
                    .remove(frag).commit()
        }
    }

    fun show(context: AppCompatActivity): ProgressDialogFragment {
        val builder = builder
        if (builder != null) {
            dismissIfNecessary(context, builder.mTag)
            show(context.supportFragmentManager, builder.mTag)
        }
        return this
    }

    interface ProgressCallback {
        fun onPositive(tag: String)
        fun onNegative(tag: String)
//        fun onNeutral(tag: String)
    }

    companion object {

        fun findVisible(context: AppCompatActivity, tag: String): ProgressDialogFragment? {
            val frag = context.supportFragmentManager.findFragmentByTag(tag)
            return if (frag != null && frag is ProgressDialogFragment) frag else null
        }
    }

}
