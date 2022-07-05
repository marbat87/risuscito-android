package it.cammino.risuscito.ui.dialog


import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.cammino.risuscito.utils.extension.capitalize
import java.io.Serializable

@Suppress("unused")
class ListChoiceDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("ListChoiceDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setSingleChoiceItems(
            mBuilder.listArrayId,
            mBuilder.initialSelection
        ) { _, index ->
            viewModel.index = index
            viewModel.handled = false
            viewModel.state.value = DialogState.Positive(this)
        }

        if (mBuilder.title != 0)
            dialog.setTitle(mBuilder.title)

        if (mBuilder.content != 0)
            context?.let {
                dialog.setMessage(it.resources.getText(mBuilder.content))
            }

        if (mBuilder.positiveButton != 0)
            context?.let {
                dialog.setPositiveButton(
                    it.resources.getText(mBuilder.positiveButton).capitalize(it.resources), null
                )
            }

        if (mBuilder.negativeButton != 0)
            context?.let {
                dialog.setNegativeButton(
                    it.resources.getText(mBuilder.negativeButton).capitalize(it.resources)
                ) { _, _ ->
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

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    class Builder(internal val mTag: String) : Serializable {

        @StringRes
        var title = 0

        @StringRes
        var content: Int = 0

        @StringRes
        var positiveButton: Int = 0

        @StringRes
        var negativeButton: Int = 0
        var canceable = false

        @ArrayRes
        var listArrayId: Int = 0
        var initialSelection: Int = -1

    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = ListChoiceDialogFragment()

        private fun newInstance(builder: Builder): ListChoiceDialogFragment {
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
        var index: Int = 0
        var handled = true
        val state = MutableLiveData<DialogState<ListChoiceDialogFragment>>()
    }

}
