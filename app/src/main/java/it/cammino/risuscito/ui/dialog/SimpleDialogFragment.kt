package it.cammino.risuscito.ui.dialog


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.extension.capitalize
import java.io.Serializable

@Suppress("unused")
class SimpleDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        if (mBuilder.title != 0)
            dialog.setTitle(mBuilder.title)

        if (mBuilder.stringTitle.isNotEmpty())
            dialog.setTitle(mBuilder.stringTitle)

        if (mBuilder.mIcon != 0)
            dialog.setIcon(mBuilder.mIcon)

        if (mBuilder.content != 0)
            dialog.setMessage(mBuilder.content)

        if (mBuilder.stringContent.isNotEmpty())
            dialog.setMessage(mBuilder.stringContent)

        if (mBuilder.positiveButton != 0)
            context?.let {
                dialog.setPositiveButton(
                    it.resources.getText(mBuilder.positiveButton).capitalize(it.resources)
                ) { _, _ ->
                    viewModel.mTag = mBuilder.mTag
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Positive(this)
                }
            }

        if (mBuilder.negativeButton != 0)
            context?.let {
                dialog.setNegativeButton(
                    it.resources.getText(mBuilder.negativeButton).capitalize(it.resources)
                ) { _, _ ->
                    viewModel.mTag = mBuilder.mTag
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Negative(this)
                }
            }

        dialog.setCancelable(mBuilder.mCanceable)

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

    fun cancel() {
        dialog?.cancel()
    }

    fun setOnCancelListener(listener: DialogInterface.OnCancelListener) {
        dialog?.setOnCancelListener(listener)
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val mBuilder = builder
        if (mBuilder?.mCanceListener == true) {
            viewModel.mTag = mBuilder.mTag
            viewModel.handled = false
            viewModel.state.value = DialogState.Positive(this)
        }
    }

    class Builder(internal val mTag: String) : Serializable {

        @StringRes
        internal var title = 0
        internal var stringTitle: CharSequence = StringUtils.EMPTY
        internal var mIcon = 0

        @StringRes
        internal var content: Int = 0
        internal var stringContent: CharSequence = StringUtils.EMPTY

        @StringRes
        internal var positiveButton: Int = 0

        @StringRes
        internal var negativeButton: Int = 0

        internal var mCanceable = false
        internal var mCanceListener = false

        fun title(@StringRes text: Int): Builder {
            title = text
            return this
        }

        fun title(text: String): Builder {
            stringTitle = text
            return this
        }

        fun icon(@DrawableRes text: Int): Builder {
            mIcon = text
            return this
        }

        fun content(@StringRes content: Int): Builder {
            this.content = content
            return this
        }

        fun content(content: String): Builder {
            this.stringContent = content
            return this
        }

        fun positiveButton(@StringRes text: Int): Builder {
            positiveButton = text
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            negativeButton = text
            return this
        }

        fun setHasCancelListener(): Builder {
            mCanceListener = true
            return this
        }

        fun setCanceable(): Builder {
            mCanceable = true
            return this
        }

    }

    companion object {
        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = SimpleDialogFragment()

        private fun newInstance(builder: Builder): SimpleDialogFragment {
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

        fun findVisible(context: AppCompatActivity?, tag: String): SimpleDialogFragment? {
            context?.let {
                val frag = it.supportFragmentManager.findFragmentByTag(tag)
                return if (frag != null && frag is SimpleDialogFragment) frag else null
            }
            return null
        }
    }

    class DialogViewModel : ViewModel() {
        var mTag: String = StringUtils.EMPTY
        var handled = true
        val state = MutableLiveData<DialogState<SimpleDialogFragment>>()
    }

}
