package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialcab.MaterialCab.Companion.destroy
import com.crashlytics.android.Crashlytics
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleHistoryItem
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.CronologiaViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_history.*

class HistoryFragment : Fragment(), SimpleDialogFragment.SimpleCallback {

    private lateinit var mCronologiaViewModel: CronologiaViewModel
    private var cantoAdapter: FastItemAdapter<SimpleHistoryItem> = FastItemAdapter()
    private var selectExtension: SelectExtension<SimpleHistoryItem>? = null

    private var actionModeOk: Boolean = false

    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_history, container, false)

        mCronologiaViewModel = ViewModelProviders.of(this).get(CronologiaViewModel::class.java)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_history)

        mMainActivity?.setTabVisible(false)

        mLUtils = LUtils.getInstance(requireActivity())

        if (!PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Utility.HISTORY_OPEN, false)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.HISTORY_OPEN, true) }
            Handler().postDelayed(250) {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                        .show()
            }
        }

        val sFragment = SimpleDialogFragment.findVisible(mMainActivity, RESET_HISTORY)
        sFragment?.setmCallback(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainActivity?.enableBottombar(false)
        mMainActivity?.enableFab(false)

        cantoAdapter.onPreClickListener = { _: View?, _: IAdapter<SimpleHistoryItem>, _: SimpleHistoryItem, position: Int ->
            var consume = false
            if (MaterialCab.isActive) {
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY_SELECTION) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    cantoAdapter
                            .getAdapterItem(position)
                            .isSelected = !cantoAdapter.getAdapterItem(position).isSelected
                    cantoAdapter.notifyAdapterItemChanged(position)
                    if (selectExtension?.selectedItems?.size == 0)
                        destroy()
                    else
                        startCab()
                }
                consume = true
            }
            consume
        }

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<SimpleHistoryItem>, item: SimpleHistoryItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(activity, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(Utility.PAGINA to item.source?.getText(context), Utility.ID_CANTO to item.id))
                mLUtils?.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        cantoAdapter.onPreLongClickListener = { _: View?, _: IAdapter<SimpleHistoryItem>, _: SimpleHistoryItem, position: Int ->
            if (!MaterialCab.isActive) {
                if (mMainActivity?.isOnTablet != true)
                    activity?.toolbar_layout?.setExpanded(true, true)
                cantoAdapter.getAdapterItem(position).isSelected = true
                cantoAdapter.notifyAdapterItemChanged(position)
                startCab()
            }
            true
        }

        cantoAdapter.setHasStableIds(true)

        selectExtension = SelectExtension(cantoAdapter)
        selectExtension?.isSelectable = true
        selectExtension?.multiSelect = true
        selectExtension?.selectOnLongClick = true
        selectExtension?.deleteAllSelectedItems()

        history_recycler?.adapter = cantoAdapter
        val llm = if (mMainActivity?.isGridLayout == true)
            GridLayoutManager(context, if (mMainActivity?.hasThreeColumns == true) 3 else 2)
        else
            LinearLayoutManager(context)
        history_recycler?.layoutManager = llm
        history_recycler?.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(requireContext(), llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)!!)
        history_recycler?.addItemDecoration(insetDivider)
        history_recycler?.itemAnimator = SlideRightAlphaAnimator()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        subscribeUiHistory()
    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        IconicsMenuInflaterUtil.inflate(
                requireActivity().menuInflater, requireContext(), R.menu.clean_list_menu, menu)
        menu.findItem(R.id.list_reset).isVisible = cantoAdapter.adapterItemCount > 0
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.list_reset -> {
                mMainActivity?.let {
                    SimpleDialogFragment.Builder(
                            it, this, RESET_HISTORY)
                            .title(R.string.dialog_reset_history_title)
                            .content(R.string.dialog_reset_history_desc)
                            .positiveButton(R.string.clear_confirm)
                            .negativeButton(R.string.cancel)
                            .show()
                }
                return true
            }
            R.id.action_help -> {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                        .show()
                return true
            }
        }
        return false
    }

    override fun onPositive(tag: String) {
        Log.d(javaClass.name, "onPositive: $tag")
        when (tag) {
            RESET_HISTORY ->
                ioThread {
                    val mDao = RisuscitoDatabase.getInstance(requireContext()).cronologiaDao()
                    mDao.emptyCronologia()
                }
        }
    }

    override fun onNegative(tag: String) {}

    private fun startCab() {
        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
            val itemSelectedCount = selectExtension?.selectedItems?.size ?: 0
            title = resources.getQuantityString(R.plurals.item_selected, itemSelectedCount, itemSelectedCount)
            popupTheme = R.style.ThemeOverlay_MaterialComponents_Dark_ActionBar
            contentInsetStartRes(R.dimen.mcab_default_content_inset)
            menuRes = R.menu.menu_delete
            backgroundColor = themeUtils.primaryColorDark()

            onCreate { _, _ ->
                Log.d(TAG, "MaterialCab onCreate")
                actionModeOk = false
            }

            onSelection { item ->
                Log.d(TAG, "MaterialCab onSelection")
                when (item.itemId) {
                    R.id.action_remove_item -> {
                        removeHistories()
                        actionModeOk = true
                        destroy()
                        true
                    }
                    else -> false
                }
            }

            onDestroy {
                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                if (!actionModeOk) {
                    try {
                        selectExtension?.deselect()
                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                    }
                }
                true
            }
        }
    }

    private fun removeHistories() {
        ListeUtils.removeHistoriesWithUndo(this, selectExtension?.selectedItems)
    }

    private fun subscribeUiHistory() {
        mCronologiaViewModel.cronologiaCanti?.observe(
                this,
                Observer {
                    cantoAdapter.set(it)
                    no_history?.visibility = if (cantoAdapter.adapterItemCount > 0) View.INVISIBLE else View.VISIBLE
                    activity?.invalidateOptionsMenu()
                })
    }

    companion object {
        private val TAG = HistoryFragment::class.java.canonicalName
        private const val RESET_HISTORY = "RESET_HISTORY"
    }
}
