package it.cammino.risuscito.dialogs


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import java.io.Serializable

@Suppress("unused")
class InputTextDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(BUILDER_TAG) as? Builder

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
                ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val dialog = MaterialDialog(requireContext())
                .input(prefill = mBuilder.mPrefill
                        ?: "", inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)

        if (mBuilder.mTitle != 0)
            dialog.title(res = mBuilder.mTitle)

        if (!mBuilder.mAutoDismiss)
            dialog.noAutoDismiss()

        mBuilder.mPositiveButton?.let {
            dialog.positiveButton(text = it) { mDialog ->
                viewModel.mTag = mBuilder.mTag
                viewModel.outputText = mDialog.getInputField().text.toString()
                viewModel.handled = false
                viewModel.state.value = DialogState.Positive(this)
            }
        }

        mBuilder.mNegativeButton?.let {
            dialog.negativeButton(text = it) {
                viewModel.mTag = mBuilder.mTag
                viewModel.handled = false
                viewModel.state.value = DialogState.Negative(this)
            }
        }

        dialog.onShow {
            it.getInputField().selectAll()
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

    fun cancel() {
        dialog?.cancel()
    }

    class Builder(context: AppCompatActivity, val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context
        @StringRes
        var mTitle = 0
        var mPositiveButton: CharSequence? = null
        var mNegativeButton: CharSequence? = null
        var mCanceable = false
        var mAutoDismiss = true
        var mPrefill: CharSequence? = null

        fun title(@StringRes text: Int): Builder {
            mTitle = text
            return this
        }

        fun prefill(@StringRes text: Int): Builder {
            mPrefill = this.mContext.resources.getText(text)
            return this
        }

        fun prefill(text: String): Builder {
            mPrefill = text
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

        fun setCanceable(canceable: Boolean): Builder {
            mCanceable = canceable
            return this
        }

        fun setAutoDismiss(autoDismiss: Boolean): Builder {
            mAutoDismiss = autoDismiss
            return this
        }

    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = InputTextDialogFragment()

        private fun newInstance(builder: Builder): InputTextDialogFragment {
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
    }

    class DialogViewModel : ViewModel() {
        var mTag: String = ""
        var outputText: String = ""
        var handled = true
        val state = MutableLiveData<DialogState<InputTextDialogFragment>>()
    }

}
