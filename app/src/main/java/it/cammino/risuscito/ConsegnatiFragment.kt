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
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferfalk.simplesearchview.SimpleSearchView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.iconics.dsl.iconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato
import it.cammino.risuscito.dialogs.ListChoiceDialogFragment
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.CheckableItem
import it.cammino.risuscito.items.NotableItem
import it.cammino.risuscito.items.checkableItem
import it.cammino.risuscito.services.ConsegnatiSaverService
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.ConsegnatiViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.common_bottom_bar.*
import kotlinx.android.synthetic.main.common_top_toolbar.*
import kotlinx.android.synthetic.main.layout_consegnati.*
import java.lang.ref.WeakReference

class ConsegnatiFragment : Fragment(R.layout.layout_consegnati), SimpleDialogFragment.SimpleCallback, ListChoiceDialogFragment.ListChoiceCallback {

    private var cantoAdapter: FastItemAdapter<NotableItem> = FastItemAdapter()

    private val mCantiViewModel: ConsegnatiViewModel by viewModels()
    private var selectableAdapter: FastItemAdapter<CheckableItem> = FastItemAdapter()
    private lateinit var mPopupMenu: PopupMenu
    private var selectExtension: SelectExtension<CheckableItem>? = null
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private var mRegularFont: Typeface? = null
    private lateinit var passaggiArray: IntArray
    private val passaggiValues: MutableMap<Int, Int> = mutableMapOf()
    private var backCallback: OnBackPressedCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRegularFont = ResourcesCompat.getFont(requireContext(), R.font.googlesans_regular)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_consegnati)
        mMainActivity?.setTabVisible(false)
        initFab()

        passaggiArray = resources.getIntArray(R.array.passaggi_values)
        for (i in passaggiArray.indices)
            passaggiValues[passaggiArray[i]] = i

        activity?.bottom_bar?.let {
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
                        backCallback?.isEnabled = false
                        chooseRecycler?.isVisible = false
                        enableBottombar(false)
                        selected_view?.isVisible = true
                        enableFab(true)
                        true
                    }
                    R.id.confirm_changes -> {
                        mMainActivity?.let { mainActivity ->
                            SimpleDialogFragment.Builder(
                                    mainActivity, this, CONFIRM_SAVE)
                                    .title(R.string.dialog_save_consegnati_title)
                                    .content(R.string.dialog_save_consegnati_desc)
                                    .positiveButton(R.string.action_salva)
                                    .negativeButton(R.string.cancel)
                                    .show()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        mLUtils = LUtils.getInstance(requireActivity())

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<NotableItem>, item: NotableItem, _: Int ->
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

        cantoAdapter.addEventHook(object : ClickEventHook<NotableItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return (viewHolder as? NotableItem.ViewHolder)?.mEditNote
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<NotableItem>, item: NotableItem) {
                mMainActivity?.let { activity ->
                    mCantiViewModel.mIdConsegnatoSelected = item.idConsegnato
                    mCantiViewModel.mIdCantoSelected = item.id
                    val prefill = passaggiValues[item.numPassaggio] ?: -1
                    ListChoiceDialogFragment.Builder(
                            activity, this@ConsegnatiFragment, ADD_PASSAGE)
                            .title(R.string.passage_title)
                            .listArrayId(R.array.passaggi_entries)
                            .initialSelection(prefill)
                            .positiveButton(R.string.action_salva)
                            .negativeButton(R.string.cancel)
                            .show()
                }
            }
        })

        cantoAdapter.set(mCantiViewModel.titoli)
        cantoAdapter.itemFilter.filterPredicate = { item: NotableItem, constraint: CharSequence? ->
            val found = constraint?.split("|")?.filter { it.toInt() == item.numPassaggio }
            !found.isNullOrEmpty()
        }
        cantiRecycler?.adapter = cantoAdapter
        val glm = GridLayoutManager(context, if (mMainActivity?.hasThreeColumns == true) 3 else 2)
        val llm = LinearLayoutManager(context)
        cantiRecycler?.layoutManager = if (mMainActivity?.isGridLayout == true) glm else llm
        val insetDivider = DividerItemDecoration(requireContext(), if (mMainActivity?.isGridLayout == true) glm.orientation else llm.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider.setDrawable(it) }
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
                return (viewHolder as? CheckableItem.ViewHolder)?.checkBox
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
        val insetDivider2 = DividerItemDecoration(requireContext(), llm2.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider2.setDrawable(it) }
        chooseRecycler?.addItemDecoration(insetDivider2)
        chooseRecycler?.itemAnimator = SlideRightAlphaAnimator()

        activity?.searchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val simplifiedString = Utility.removeAccents(newText
                        ?: "").toLowerCase(getSystemLocale(resources))
                Log.d(TAG, "onQueryTextChange: simplifiedString $simplifiedString")
                if (simplifiedString.isNotEmpty()) {
                    mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose.filter {
                        Utility.removeAccents(it.title?.getText(context)
                                ?: "").toLowerCase(getSystemLocale(resources)).contains(simplifiedString)
                    }
                    mCantiViewModel.titoliChooseFiltered.forEach { it.filter = simplifiedString }
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

        val wrapper = ContextThemeWrapper(requireContext(), R.style.Widget_MaterialComponents_PopupMenu_Risuscito)
        mPopupMenu = if (LUtils.hasK()) PopupMenu(wrapper, requireActivity().risuscito_toolbar, Gravity.END) else PopupMenu(wrapper, requireActivity().risuscito_toolbar)
        mPopupMenu.inflate(R.menu.passage_filter_menu)
        mPopupMenu.setOnMenuItemClickListener {
            it.isChecked = !it.isChecked
            // Keep the popup menu open
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            it.actionView = View(context)
            cantoAdapter.filter(mPopupMenu.menu.children.filter { item -> item.isChecked }
                    .map { item -> item.titleCondensed }
                    .joinToString("|"))
            activity?.invalidateOptionsMenu()
            false
        }
//        if (LUtils.hasM())
//            mPopupMenu.gravity = Gravity.END

        view.isFocusableInTouchMode = true
        view.requestFocus()

        ListChoiceDialogFragment.findVisible(mMainActivity, ADD_PASSAGE)?.setmCallback(this)
        SimpleDialogFragment.findVisible(mMainActivity, CONFIRM_SAVE)?.setmCallback(this)

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(
                        positionBRec, IntentFilter(ConsegnatiSaverService.BROADCAST_SINGLE_COMPLETED))
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(
                        completedBRec, IntentFilter(ConsegnatiSaverService.BROADCAST_SAVING_COMPLETED))
        chooseRecycler?.isVisible = mCantiViewModel.editMode
        enableBottombar(mCantiViewModel.editMode)
        selected_view?.isVisible = !mCantiViewModel.editMode
        enableFab(!mCantiViewModel.editMode)
        backCallback = object : OnBackPressedCallback(mCantiViewModel.editMode) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed")
                mCantiViewModel.editMode = false
                this.isEnabled = false
                mMainActivity?.expandToolbar()
                chooseRecycler?.isVisible = false
                enableBottombar(false)
                selected_view?.isVisible = true
                enableFab(true)
            }
        }
        // note that you could enable/disable the callback here as well by setting callback.isEnabled = true/false
        backCallback?.let { requireActivity().onBackPressedDispatcher.addCallback(this, it) }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (mCantiViewModel.editMode) {
            IconicsMenuInflaterUtil.inflate(
                    requireActivity().menuInflater, requireContext(), R.menu.consegnati_menu_edit_mode, menu)
            val item = menu.findItem(R.id.action_search)
            requireActivity().searchView.setMenuItem(item)
        } else {
            IconicsMenuInflaterUtil.inflate(
                    requireActivity().menuInflater, requireActivity(), if (mPopupMenu.menu.children.toList().any { it.isChecked }) R.menu.consegnati_menu_reset_filter else R.menu.consegnati_menu, menu)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                mPopupMenu.show()
                return true
            }
            R.id.action_filter_remove -> {
                mPopupMenu.menu.children.forEach { it.isChecked = false }
                cantoAdapter.filter("")
                activity?.invalidateOptionsMenu()
            }
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

    private fun enableBottombar(enabled: Boolean) {
        mMainActivity?.enableBottombar(enabled)
        if (!enabled)
            activity?.searchView?.closeSearch()
        activity?.invalidateOptionsMenu()
    }

    private fun enableFab(enabled: Boolean) {
        mMainActivity?.enableFab(enabled)
    }

    private fun initFab() {
//        val icon = IconicsDrawable(requireActivity(), CommunityMaterial.Icon2.cmd_pencil)
//                .colorInt(Color.WHITE)
//                .sizeDp(24)
//                .paddingDp(4)
        val icon = requireContext().iconicsDrawable(CommunityMaterial.Icon2.cmd_pencil) {
            color = colorInt(Color.WHITE)
            size = sizeDp(24)
            padding = sizeDp(4)
        }
        val onClick = View.OnClickListener {
            mCantiViewModel.editMode = true
            backCallback?.isEnabled = true
            UpdateChooseListTask(this).execute()
            selected_view?.isVisible = false
            chooseRecycler?.isVisible = true
            enableBottombar(true)
            enableFab(false)
            val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI_2, false)) {
                managerIntro()
            }
        }
        mMainActivity?.initFab(false, icon, onClick, null, false)
    }

    private fun fabIntro() {
        mMainActivity?.getFab()?.let { fab ->
            TapTargetView.showFor(
                    requireActivity(), // `this` is an Activity
                    TapTarget.forView(
                            fab,
                            getString(R.string.title_activity_consegnati),
                            getString(R.string.showcase_consegnati_howto))
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
    }

    private fun managerIntro() {
        TapTargetSequence(requireActivity())
                .continueOnCancel(true)
                .targets(
                        TapTarget.forToolbarMenuItem(
                                requireActivity().bottom_bar,
                                R.id.confirm_changes,
                                getString(R.string.title_activity_consegnati),
                                getString(R.string.showcase_consegnati_confirm))
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark),
                        TapTarget.forToolbarMenuItem(
                                requireActivity().bottom_bar,
                                R.id.cancel_change,
                                getString(R.string.title_activity_consegnati),
                                getString(R.string.showcase_consegnati_cancel))
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark))
                .listener(
                        object : TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                            override fun onSequenceFinish() {
                                if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
                            }

                            override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
                                // no-op
                            }

                            override fun onSequenceCanceled(tapTarget: TapTarget) {
                                if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
                            }
                        })
                .start()
    }

    private fun subscribeUiConsegnati() {
        mCantiViewModel.mIndexResult?.observe(this) { cantos ->
            mCantiViewModel.titoli = cantos.sortedWith(compareBy { it.title?.getText(context) })
            cantoAdapter.set(mCantiViewModel.titoli)
            cantoAdapter.filter(mPopupMenu.menu.children.filter { item -> item.isChecked }
                    .map { item -> item.titleCondensed }
                    .joinToString("|"))
            no_consegnati?.isInvisible = cantoAdapter.adapterItemCount > 0
            cantiRecycler.isInvisible = cantoAdapter.adapterItemCount == 0
        }
    }

    private class UpdateChooseListTask internal constructor(fragment: ConsegnatiFragment) : AsyncTask<Void, Void, Void>() {

        private val fragmentReference: WeakReference<ConsegnatiFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sUrl: Void): Void? {

            fragmentReference.get()?.let {
                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).consegnatiDao()
                val canti = mDao.choosen
                val newList = ArrayList<CheckableItem>()
                for (canto in canti) {
                    newList.add(
                            checkableItem {
                                isSelected = canto.consegnato > 0
                                setTitle = LUtils.getResId(canto.titolo, R.string::class.java)
                                setPage = LUtils.getResId(canto.pagina, R.string::class.java)
                                setColor = canto.color
                                id = canto.id
                            }
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
                chooseRecycler?.isVisible = false
                enableBottombar(false)
                selected_view?.isVisible = true
                enableFab(true)
            } catch (e: IllegalArgumentException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }

        }
    }

    override fun onPositive(tag: String, index: Int) {
        when (tag) {
            ADD_PASSAGE -> {
                val consegnato = Consegnato().apply {
                    idConsegnato = mCantiViewModel.mIdConsegnatoSelected
                    idCanto = mCantiViewModel.mIdCantoSelected
                    numPassaggio = passaggiArray[index]
                }
                val mDao = RisuscitoDatabase.getInstance(requireContext()).consegnatiDao()
                ioThread { mDao.updateConsegnato(consegnato) }
            }
        }
    }

    override fun onPositive(tag: String) {
        when (tag) {
            CONFIRM_SAVE -> {
                mCantiViewModel.editMode = false
                backCallback?.isEnabled = false
                mMainActivity?.let { activity ->
                    ProgressDialogFragment.Builder(
                            activity, null, CONSEGNATI_SAVING)
                            .content(R.string.save_consegnati_running)
                            .progressIndeterminate(false)
                            .progressMax(mCantiViewModel.titoliChoose.size)
                            .show()
                }
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
            }
        }
    }

    override fun onNegative(tag: String) {
        // no-op
    }

    companion object {
        private val TAG = ConsegnatiFragment::class.java.canonicalName
        private const val CONSEGNATI_SAVING = "CONSEGNATI_SAVING"
        private const val ADD_PASSAGE = "ADD_PASSAGE"
        private const val CONFIRM_SAVE = "CONFIRM_SAVE"
    }
}
