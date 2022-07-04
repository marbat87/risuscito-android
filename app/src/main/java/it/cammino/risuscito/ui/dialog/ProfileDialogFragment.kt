package it.cammino.risuscito.ui.dialog


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        if (mBuilder.mProfileName.isNotEmpty())
            mView.findViewById<TextView>(R.id.profile_name).text = mBuilder.mProfileName

        if (mBuilder.mProfileEmail.isNotEmpty())
            mView.findViewById<TextView>(R.id.profile_email).text = mBuilder.mProfileEmail

        if (mBuilder.mProfileImageSrc.isEmpty())
            mView.findViewById<ShapeableImageView>(R.id.profile_icon)
                ?.setImageResource(R.drawable.account_circle_56px)
        else {
            AppCompatResources.getDrawable(mView.context, R.drawable.account_circle_56px)
                ?.let {
                    Picasso.get().load(mBuilder.mProfileImageSrc)
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

    class Builder(context: AppCompatActivity, val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context

        var mProfileName = StringUtils.EMPTY
        var mProfileEmail = StringUtils.EMPTY
        var mProfileImageSrc = StringUtils.EMPTY
        var mCanceable = false
        var mPrefill: CharSequence? = null

        fun profileName(text: String): Builder {
            mProfileName = text
            return this
        }

        fun profileEmail(text: String): Builder {
            mProfileEmail = text
            return this
        }

        fun profileImageSrc(text: String): Builder {
            mProfileImageSrc = text
            return this
        }

        fun setCanceable(canceable: Boolean): Builder {
            mCanceable = canceable
            return this
        }

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
