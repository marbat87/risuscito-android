package it.cammino.risuscito.ui.dialog


import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.extension.capitalize
import it.cammino.risuscito.utils.extension.getSerializableWrapper
import java.io.Serializable

@Suppress("unused")
class InputTextDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializableWrapper(
            BUILDER_TAG,
            Builder::class.java
        ) as? Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val mView = layoutInflater.inflate(R.layout.input_search, null, false)
        dialog.setView(mView)

        val input =
            mView.findViewById<TextInputLayout>(if (mBuilder.multiLine) R.id.multiple_line_text else R.id.single_line_text).editText
        if (mBuilder.prefill.isNotEmpty())
            input?.setText(mBuilder.prefill)
        mView.findViewById<TextInputLayout>(R.id.multiple_line_text).isVisible = mBuilder.multiLine
        mView.findViewById<TextInputLayout>(R.id.single_line_text).isVisible = !mBuilder.multiLine

        if (mBuilder.title != 0)
            dialog.setTitle(mBuilder.title)

        if (mBuilder.positiveButton != 0)
            context?.let {
                dialog.setPositiveButton(
                    it.resources.getText(mBuilder.positiveButton)
                        .capitalize(it)
                ) { _, _ ->
                    viewModel.mTag = mBuilder.mTag
                    viewModel.outputText = input?.text.toString()
                    viewModel.outputItemId = mBuilder.itemId
                    viewModel.outputCantoId = mBuilder.cantoId
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Positive(this)
                }
            }

        if (mBuilder.negativeButton != 0)
            context?.let {
                dialog.setNegativeButton(
                    it.resources.getText(mBuilder.negativeButton)
                        .capitalize(it)
                ) { _, _ ->
                    viewModel.mTag = mBuilder.mTag
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Negative(this)
                }
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

    fun cancel() {
        dialog?.cancel()
    }

    class Builder(val mTag: String) : Serializable {

        @StringRes
        var title = 0

        @StringRes
        var positiveButton: Int = 0

        @StringRes
        var negativeButton: Int = 0
        var canceable = false
        var prefill: String = StringUtils.EMPTY
        var itemId: Int = 0
        var cantoId: Int = 0
        var multiLine: Boolean = false

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
        var mTag: String = StringUtils.EMPTY
        var outputText: String = StringUtils.EMPTY
        var outputItemId: Int = 0
        var outputCantoId: Int = 0
        var handled = true
        val state = MutableLiveData<DialogState<InputTextDialogFragment>>()
    }

}
