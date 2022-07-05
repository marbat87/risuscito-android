package it.cammino.risuscito.ui.dialog


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.extension.capitalize
import java.io.Serializable
import java.text.NumberFormat

@Suppress("unused")
class ProgressDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val progressPercentFormat = NumberFormat.getPercentInstance()
    private val progressNumberFormat = "%1d/%2d"

    private var mView: View? = null

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        if (mBuilder.title != 0)
            dialog.setTitle(mBuilder.title)

        if (mBuilder.icon != 0)
            dialog.setIcon(mBuilder.icon)

        if (mBuilder.positiveButton != 0)
            context?.let {
                dialog.setPositiveButton(
                    it.resources.getText(mBuilder.positiveButton)
                        .capitalize(it.resources)
                ) { _, _ ->
                    viewModel.mTag = mBuilder.mTag
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Positive(this)
                }
            }


        if (mBuilder.negativeButton != 0)
            context?.let {
                dialog.setNegativeButton(
                    it.resources.getText(mBuilder.negativeButton)
                        .capitalize(it.resources)
                ) { _, _ ->
                    viewModel.mTag = mBuilder.mTag
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Positive(this)
                }
            }

        val mdContent: TextView?

        if (mBuilder.progressIndeterminate) {
            mView = layoutInflater.inflate(R.layout.indeterminate_progressbar, null, false)
            dialog.setView(mView)
            mdContent =
                mView?.findViewById(R.id.md_content_indeterminate)
        } else {
            mView = layoutInflater.inflate(R.layout.linear_progressbar, null, false)
            dialog.setView(mView)
            mView?.findViewById<LinearProgressIndicator>(R.id.working_progress)?.max =
                mBuilder.progressMax
            mView?.findViewById<TextView>(R.id.md_minMax)?.isVisible =
                mBuilder.showMinMax
            mdContent = mView?.findViewById(R.id.md_content_linear)
        }

        mdContent?.isVisible = mBuilder.content != 0
        if (mBuilder.content != 0)
            context?.let {
                mdContent?.text = it.resources.getText(mBuilder.content)
            }

        dialog.setCancelable(mBuilder.canceable)

        dialog.setOnKeyListener { arg0, keyCode, event ->
            var returnValue = false
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                arg0.cancel()
                returnValue = true
            }
            returnValue
        }

        return dialog.show()
    }

    fun setContent(@StringRes res: Int) {
        mView?.findViewById<TextView>(R.id.md_content_indeterminate)?.setText(res)
    }

    fun setProgress(progress: Int) {
        mView?.findViewById<LinearProgressIndicator>(R.id.working_progress)?.progress = progress
        mView?.findViewById<TextView>(R.id.md_label)?.text =
            progressPercentFormat.format(
                progress.toFloat() / (builder?.progressMax?.toFloat() ?: Float.MIN_VALUE)
            )
        mView?.findViewById<TextView>(R.id.md_minMax)?.text =
            String.format(progressNumberFormat, progress, builder?.progressMax)
    }

    fun cancel() {
        dialog?.cancel()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    class Builder(internal val mTag: String) : Serializable {

        @StringRes
        var title = 0

        @DrawableRes
        var icon = 0
        var progressIndeterminate: Boolean = false
        var progressMax: Int = 0

        @StringRes
        var content: Int = 0

        @StringRes
        var positiveButton: Int = 0

        @StringRes
        var negativeButton: Int = 0
        var canceable = false
        var showMinMax = false

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
        var mTag: String = StringUtils.EMPTY
        var handled = true
        val state = MutableLiveData<DialogState<ProgressDialogFragment>>()
    }

}
