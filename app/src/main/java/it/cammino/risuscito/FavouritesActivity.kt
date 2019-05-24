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
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.FavoritesViewModel
import kotlinx.android.synthetic.main.activity_favourites.*
import kotlinx.android.synthetic.main.activity_main.*

class FavouritesActivity : Fragment(), SimpleDialogFragment.SimpleCallback {
    private var mFavoritesViewModel: FavoritesViewModel? = null
    private var cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private var selectExtension: SelectExtension<SimpleItem>? = null
    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_favourites, container, false)

        mFavoritesViewModel = ViewModelProviders.of(this).get(FavoritesViewModel::class.java)

        mMainActivity = activity as MainActivity?
        Log.d(TAG, "onCreateView: isOnTablet " + mMainActivity!!.isOnTablet)

        mMainActivity!!.setupToolbarTitle(R.string.title_activity_favourites)

        mMainActivity!!.setTabVisible(false)

        mLUtils = LUtils.getInstance(activity!!)

        if (!PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Utility.PREFERITI_OPEN, false)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.PREFERITI_OPEN, true) }
            Handler().postDelayed(250) {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                        .show()
            }
        }

        val sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "FAVORITES_RESET")
        sFragment?.setmCallback(this@FavouritesActivity)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity!!.enableBottombar(false)
        mMainActivity!!.enableFab(false)

        cantoAdapter.onPreClickListener = { _: View?, _: IAdapter<SimpleItem>, _: SimpleItem, position: Int ->
            var consume = false
            if (MaterialCab.isActive) {
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY_SELECTION) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    cantoAdapter
                            .getAdapterItem(position)
                            .isSelected = !cantoAdapter.getAdapterItem(position).isSelected
                    cantoAdapter.notifyAdapterItemChanged(position)
                    if (selectExtension?.selectedItems!!.size == 0)
                        destroy()
                    else
                        startCab()
                }
                consume = true
            }
            consume
        }

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                // lancia l'activity che visualizza il canto passando il parametro creato
                val intent = Intent(activity, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf("pagina" to item.source!!.text, "idCanto" to item.id))
                mLUtils!!.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        cantoAdapter.onPreLongClickListener = { _: View?, _: IAdapter<SimpleItem>, _: SimpleItem, position: Int ->
            if (!MaterialCab.isActive) {
                if (!mMainActivity!!.isOnTablet)
                    activity!!.toolbar_layout!!.setExpanded(true, true)
                cantoAdapter.getAdapterItem(position).isSelected = true
                cantoAdapter.notifyAdapterItemChanged(position)
                startCab()
            }
            true
        }

        selectExtension = SelectExtension(cantoAdapter)
        selectExtension!!.isSelectable = true
        selectExtension!!.multiSelect = true
        selectExtension!!.selectOnLongClick = true
        selectExtension!!.deleteAllSelectedItems()

        cantoAdapter.setHasStableIds(true)

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
        favouritesList!!.itemAnimator = SlideRightAlphaAnimator()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        subscribeUiFavorites()
    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        IconicsMenuInflaterUtil.inflate(
                activity!!.menuInflater, activity!!, R.menu.clean_list_menu, menu!!)
        menu.findItem(R.id.list_reset).isVisible = cantoAdapter.adapterItemCount > 0
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.list_reset -> {
                SimpleDialogFragment.Builder(
                        (activity as AppCompatActivity?)!!, this@FavouritesActivity, "FAVORITES_RESET")
                        .title(R.string.dialog_reset_favorites_title)
                        .content(R.string.dialog_reset_favorites_desc)
                        .positiveButton(R.string.clear_confirm)
                        .negativeButton(R.string.cancel)
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

    override fun onPositive(tag: String) {
        Log.d(javaClass.name, "onPositive: $tag")
        when (tag) {
            "FAVORITES_RESET" ->
                // run the sentence in a new thread
                ioThread {
                    val mDao = RisuscitoDatabase.getInstance(context!!).favoritesDao()
                    mDao.resetFavorites()
                }
        }
    }

    override fun onNegative(tag: String) {}

    private fun startCab() {
        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
            //            val itemSelectedCount = (cantoAdapter.getExtension<SelectExtension<SimpleItem>>(SelectExtension::class.java))!!
            val itemSelectedCount = selectExtension!!
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
                        ListeUtils.removeFavoritesWithUndo(this@FavouritesActivity, selectExtension!!
                                .selectedItems)
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
                        selectExtension!!.deselect()
                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                    }
                }
                true
            }
        }
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
                                cantoAdapter.set(mFavoritesViewModel!!.titoli)
                                no_favourites!!.visibility = if (cantoAdapter.adapterItemCount > 0) View.INVISIBLE else View.VISIBLE
                                activity!!.invalidateOptionsMenu()
                            }
                        })
    }

    companion object {
        private val TAG = FavouritesActivity::class.java.canonicalName
    }

}
