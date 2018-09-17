package it.cammino.risuscito

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.OnClickListener
import com.mikepenz.fastadapter.listeners.OnLongClickListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter_extensions.UndoHelper
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.FavoritesViewModel
import kotlinx.android.synthetic.main.activity_favourites.*
import kotlinx.android.synthetic.main.activity_main.*

class FavouritesActivity : Fragment(), SimpleDialogFragment.SimpleCallback {
    private var mFavoritesViewModel: FavoritesViewModel? = null
    private var cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private var mUndoHelper: UndoHelper<*>? = null

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_favourites, container, false)

        mFavoritesViewModel = ViewModelProviders.of(this).get(FavoritesViewModel::class.java)

        mMainActivity = activity as MainActivity?
        Log.d(TAG, "onCreateView: isOnTablet " + mMainActivity!!.isOnTablet)

        mMainActivity!!.setupToolbarTitle(R.string.title_activity_favourites)

        activity!!.material_tabs.visibility = View.GONE

        mLUtils = LUtils.getInstance(activity!!)

        if (!PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(Utility.PREFERITI_OPEN, false)) {
            PreferenceManager.getDefaultSharedPreferences(activity).edit { putBoolean(Utility.PREFERITI_OPEN, true) }
            val mHandler = android.os.Handler()
            mHandler.postDelayed(
                    {
                        Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                                .show()
                    },
                    250)
        }

        val sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "FAVORITES_RESET")
        sFragment?.setmCallback(this@FavouritesActivity)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity!!.enableBottombar(false)
        mMainActivity!!.enableFab(false)

        val mOnPreClickListener = OnClickListener<SimpleItem> { _, _, _, i ->
            Log.d(TAG, "onClick: 2")
            if (MaterialCab.isActive) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY_SELECTION)
                    return@OnClickListener true
                mLastClickTime = SystemClock.elapsedRealtime()
                cantoAdapter
                        .getAdapterItem(i)
                        .withSetSelected(!cantoAdapter.getAdapterItem(i).isSelected)
                cantoAdapter.notifyAdapterItemChanged(i)
                if ((cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))?.selectedItems!!.size == 0)
                    MaterialCab.destroy()
                else
                    startCab()
                return@OnClickListener true
            }
            false
        }

        val mOnClickListener = OnClickListener<SimpleItem> { _, _, item, _ ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener true
            mLastClickTime = SystemClock.elapsedRealtime()
            val bundle = Bundle()
            bundle.putCharSequence("pagina", item.source!!.text)
            bundle.putInt("idCanto", item.id)

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle)
            true
        }

        val mOnPreLongClickListener = OnLongClickListener<SimpleItem> { _, _, _, i ->
            if (MaterialCab.isActive) return@OnLongClickListener true
            if (!mMainActivity!!.isOnTablet) {
                activity!!.toolbar_layout!!.setExpanded(true, true)
            }
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
        FastAdapterDiffUtil.set(cantoAdapter, mFavoritesViewModel!!.titoli)
        (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!.deleteAllSelectedItems()

        favouritesList!!.adapter = cantoAdapter
        val llm = if (mMainActivity!!.isGridLayout)
            GridLayoutManager(context, if (mMainActivity!!.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        favouritesList!!.layoutManager = llm
        favouritesList!!.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(context!!, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(context!!, R.drawable.material_inset_divider)!!)
        favouritesList!!.addItemDecoration(insetDivider)
        favouritesList!!.itemAnimator = SlideLeftAlphaAnimator()

        mUndoHelper = UndoHelper(
                cantoAdapter,
                UndoHelper.UndoListener { _, arrayList ->
                    Log.d(TAG, "commitRemove: " + arrayList.size)
                    arrayList
                            .map { it.item }
                            .forEach {
                                Thread(
                                        Runnable {
                                            val mDao = RisuscitoDatabase.getInstance(context!!).favoritesDao()
                                            mDao.removeFavorite(it.id)
                                        })
                                        .start()
                            }
                })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        populateDb()
        subscribeUiFavorites()
    }

    override fun onDestroy() {
        if (MaterialCab.isActive) MaterialCab.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        IconicsMenuInflaterUtil.inflate(
                activity!!.menuInflater, activity, R.menu.clean_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.list_reset -> {
                SimpleDialogFragment.Builder(
                        (activity as AppCompatActivity?)!!, this@FavouritesActivity, "FAVORITES_RESET")
                        .title(R.string.dialog_reset_favorites_title)
                        .content(R.string.dialog_reset_favorites_desc)
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
            "FAVORITES_RESET" ->
                // run the sentence in a new thread
                Thread(
                        Runnable {
                            val mDao = RisuscitoDatabase.getInstance(context!!).favoritesDao()
                            mDao.resetFavorites()
                        })
                        .start()
        }
    }

    override fun onNegative(tag: String) {}

    override fun onNeutral(tag: String) {}

    private fun startCab() {
        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
            val itemSelectedCount = (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!
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
                        val iRemoved = (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!
                                .selectedItems
                                .size
                        Log.d(TAG, "onCabItemClicked: $iRemoved")
                        val selectedItems = (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!.selections
                        (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!.deselect()

                        mUndoHelper!!.remove(
                                activity!!.main_content,
                                resources.getQuantityString(R.plurals.favorites_removed, iRemoved, iRemoved),
                                getString(android.R.string.cancel).toUpperCase(),
                                Snackbar.LENGTH_SHORT,
                                selectedItems)
                        actionModeOk = true
                        MaterialCab.destroy()
                        true
                    }
                    else -> false
                }
            }

            onDestroy {
                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                if (!actionModeOk) {
                    try {
                        (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!.deselect()
                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                    }
                }
                true
            }
        }
    }


    private fun populateDb() {
        mFavoritesViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mFavoritesViewModel!!
                .mFavoritesResult!!
                .observe(
                        this,
                        Observer { canti ->
                            Log.d(TAG, "onChanged: a")
                            if (canti != null) {
                                val newList = ArrayList<SimpleItem>()
                                for (canto in canti) {
                                    newList.add(
                                            SimpleItem()
                                                    .withTitle(resources.getString(LUtils.getResId(canto.titolo, R.string::class.java)))
                                                    .withPage(resources.getString(LUtils.getResId(canto.pagina, R.string::class.java)))
                                                    .withSource(resources.getString(LUtils.getResId(canto.source, R.string::class.java)))
                                                    .withColor(canto.color!!)
                                                    .withId(canto.id)
                                                    .withSelectedColor(themeUtils.primaryColorDark())
                                    )
                                }
                                mFavoritesViewModel!!.titoli = newList.sortedWith(compareBy { it.title.toString() })
                                FastAdapterDiffUtil.set(cantoAdapter, mFavoritesViewModel!!.titoli)
                                no_favourites!!.visibility = if (cantoAdapter.adapterItemCount > 0) View.INVISIBLE else View.VISIBLE
                            }
                        })
    }

    companion object {
        private val TAG = FavouritesActivity::class.java.canonicalName
    }

}
