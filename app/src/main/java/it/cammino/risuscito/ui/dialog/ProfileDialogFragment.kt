package it.cammino.risuscito.ui.dialog


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.StringUtils
import java.io.Serializable

@Suppress("unused")
class ProfileDialogFragment : DialogFragment() {

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

        val mView = layoutInflater.inflate(R.layout.profile_dialog_content, null, false)
        dialog.setView(mView)

        if (mBuilder.profileName.isNotEmpty())
            mView.findViewById<TextView>(R.id.profile_name).text = mBuilder.profileName

        if (mBuilder.profileEmail.isNotEmpty())
            mView.findViewById<TextView>(R.id.profile_email).text = mBuilder.profileEmail

        if (mBuilder.profileImageSrc.isEmpty())
            mView.findViewById<ShapeableImageView>(R.id.profile_icon)
                ?.setImageResource(R.drawable.account_circle_56px)
        else {
            AppCompatResources.getDrawable(mView.context, R.drawable.account_circle_56px)
                ?.let {
                    Picasso.get().load(mBuilder.profileImageSrc)
                        .placeholder(it)
                        .into(mView.findViewById<ShapeableImageView>(R.id.profile_icon))
                }
        }

        mView.findViewById<NavigationView>(R.id.profile_options).let {
            it.menu.clear()
            it.inflateMenu(R.menu.account_options)
            it.checkedItem?.isChecked = false
        }

        mView.findViewById<NavigationView>(R.id.profile_options).setNavigationItemSelectedListener {
            cancel()
            viewModel.mTag = mBuilder.mTag
            viewModel.menuItemId = it.itemId
            viewModel.handled = false
            viewModel.state.value = DialogState.Positive(this)
            true
        }

        mView.findViewById<Button>(R.id.close_modal).setOnClickListener {
            cancel()
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

        var profileName = StringUtils.EMPTY
        var profileEmail = StringUtils.EMPTY
        var profileImageSrc = StringUtils.EMPTY
        var canceable = false

    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = ProfileDialogFragment()

        private fun newInstance(builder: Builder): ProfileDialogFragment {
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
        var menuItemId: Int = 0
        var handled = true
        val state = MutableLiveData<DialogState<ProfileDialogFragment>>()
    }

}
