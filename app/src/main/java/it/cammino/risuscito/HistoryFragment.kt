package it.cammino.risuscito

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.edit
import com.afollestad.materialcab.MaterialCab
import com.crashlytics.android.Crashlytics
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.OnClickListener
import com.mikepenz.fastadapter.listeners.OnLongClickListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter_extensions.UndoHelper
import com.mikepenz.iconics.IconicsDrawable
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

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class HistoryFragment : Fragment(), SimpleDialogFragment.SimpleCallback, MaterialCab.Callback {

    private var mCronologiaViewModel: CronologiaViewModel? = null
    private var cantoAdapter: FastItemAdapter<SimpleHistoryItem>? = null

    private var actionModeOk: Boolean = false

    private var mUndoHelper: UndoHelper<*>? = null
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_history, container, false)

        mCronologiaViewModel = ViewModelProviders.of(this).get(CronologiaViewModel::class.java)

        mMainActivity = activity as MainActivity?
        mMainActivity!!.setupToolbarTitle(R.string.title_activity_history)

        activity!!.material_tabs.visibility = View.GONE

        mLUtils = LUtils.getInstance(activity!!)

        mMainActivity!!.enableFab(true)
        if (!mMainActivity!!.isOnTablet) mMainActivity!!.enableBottombar(false)
        val fabClear = activity!!.fab_pager
        val icon = IconicsDrawable(activity!!)
                .icon(CommunityMaterial.Icon.cmd_eraser_variant)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2)
        fabClear.setImageDrawable(icon)
        fabClear.setOnClickListener {
            SimpleDialogFragment.Builder(
                    (activity as AppCompatActivity?)!!, this@HistoryFragment, "RESET_HISTORY")
                    .title(R.string.dialog_reset_history_title)
                    .content(R.string.dialog_reset_history_desc)
                    .positiveButton(android.R.string.yes)
                    .negativeButton(android.R.string.no)
                    .show()
        }

        if (!PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(Utility.HISTORY_OPEN, false)) {
            PreferenceManager.getDefaultSharedPreferences(activity).edit { putBoolean(Utility.HISTORY_OPEN, true) }
            val mHandler = android.os.Handler()
            mHandler.postDelayed(
                    {
                        Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                                .show()
                    },
                    250)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mOnPreClickListener = OnClickListener<SimpleHistoryItem> { _, iAdapter, item, i ->
            Log.d(TAG, "onClick: 2")
            if (mMainActivity!!.materialCab!!.isActive) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY_SELECTION)
                    return@OnClickListener true
                mLastClickTime = SystemClock.elapsedRealtime()

                cantoAdapter!!
                        .getAdapterItem(i)
                        .withSetSelected(!cantoAdapter!!.getAdapterItem(i).isSelected)
                cantoAdapter!!.notifyAdapterItemChanged(i)
                if ((cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!
                        .selectedItems
                        .size == 0)
                    mMainActivity!!.materialCab!!.finish()
                return@OnClickListener true
            }
            false
        }

        val mOnClickListener = OnClickListener<SimpleHistoryItem> { mView, _, item, _ ->
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
            if (mMainActivity!!.materialCab!!.isActive) return@OnLongClickListener true
            if (!mMainActivity!!.isOnTablet)
                activity!!.toolbar_layout!!.setExpanded(true, true)
            mMainActivity!!.materialCab!!.start(this@HistoryFragment)
            cantoAdapter!!.getAdapterItem(i).withSetSelected(true)
            cantoAdapter!!.notifyAdapterItemChanged(i)
            true
        }

        cantoAdapter = FastItemAdapter()
        cantoAdapter!!
                .withSelectable(true)
                .withMultiSelect(true)
                .withSelectOnLongClick(true)
                .withOnPreClickListener(mOnPreClickListener)
                .withOnClickListener(mOnClickListener)
                .withOnPreLongClickListener(mOnPreLongClickListener)
                .setHasStableIds(true)
        FastAdapterDiffUtil.set(cantoAdapter!!, mCronologiaViewModel!!.titoli)
        (cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.deleteAllSelectedItems()

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

        mUndoHelper = UndoHelper(
                cantoAdapter,
                UndoHelper.UndoListener { _, arrayList ->
                    Log.d(TAG, "commitRemove: " + arrayList.size)
                    arrayList
                            .map { it.item }
                            .forEach {
                                Thread(
                                        Runnable {
                                            val mDao = RisuscitoDatabase.getInstance(context!!).cronologiaDao()
                                            val cronTemp = Cronologia()
                                            cronTemp.idCanto = it.id
                                            mDao.deleteCronologia(cronTemp)
                                        })
                                        .start()
                            }
                })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        populateDb()
        subscribeUiHistory()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        IconicsMenuInflaterUtil.inflate(
                activity!!.menuInflater, activity, R.menu.help_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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

    override fun onCabCreated(cab: MaterialCab, menu: Menu): Boolean {
        Log.d(TAG, "onCabCreated: ")
        cab.setMenu(R.menu.menu_delete)
        cab.setTitle("")
        menu.findItem(R.id.action_remove_item).icon = IconicsDrawable(activity!!, CommunityMaterial.Icon.cmd_delete)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(android.R.color.white)
        actionModeOk = false
        return true
    }

    override fun onCabItemClicked(item: MenuItem): Boolean {
        Log.d(TAG, "onCabCreated: ")
        when (item.itemId) {
            R.id.action_remove_item -> {
                val iRemoved = (cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!
                        .selectedItems
                        .size
                Log.d(TAG, "onCabItemClicked: $iRemoved")
                val selectedItems = (cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.selections
                (cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.deselect()
                mUndoHelper!!.remove(
                        activity!!.findViewById(R.id.main_content),
                        resources.getQuantityString(R.plurals.histories_removed, iRemoved, iRemoved),
                        getString(android.R.string.cancel).toUpperCase(),
                        Snackbar.LENGTH_SHORT,
                        selectedItems)
//                (cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.deselect()
                actionModeOk = true
                mMainActivity!!.materialCab!!.finish()
                return true
            }
        }
        return false
    }

    override fun onCabFinished(cab: MaterialCab): Boolean {
        Log.d(TAG, "onCabFinished: $actionModeOk")
        if (!actionModeOk) {
            try {
                (cantoAdapter!!.getExtension<SelectExtension<SimpleHistoryItem>>(SelectExtension::class.java))!!.deselect()
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }

        }
        return true
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
                                FastAdapterDiffUtil.set(cantoAdapter!!, mCronologiaViewModel!!.titoli)
                                no_history!!.visibility = if (cantoAdapter!!.adapterItemCount > 0) View.INVISIBLE else View.VISIBLE
                                mMainActivity!!.enableFab(cantoAdapter!!.adapterItemCount != 0)
                            }
                        })
    }

    companion object {
        private val TAG = HistoryFragment::class.java.canonicalName
    }
}
