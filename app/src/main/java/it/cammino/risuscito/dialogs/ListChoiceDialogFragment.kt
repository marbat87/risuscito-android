package it.cammino.risuscito.dialogs


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import java.io.Serializable

@Suppress("unused")
class ListChoiceDialogFragment : DialogFragment() {

    private var mCallback: ListChoiceCallback? = null

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(BUILDER_TAG) as? Builder

    override fun onDestroyView() {
        if (retainInstance)
            dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
                ?: throw IllegalStateException("ListChoiceDialogFragment should be created using its Builder interface.")

        if (mCallback == null)
            mCallback = mBuilder.mListener

        val dialog = MaterialDialog(requireContext())
                .listItemsSingleChoice(mBuilder.listArrayId, initialSelection = mBuilder.initialSelection) { _, index, _ ->
                    mCallback?.onPositive(mBuilder.mTag, index)
                }

        if (mBuilder.mTitle != 0)
            dialog.title(res = mBuilder.mTitle)

        if (!mBuilder.mAutoDismiss)
            dialog.noAutoDismiss()

        mBuilder.mContent?.let {
            dialog.message(text = it)
        }

        mBuilder.mPositiveButton?.let {
            dialog.positiveButton(text = it)
        }

        mBuilder.mNegativeButton?.let {
            dialog.negativeButton(text = it) {
                mCallback?.onNegative(mBuilder.mTag)
            }
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

    @SuppressLint("CheckResult")
    fun setContent(@StringRes res: Int) {
        (dialog as? MaterialDialog)?.message(res)
    }

    fun setmCallback(callback: ListChoiceCallback) {
        mCallback = callback
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
        if (mBuilder?.mCanceListener == true)
            mCallback?.onNegative(mBuilder.mTag)
    }

    class Builder(context: AppCompatActivity, @field:Transient internal var mListener: ListChoiceCallback, internal val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context
        internal var mTitle = 0
        internal var mContent: CharSequence? = null
        internal var mPositiveButton: CharSequence? = null
        internal var mNegativeButton: CharSequence? = null
        internal var mCanceable = false
        internal var mAutoDismiss = true
        internal var mCanceListener = false
        internal var listArrayId: Int = 0
        internal var initialSelection: Int = -1

        fun listArrayId(@ArrayRes res: Int): Builder {
            listArrayId = res
            return this
        }

        fun initialSelection(sel: Int): Builder {
            initialSelection = sel
            return this
        }

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

        fun positiveButton(@StringRes text: Int): Builder {
            mPositiveButton = this.mContext.resources.getText(text)
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            mNegativeButton = this.mContext.resources.getText(text)
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

        fun setAutoDismiss(autoDismiss: Boolean): Builder {
            mAutoDismiss = autoDismiss
            return this
        }

        fun build(): ListChoiceDialogFragment {
            val dialog = ListChoiceDialogFragment()
            val args = Bundle()
            args.putSerializable(BUILDER_TAG, this)
            dialog.arguments = args
            return dialog
        }

        fun show(): ListChoiceDialogFragment {
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

    fun show(context: AppCompatActivity): ListChoiceDialogFragment {
        val builder = builder
        builder?.let {
            dismissIfNecessary(context, it.mTag)
            show(context.supportFragmentManager, it.mTag)
        }
        return this
    }

    interface ListChoiceCallback {
        fun onPositive(tag: String, index: Int)
        fun onNegative(tag: String)
    }

    companion object {
        private const val BUILDER_TAG = "builder"
        fun findVisible(context: AppCompatActivity?, tag: String): ListChoiceDialogFragment? {
            context?.let {
                val frag = it.supportFragmentManager.findFragmentByTag(tag)
                return if (frag != null && frag is ListChoiceDialogFragment) frag else null
            }
            return null
        }
    }

}
