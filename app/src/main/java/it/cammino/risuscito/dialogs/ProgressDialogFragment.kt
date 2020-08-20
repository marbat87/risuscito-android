package it.cammino.risuscito.dialogs


import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import it.cammino.risuscito.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.io.Serializable
import java.text.NumberFormat

@Suppress("unused")
class ProgressDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val progressPercentFormat = NumberFormat.getPercentInstance()
    private val progressNumberFormat = "%1d/%2d"

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(BUILDER_TAG) as? Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
                ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val dialog = MaterialDialog(requireContext())

        if (mBuilder.mTitle != 0)
            dialog.title(res = mBuilder.mTitle)

        if (!mBuilder.mAutoDismiss)
            dialog.noAutoDismiss()

        mBuilder.mPositiveButton?.let {
            dialog.positiveButton(text = it) {
                viewModel.mTag = mBuilder.mTag
                viewModel.handled = false
                viewModel.state.value = DialogState.Positive(this)
            }
        }

        mBuilder.mNegativeButton?.let {
            dialog.negativeButton(text = it) {
                viewModel.mTag = mBuilder.mTag
                viewModel.handled = false
                viewModel.state.value = DialogState.Positive(this)
            }
        }

        if (mBuilder.mProgressIndeterminate) {
            dialog.customView(R.layout.indeterminate_progressbar)
            val mdContent = dialog.getCustomView().findViewById<TextView>(R.id.md_content_indeterminate)
            mdContent.isVisible = mBuilder.mContent != null
            mdContent.text = mBuilder.mContent
                    ?: ""
        } else {
            dialog.customView(R.layout.linear_progressbar)
            dialog.getCustomView().findViewById<MaterialProgressBar>(R.id.working_progress).max = mBuilder.mProgressMax
            dialog.getCustomView().findViewById<TextView>(R.id.md_minMax).isVisible = mBuilder.mShowMinMax
            val mdContent = dialog.getCustomView().findViewById<TextView>(R.id.md_content_linear)
            mdContent.isVisible = mBuilder.mContent != null
            mdContent.text = mBuilder.mContent
                    ?: ""
        }

        dialog.cancelable(mBuilder.mCanceable)

        dialog.setOnKeyListener { arg0, keyCode, event ->
            var returnValue = false
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                arg0.cancel()
                returnValue = true
            }
            returnValue
        }

        return dialog
    }

    fun setContent(@StringRes res: Int) {
        (dialog as? MaterialDialog)?.getCustomView()?.findViewById<TextView>(R.id.md_content_indeterminate)?.setText(res)
    }

    fun setProgress(progress: Int) {
        (dialog as? MaterialDialog)?.getCustomView()?.findViewById<MaterialProgressBar>(R.id.working_progress)?.progress = progress
        (dialog as? MaterialDialog)?.getCustomView()?.findViewById<TextView>(R.id.md_label)?.text =
                progressPercentFormat.format(
                        progress.toFloat() / (builder?.mProgressMax?.toFloat() ?: Float.MIN_VALUE))
        (dialog as? MaterialDialog)?.getCustomView()?.findViewById<TextView>(R.id.md_minMax)?.text =
                String.format(progressNumberFormat, progress, builder?.mProgressMax)
    }

    fun cancel() {
        dialog?.cancel()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    class Builder(context: AppCompatActivity, internal val mTag: String) : Serializable {

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

    }

    companion object {
        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = ProgressDialogFragment()

        private fun newInstance(builder: Builder): ProgressDialogFragment {
            return newInstance().apply {
                arguments = bundleOf(
                        Pair(BUILDER_TAG, builder)
                )
            }
        }

        fun show(builder: Builder, fragmentManger: FragmentManager) {
            newInstance(builder).run {
                show(fragmentManger, builder.mTag)
            }
        }

        fun findVisible(context: AppCompatActivity?, tag: String): ProgressDialogFragment? {
            context?.let {
                val frag = it.supportFragmentManager.findFragmentByTag(tag)
                return if (frag != null && frag is ProgressDialogFragment) frag else null
            }
            return null
        }
    }

    class DialogViewModel : ViewModel() {
        var mTag: String = ""
        var handled = true
        val state = MutableLiveData<DialogState<ProgressDialogFragment>>()
    }

}
