package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferfalk.simplesearchview.SimpleSearchView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.colorInt
import com.mikepenz.iconics.paddingDp
import com.mikepenz.iconics.sizeDp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.CheckableItem
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.services.ConsegnatiSaverService
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.ConsegnatiViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.common_bottom_bar.*
import kotlinx.android.synthetic.main.layout_consegnati.*
import java.lang.ref.WeakReference

class ConsegnatiFragment : Fragment(), SimpleDialogFragment.SimpleCallback {

    private var cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()

    private lateinit var mCantiViewModel: ConsegnatiViewModel
    private var rootView: View? = null
    private var selectableAdapter: FastItemAdapter<CheckableItem> = FastItemAdapter()
    private var selectExtension: SelectExtension<CheckableItem>? = null
    private var mBottomBar: BottomAppBar? = null
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private var mRegularFont: Typeface? = null
    private val positionBRec = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Implement UI change code here once notification is received
            try {
                Log.d(javaClass.name, ConsegnatiSaverService.BROADCAST_SINGLE_COMPLETED)
                Log.d(
                        javaClass.name,
                        "$ConsegnatiSaverService.DATA_DONE: ${intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0)}")
                val fragment = ProgressDialogFragment.findVisible(
                        mMainActivity, CONSEGNATI_SAVING)
                fragment?.setProgress(intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0))
            } catch (e: IllegalArgumentException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }

        }
    }
    private val completedBRec = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Implement UI change code here once notification is received
            try {
                Log.d(javaClass.name, "BROADCAST_SAVING_COMPLETED")
                val fragment = ProgressDialogFragment.findVisible(
                        mMainActivity, CONSEGNATI_SAVING)
                fragment?.dismiss()
                chooseRecycler?.visibility = View.GONE
                enableBottombar(false)
                selected_view?.visibility = View.VISIBLE
                enableFab(true)
            } catch (e: IllegalArgumentException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }

        }
    }

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.layout_consegnati, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get(ConsegnatiViewModel::class.java)

        mMainActivity = activity as? MainActivity

        mRegularFont = ResourcesCompat.getFont(mMainActivity!!, R.font.googlesans_regular)

        mMainActivity?.setupToolbarTitle(R.string.title_activity_consegnati)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBottomBar = if (mMainActivity?.isOnTablet == true)
            bottom_bar
        else
            activity?.bottom_bar

        mMainActivity?.setTabVisible(false)
        initFab()

        mBottomBar?.let {
            it.menu?.clear()
            IconicsMenuInflaterUtil.inflate(
                    requireActivity().menuInflater, requireContext(), R.menu.consegnati, it.menu, false)
            it.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.select_none -> {
                        selectExtension?.deselect()
                        true
                    }
                    R.id.select_all -> {
                        selectExtension?.select()
                        true
                    }
                    R.id.cancel_change -> {
                        mCantiViewModel.editMode = false
                        chooseRecycler?.visibility = View.INVISIBLE
                        enableBottombar(false)
                        selected_view?.visibility = View.VISIBLE
                        enableFab(true)
                        true
                    }
                    R.id.confirm_changes -> {
                        mCantiViewModel.editMode = false
                        ProgressDialogFragment.Builder(
                                mMainActivity!!, null, CONSEGNATI_SAVING)
                                .content(R.string.save_consegnati_running)
                                .progressIndeterminate(false)
                                .progressMax(mCantiViewModel.titoliChoose.size)
                                .show()

                        val mSelected = selectExtension?.selectedItems
                        val mSelectedId = mSelected?.mapTo(ArrayList()) { item -> item.id }

                        //IMPORTANTE PER AGGIUNGERE ALLA LISTA DEGLI ID SELEZIONATI (FILTRATI) ANCHCE QUELLI CHE AL MOMENTO NON SONO VISIBILI (MA SELEZIONATI COMUNQUE)
                        mCantiViewModel.titoliChoose.forEach { item ->
                            if (item.isSelected)
                                if (mSelectedId?.any { i -> i == item.id } != true)
                                    mSelectedId?.add(item.id)
                        }

                        val intent = Intent(requireActivity().applicationContext, ConsegnatiSaverService::class.java)
                        intent.putIntegerArrayListExtra(ConsegnatiSaverService.IDS_CONSEGNATI, mSelectedId)
                        requireActivity().applicationContext.startService(intent)
                        true
                    }
                    else -> false
                }
            }
        }

        mLUtils = LUtils.getInstance(requireActivity())

        mBottomBar?.setBackgroundColor(themeUtils.primaryColor())

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(activity, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(
                        Utility.PAGINA to item.source?.getText(context),
                        Utility.ID_CANTO to item.id
                ))
                mLUtils?.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        cantoAdapter.set(mCantiViewModel.titoli)

        cantiRecycler?.adapter = cantoAdapter
        val glm = GridLayoutManager(context, if (mMainActivity?.hasThreeColumns == true) 3 else 2)
        val llm = LinearLayoutManager(context)
        cantiRecycler?.layoutManager = if (mMainActivity?.isGridLayout == true) glm else llm
        cantiRecycler?.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(requireContext(), if (mMainActivity?.isGridLayout == true) glm.orientation else llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)!!)
        cantiRecycler?.addItemDecoration(insetDivider)
        cantiRecycler?.itemAnimator = SlideRightAlphaAnimator()

        // Creating new adapter object
        selectExtension = SelectExtension(selectableAdapter)
        selectExtension?.isSelectable = true
        selectableAdapter.setHasStableIds(true)

        selectableAdapter.onPreClickListener = { _: View?, _: IAdapter<CheckableItem>, _: CheckableItem, position: Int ->
            selectableAdapter
                    .getAdapterItem(position)
                    .isSelected = !selectableAdapter.getAdapterItem(position).isSelected
            selectableAdapter.notifyAdapterItemChanged(position)
            true
        }

        selectableAdapter.addEventHook(object : ClickEventHook<CheckableItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return (viewHolder as? CheckableItem.ViewHolder)?.checkBox as View
            }

            override fun onClick(
                    v: View, position: Int, fastAdapter: FastAdapter<CheckableItem>, item: CheckableItem) {
                selectExtension?.toggleSelection(position)
            }
        })

        selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)

        chooseRecycler?.adapter = selectableAdapter
        val llm2 = if (mMainActivity?.isGridLayout == true)
            GridLayoutManager(context, if (mMainActivity?.hasThreeColumns == true) 3 else 2)
        else
            LinearLayoutManager(context)
        chooseRecycler?.layoutManager = llm2
        chooseRecycler?.setHasFixedSize(true)
        val insetDivider2 = DividerItemDecoration(requireContext(), llm2.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)!!)
        chooseRecycler?.addItemDecoration(insetDivider2)
        chooseRecycler?.itemAnimator = SlideRightAlphaAnimator()

        activity?.searchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val simplifiedString = Utility.removeAccents(newText ?: "").toLowerCase()
                Log.d(TAG, "onQueryTextChange: simplifiedString $simplifiedString")
                if (simplifiedString.isNotEmpty()) {
                    mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose.filter { Utility.removeAccents(it.title?.text.toString()).toLowerCase().contains(simplifiedString) }
                    selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)
                } else
                    mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose.sortedWith(compareBy { it.title.toString() })
                return true
            }

            override fun onQueryTextCleared(): Boolean {
                mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose.sortedWith(compareBy { it.title.toString() })
                selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)
                return true
            }

        })

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, _ ->
            var managed = false
            if (keyCode == KeyEvent.KEYCODE_BACK && mCantiViewModel.editMode) {
                mCantiViewModel.editMode = false
                chooseRecycler?.visibility = View.INVISIBLE
                enableBottombar(false)
                selected_view?.visibility = View.VISIBLE
                enableFab(true)
                managed = true
            }
            managed
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(
                        positionBRec, IntentFilter(ConsegnatiSaverService.BROADCAST_SINGLE_COMPLETED))
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(
                        completedBRec, IntentFilter(ConsegnatiSaverService.BROADCAST_SAVING_COMPLETED))
        if (mCantiViewModel.editMode) {
            chooseRecycler?.visibility = View.VISIBLE
            enableBottombar(true)
            selected_view?.visibility = View.INVISIBLE
            enableFab(false)
        } else {
            chooseRecycler?.visibility = View.GONE
            enableBottombar(false)
            selected_view?.visibility = View.VISIBLE
            enableFab(true)
        }
        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI, false)) {
            fabIntro()
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(positionBRec)
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(completedBRec)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        subscribeUiConsegnati()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.searchView?.closeSearch()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.let {
            if (mCantiViewModel.editMode) {
                IconicsMenuInflaterUtil.inflate(
                        requireActivity().menuInflater, requireContext(), R.menu.consegnati_menu, it)
                val item = it.findItem(R.id.action_search)
                requireActivity().searchView.setMenuItem(item)
            } else
                IconicsMenuInflaterUtil.inflate(
                        requireActivity().menuInflater, requireActivity(), R.menu.help_menu, it)

        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_help -> {
                if (mCantiViewModel.editMode)
                    managerIntro()
                else
                    fabIntro()
                return true
            }
        }
        return false
    }

    private fun getFab(): FloatingActionButton {
        return mMainActivity?.getFab()!!
    }

    private fun enableBottombar(enabled: Boolean) {
        if (mMainActivity?.isOnTablet == true)
            mBottomBar?.visibility = if (enabled) View.VISIBLE else View.GONE
        else
            mMainActivity?.enableBottombar(enabled)
        if (!enabled)
            activity?.searchView?.closeSearch()
        activity?.invalidateOptionsMenu()
    }

    private fun enableFab(enabled: Boolean) {
        mMainActivity?.enableFab(enabled)
    }

    private fun initFab() {
        val icon = IconicsDrawable(requireActivity())
                .icon(CommunityMaterial.Icon2.cmd_pencil)
                .colorInt(Color.WHITE)
                .sizeDp(24)
                .paddingDp(4)
        val onClick = View.OnClickListener {
            mCantiViewModel.editMode = true
            UpdateChooseListTask(this).execute()
            selected_view?.visibility = View.INVISIBLE
            chooseRecycler?.visibility = View.VISIBLE
            enableBottombar(true)
            enableFab(false)
            val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI_2, false)) {
                managerIntro()
            }
        }
        mMainActivity?.initFab(false, icon, onClick, null, false)
    }

    override fun onPositive(tag: String) {}

    override fun onNegative(tag: String) {}

    private fun fabIntro() {
        TapTargetView.showFor(
                requireActivity(), // `this` is an Activity
                TapTarget.forView(
                        getFab(),
                        getString(R.string.title_activity_consegnati),
                        getString(R.string.showcase_consegnati_howto))
                        .outerCircleColorInt(
                                themeUtils.primaryColor()) // Specify a color for the outer circle
                        .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                        .textTypeface(mRegularFont) // Specify a typeface for the text
                        .titleTextColor(R.color.primary_text_default_material_dark)
                        .textColor(R.color.secondary_text_default_material_dark)
                        .tintTarget(false) // Whether to tint the target view's color
                ,
                object : TapTargetView.Listener() { // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CONSEGNATI, true) }
                    }
                })
    }

    private fun managerIntro() {
        TapTargetSequence(requireActivity())
                .continueOnCancel(true)
                .targets(
                        TapTarget.forToolbarMenuItem(
                                mBottomBar,
                                R.id.confirm_changes,
                                getString(R.string.title_activity_consegnati),
                                getString(R.string.showcase_consegnati_confirm))
                                .outerCircleColorInt(
                                        themeUtils.primaryColor()) // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark),
                        TapTarget.forToolbarMenuItem(
                                mBottomBar,
                                R.id.cancel_change,
                                getString(R.string.title_activity_consegnati),
                                getString(R.string.showcase_consegnati_cancel))
                                .outerCircleColorInt(
                                        themeUtils.primaryColor()) // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark))
                .listener(
                        object : TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                            override fun onSequenceFinish() {
                                if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
                            }

                            override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {}

                            override fun onSequenceCanceled(tapTarget: TapTarget) {
                                if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
                            }
                        })
                .start()
    }

    private fun subscribeUiConsegnati() {
        mCantiViewModel
                .mIndexResult?.observe(
                this,
                Observer { cantos ->
                    Log.d(TAG, "onChanged: " + (cantos != null))
                    mCantiViewModel.titoli = cantos.sortedWith(compareBy { it.title?.getText(context) })
                    cantoAdapter.set(mCantiViewModel.titoli)
                    no_consegnati?.visibility = if (cantoAdapter.adapterItemCount > 0) View.INVISIBLE else View.VISIBLE
                })
    }

    private class UpdateChooseListTask internal constructor(fragment: ConsegnatiFragment) : AsyncTask<Void, Void, Void>() {

        private val fragmentReference: WeakReference<ConsegnatiFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sUrl: Void): Void? {

            fragmentReference.get()?.let {
                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).consegnatiDao()
                val canti = mDao.choosen
                val newList = ArrayList<CheckableItem>()
                for (canto in canti) {
                    val checkableItem = CheckableItem()
                    checkableItem.isSelected = canto.consegnato > 0
                    newList.add(
                            checkableItem
                                    .withTitle(it.resources.getString(LUtils.getResId(canto.titolo, R.string::class.java)))
                                    .withPage(it.resources.getString(LUtils.getResId(canto.pagina, R.string::class.java)))
                                    .withColor(canto.color ?: Canto.BIANCO)
                                    .withId(canto.id)
                    )
                }
                it.mCantiViewModel.titoliChoose = newList.sortedWith(compareBy { item -> item.title.toString() })
                it.mCantiViewModel.titoliChooseFiltered = it.mCantiViewModel.titoliChoose.sortedWith(compareBy { item -> item.title.toString() })
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            fragmentReference.get()?.let {
                it.selectableAdapter.set(it.mCantiViewModel.titoliChooseFiltered)
            }
        }
    }

    companion object {
        private val TAG = ConsegnatiFragment::class.java.canonicalName
        private const val CONSEGNATI_SAVING = "CONSEGNATI_SAVING"
    }
}
