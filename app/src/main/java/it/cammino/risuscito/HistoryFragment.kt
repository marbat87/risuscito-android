package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.crashlytics.android.Crashlytics
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.OnClickListener
import com.mikepenz.fastadapter.listeners.OnLongClickListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleHistoryItem
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.CronologiaViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_history.*
import java.sql.Date

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class HistoryFragment : Fragment(), SimpleDialogFragment.SimpleCallback {

    private var mCronologiaViewModel: CronologiaViewModel? = null
    private var cantoAdapter: FastItemAdapter<SimpleHistoryItem> = FastItemAdapter()

    private var actionModeOk: Boolean = false

    //    private var mUndoHelper: UndoHelper<*>? = null
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private var mRemovedItems: Set<SimpleHistoryItem>? = null

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_history, container, false)

        mCronologiaViewModel = ViewModelProviders.of(this).get(CronologiaViewModel::class.java)

        mMainActivity = activity as MainActivity?
        mMainActivity!!.setupToolbarTitle(R.string.title_activity_history)

        mMainActivity!!.setTabVisible(false)

        mLUtils = LUtils.getInstance(activity!!)

        if (!PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Utility.HISTORY_OPEN, false)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.HISTORY_OPEN, true) }
            Handler().postDelayed(250) {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                        .show()
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainActivity!!.enableBottombar(false)
        mMainActivity!!.enableFab(false)

        val mOnPreClickListener = OnClickListener<SimpleHistoryItem> { _, iAdapter, item, i ->
            Log.d(TAG, "mOnPreClickListener: MaterialCab.isActive ${MaterialCab.isActive}")
            if (MaterialCab.isActive) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY_SELECTION)
                    return@OnClickListener true
                mLastClickTime = SystemClock.elapsedRealtime()

                cantoAdapter
                        .getAdapterItem(i)
                        .withSetSelected(!cantoAdapter.getAdapterItem(i).isSelected)
                cantoAdapter.notifyAdapterItemChanged(i)
                if ((cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!
                                .selectedItems
                                .size == 0)
                    MaterialCab.destroy()
                else
                    startCab()
                return@OnClickListener true
            }
            false
        }

        val mOnClickListener = OnClickListener<SimpleHistoryItem> { mView, _, item, _ ->
            Log.d(TAG, "mOnClickListener: MaterialCab.isActive ${MaterialCab.isActive}")
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener true
            mLastClickTime = SystemClock.elapsedRealtime()
            val bundle = Bundle()
            bundle.putCharSequence("pagina", item.source!!.text)
            bundle.putInt("idCanto", item.id)

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle)
            true
        }

        val mOnPreLongClickListener = OnLongClickListener<SimpleHistoryItem> { _, _, _, i ->
            if (MaterialCab.isActive) return@OnLongClickListener true
            if (!mMainActivity!!.isOnTablet)
                activity!!.toolbar_layout!!.setExpanded(true, true)
            cantoAdapter.getAdapterItem(i).withSetSelected(true)
            cantoAdapter.notifyAdapterItemChanged(i)
            startCab()
            true
        }

        cantoAdapter
                .withSelectable(true)
                .withMultiSelect(true)
                .withSelectOnLongClick(true)
                .withOnPreClickListener(mOnPreClickListener)
                .withOnClickListener(mOnClickListener)
                .withOnPreLongClickListener(mOnPreLongClickListener)
                .setHasStableIds(true)
        cantoAdapter.set(mCronologiaViewModel!!.titoli)
        (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::
        class.java))!!.deleteAllSelectedItems()

        history_recycler!!.adapter = cantoAdapter
        //        val llm = LinearLayoutManager(context)
        val llm = if (mMainActivity!!.isGridLayout)
            GridLayoutManager(context, if (mMainActivity!!.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        history_recycler!!.layoutManager = llm
        history_recycler!!.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(context!!, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(context!!, R.drawable.material_inset_divider)!!)
        history_recycler!!.addItemDecoration(insetDivider)
        history_recycler!!.itemAnimator = SlideLeftAlphaAnimator()

//        mUndoHelper = UndoHelper(
//                cantoAdapter,
//                UndoHelper.UndoListener
//                { _, arrayList ->
//                    Log.d(TAG, "commitRemove: " + arrayList.size)
//                    arrayList
//                            .map { it.item }
//                            .forEach {
//                                Thread(
//                                        Runnable {
//                                            val mDao = RisuscitoDatabase.getInstance(context!!).cronologiaDao()
//                                            val cronTemp = Cronologia()
//                                            cronTemp.idCanto = it.id
//                                            mDao.deleteCronologia(cronTemp)
//                                        })
//                                        .start()
//                            }
//                })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        populateDb()
        subscribeUiHistory()
    }

    override fun onDestroy() {
        if (MaterialCab.isActive) MaterialCab.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        IconicsMenuInflaterUtil.inflate(
                activity!!.menuInflater, activity, R.menu.clean_list_menu, menu)
        menu!!.findItem(R.id.list_reset).isVisible = cantoAdapter.adapterItemCount > 0
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.list_reset -> {
                SimpleDialogFragment.Builder(
                        (activity as AppCompatActivity?)!!, this@HistoryFragment, "RESET_HISTORY")
                        .title(R.string.dialog_reset_history_title)
                        .content(R.string.dialog_reset_history_desc)
                        .positiveButton(android.R.string.yes)
                        .negativeButton(android.R.string.no)
                        .show()
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

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    override fun onPositive(tag: String) {
        Log.d(javaClass.name, "onPositive: $tag")
        when (tag) {
            "RESET_HISTORY" ->
                Thread(
                        Runnable {
                            val mDao = RisuscitoDatabase.getInstance(context!!).cronologiaDao()
                            mDao.emptyCronologia()
                        })
                        .start()
        }
    }

    override fun onNegative(tag: String) {}

    override fun onNeutral(tag: String) {}

    private fun startCab() {
        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
            val itemSelectedCount = (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!
                    .selectedItems
                    .size
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
//                        val iRemoved = (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!
//                                .selectedItems
//                                .size
                        mRemovedItems = (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!
                                .selectedItems
                        val iRemoved = mRemovedItems!!.size
                        Log.d(TAG, "onCabItemClicked: $iRemoved")
//                        val selectedItems = (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.selections
                        (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.deselect()
//                        mUndoHelper!!.remove(
//                                activity!!.findViewById(R.id.main_content),
//                                resources.getQuantityString(R.plurals.histories_removed, iRemoved, iRemoved),
//                                getString(android.R.string.cancel).toUpperCase(),
//                                Snackbar.LENGTH_SHORT,
//                                selectedItems)
                        Thread(
                                Runnable {
                                    val mDao = RisuscitoDatabase.getInstance(context!!).cronologiaDao()
                                    for (removedItem in mRemovedItems!!) {
                                        val cronTemp = Cronologia()
                                        cronTemp.idCanto = removedItem.id
                                        mDao.deleteCronologia(cronTemp)
                                    }
                                })
                                .start()

                        Snackbar.make(activity!!.main_content, resources.getQuantityString(R.plurals.favorites_removed, iRemoved, iRemoved), Snackbar.LENGTH_SHORT)
                                .setAction(getString(android.R.string.cancel).toUpperCase()) {
                                    Thread(
                                            Runnable {
                                                val mDao = RisuscitoDatabase.getInstance(context!!).cronologiaDao()
                                                for (removedItem in mRemovedItems!!) {
                                                    val cronTemp = Cronologia()
                                                    cronTemp.idCanto = removedItem.id
                                                    cronTemp.ultimaVisita = Date(java.lang.Long.parseLong(removedItem.timestamp!!.text.toString()))
                                                    mDao.insertCronologia(cronTemp)
                                                }

                                            })
                                            .start()
                                }.show()
                        actionModeOk = true
                        MaterialCab.destroy()
                        true
                    }
                    else -> false
                }
            }

            onDestroy { cab ->
                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                if (!actionModeOk) {
                    try {
                        (cantoAdapter.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.deselect()
                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                    }
                }
                true
            }
        }
    }

    private fun populateDb() {
        mCronologiaViewModel!!.createDb()
    }

    private fun subscribeUiHistory() {
        mCronologiaViewModel!!
                .cronologiaCanti!!
                .observe(
                        this,
                        Observer { canti ->
                            Log.d(TAG, "onChanged: ")
                            if (canti != null) {
                                mCronologiaViewModel!!.titoli.clear()
                                for (canto in canti) {
                                    val sampleItem = SimpleHistoryItem()
                                    sampleItem
                                            .withTitle(resources.getString(LUtils.getResId(canto.titolo, R.string::class.java)))
                                            .withPage(resources.getString(LUtils.getResId(canto.pagina, R.string::class.java)))
                                            .withSource(resources.getString(LUtils.getResId(canto.source, R.string::class.java)))
                                            .withColor(canto.color!!)
                                            .withTimestamp(canto.ultimaVisita?.time.toString())
                                            .withId(canto.id)
                                            .withSelectedColor(themeUtils.primaryColorDark())
                                    mCronologiaViewModel!!.titoli.add(sampleItem)
                                }
                                cantoAdapter.set(mCronologiaViewModel!!.titoli)
                                no_history!!.visibility = if (cantoAdapter.adapterItemCount > 0) View.INVISIBLE else View.VISIBLE
                                activity!!.invalidateOptionsMenu()
                            }
                        })
    }

    companion object {
        private val TAG = HistoryFragment::class.java.canonicalName
    }
}
