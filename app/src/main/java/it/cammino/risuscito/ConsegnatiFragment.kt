package it.cammino.risuscito

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferfalk.simplesearchview.SimpleSearchView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.color.MaterialColors
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.binding.listeners.addClickListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato
import it.cammino.risuscito.databinding.CheckableRowItemBinding
import it.cammino.risuscito.databinding.LayoutConsegnatiBinding
import it.cammino.risuscito.databinding.RowItemNotableBinding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.ListChoiceDialogFragment
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.CheckableItem
import it.cammino.risuscito.items.NotableItem
import it.cammino.risuscito.items.checkableItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.viewmodels.ConsegnatiViewModel
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class ConsegnatiFragment : Fragment() {

    private var cantoAdapter: FastItemAdapter<NotableItem> = FastItemAdapter()

    private val mCantiViewModel: ConsegnatiViewModel by viewModels()
    private val dialogViewModel: ListChoiceDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val activityViewModel: MainActivityViewModel by viewModels({ requireActivity() })
    private val selectableAdapter: FastItemAdapter<CheckableItem> = FastItemAdapter()
    private lateinit var mPopupMenu: PopupMenu
    private val selectExtension: SelectExtension<CheckableItem> = SelectExtension(selectableAdapter)
    private var mMainActivity: MainActivity? = null
    private var mLastClickTime: Long = 0
    private var mRegularFont: Typeface? = null
    private lateinit var passaggiArray: IntArray
    private val passaggiValues: MutableMap<Int, Int> = mutableMapOf()
    private var backCallback: OnBackPressedCallback? = null

    private var _binding: LayoutConsegnatiBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutConsegnatiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

        mMainActivity?.activityBottomBar?.let {
            it.menu?.clear()
            IconicsMenuInflaterUtil.inflate(
                    requireActivity().menuInflater, requireContext(), R.menu.consegnati, it.menu, false)
            it.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.select_none -> {
                        selectExtension.deselect()
                        true
                    }
                    R.id.select_all -> {
                        selectExtension.select()
                        true
                    }
                    R.id.cancel_change -> {
                        mCantiViewModel.editMode = false
                        backCallback?.isEnabled = false
                        binding.chooseRecycler.isVisible = false
                        enableBottombar(false)
                        binding.selectedView.isVisible = true
                        enableFab(true)
                        true
                    }
                    R.id.confirm_changes -> {
                        mMainActivity?.let { mainActivity ->
                            SimpleDialogFragment.show(SimpleDialogFragment.Builder(
                                    mainActivity, CONFIRM_SAVE)
                                    .title(R.string.dialog_save_consegnati_title)
                                    .content(R.string.dialog_save_consegnati_desc)
                                    .positiveButton(R.string.action_salva)
                                    .negativeButton(R.string.cancel),
                                    mainActivity.supportFragmentManager)
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        setHasOptionsMenu(true)
        subscribeUiConsegnati()

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<NotableItem>, item: NotableItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(activity, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(
                        Utility.PAGINA to item.source?.getText(requireContext()),
                        Utility.ID_CANTO to item.id
                ))
                activityViewModel.mLUtils.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        cantoAdapter.addClickListener<RowItemNotableBinding, NotableItem>({ binding -> binding.editNote }) { _, _, _, item ->
            mMainActivity?.let { activity ->
                mCantiViewModel.mIdConsegnatoSelected = item.idConsegnato
                mCantiViewModel.mIdCantoSelected = item.id
                val prefill = passaggiValues[item.numPassaggio] ?: -1
                ListChoiceDialogFragment.show(ListChoiceDialogFragment.Builder(
                        activity, ADD_PASSAGE)
                        .title(R.string.passage_title)
                        .listArrayId(R.array.passaggi_entries)
                        .initialSelection(prefill)
                        .positiveButton(R.string.action_salva)
                        .negativeButton(R.string.cancel), activity.supportFragmentManager)
            }
        }

        cantoAdapter.set(mCantiViewModel.titoli)
        cantoAdapter.itemFilter.filterPredicate = { item: NotableItem, constraint: CharSequence? ->
            val found = constraint?.split("|")?.filter { it.toInt() == item.numPassaggio }
            !found.isNullOrEmpty()
        }
        binding.cantiRecycler.adapter = cantoAdapter
        val glm = GridLayoutManager(context, if (activityViewModel.hasThreeColumns) 3 else 2)
        val llm = LinearLayoutManager(context)
        binding.cantiRecycler.layoutManager = if (activityViewModel.isGridLayout) glm else llm
        val insetDivider = DividerItemDecoration(requireContext(), if (activityViewModel.isGridLayout) glm.orientation else llm.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider.setDrawable(it) }
        binding.cantiRecycler.addItemDecoration(insetDivider)
        binding.cantiRecycler.itemAnimator = SlideRightAlphaAnimator()

        // Creating new adapter object
//        selectExtension = SelectExtension(selectableAdapter)
        selectExtension.isSelectable = true
        selectableAdapter.setHasStableIds(true)

        selectableAdapter.onPreClickListener = { _: View?, _: IAdapter<CheckableItem>, _: CheckableItem, position: Int ->
            selectableAdapter
                    .getAdapterItem(position)
                    .isSelected = !selectableAdapter.getAdapterItem(position).isSelected
            selectableAdapter.notifyAdapterItemChanged(position)
            true
        }

        selectableAdapter.addClickListener<CheckableRowItemBinding, CheckableItem>({ binding -> binding.checkBox }) { _, position, _, _ ->
            selectExtension.toggleSelection(position)
        }

        selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)

        binding.chooseRecycler.adapter = selectableAdapter
        val llm2 = if (activityViewModel.isGridLayout)
            GridLayoutManager(context, if (activityViewModel.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        binding.chooseRecycler.layoutManager = llm2
        val insetDivider2 = DividerItemDecoration(requireContext(), llm2.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider2.setDrawable(it) }
        binding.chooseRecycler.addItemDecoration(insetDivider2)
        binding.chooseRecycler.itemAnimator = SlideRightAlphaAnimator()

        mMainActivity?.activitySearchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val simplifiedString = Utility.removeAccents(newText
                        ?: "").toLowerCase(getSystemLocale(resources))
                Log.d(TAG, "onQueryTextChange: simplifiedString $simplifiedString")
                if (simplifiedString.isNotEmpty()) {
                    mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose.filter {
                        Utility.removeAccents(it.title?.getText(requireContext())
                                ?: "").toLowerCase(getSystemLocale(resources)).contains(simplifiedString)
                    }
                    mCantiViewModel.titoliChooseFiltered.forEach { it.filter = simplifiedString }
                    selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)
                } else
                    mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose
                return true
            }

            override fun onQueryTextCleared(): Boolean {
                mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose
                selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)
                return true
            }

        })

        val wrapper = ContextThemeWrapper(requireContext(), R.style.Widget_MaterialComponents_PopupMenu_Risuscito)
        mMainActivity?.let {
            mPopupMenu = if (LUtils.hasK()) PopupMenu(wrapper, it.activityToolbar, Gravity.END) else PopupMenu(wrapper, it.activityToolbar)
        }
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

        view.isFocusableInTouchMode = true
        view.requestFocus()

    }

    override fun onResume() {
        super.onResume()
        binding.chooseRecycler.isVisible = mCantiViewModel.editMode
        enableBottombar(mCantiViewModel.editMode)
        binding.selectedView.isVisible = !mCantiViewModel.editMode
        enableFab(!mCantiViewModel.editMode)
        backCallback = object : OnBackPressedCallback(mCantiViewModel.editMode) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed")
                mCantiViewModel.editMode = false
                this.isEnabled = false
                mMainActivity?.expandToolbar()
                binding.chooseRecycler.isVisible = false
                enableBottombar(false)
                binding.selectedView.isVisible = true
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

    override fun onDestroy() {
        super.onDestroy()
        mMainActivity?.activitySearchView?.closeSearch()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (mCantiViewModel.editMode) {
            IconicsMenuInflaterUtil.inflate(
                    requireActivity().menuInflater, requireContext(), R.menu.consegnati_menu_edit_mode, menu)
            val item = menu.findItem(R.id.action_search)
            mMainActivity?.activitySearchView?.setMenuItem(item)
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
            mMainActivity?.activitySearchView?.closeSearch()
        activity?.invalidateOptionsMenu()
    }

    private fun enableFab(enabled: Boolean) {
        mMainActivity?.enableFab(enabled)
    }

    private fun initFab() {
        val icon = IconicsDrawable(requireActivity(), CommunityMaterial.Icon2.cmd_pencil).apply {
            colorInt = Color.WHITE
            sizeDp = 24
            paddingDp = 4
        }
        val onClick = View.OnClickListener {
            mCantiViewModel.editMode = true
            backCallback?.isEnabled = true
            lifecycleScope.launch { updateChooseList() }
            binding.selectedView.isVisible = false
            binding.chooseRecycler.isVisible = true
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
            val colorOnPrimary = MaterialColors.getColor(requireContext(), R.attr.colorOnPrimary, TAG)
            TapTargetView.showFor(
                    requireActivity(), // `this` is an Activity
                    TapTarget.forView(
                            fab,
                            getString(R.string.title_activity_consegnati),
                            getString(R.string.showcase_consegnati_howto))
                            .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                            .textTypeface(mRegularFont) // Specify a typeface for the text
                            .titleTextColorInt(colorOnPrimary)
                            .textColorInt(colorOnPrimary)
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
        val colorOnPrimary = MaterialColors.getColor(requireContext(), R.attr.colorOnPrimary, TAG)
        TapTargetSequence(requireActivity())
                .continueOnCancel(true)
                .targets(
                        TapTarget.forToolbarMenuItem(
                                mMainActivity?.activityBottomBar,
                                R.id.confirm_changes,
                                getString(R.string.title_activity_consegnati),
                                getString(R.string.showcase_consegnati_confirm))
                                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColorInt(colorOnPrimary)
                                .textColorInt(colorOnPrimary),
                        TapTarget.forToolbarMenuItem(
                                mMainActivity?.activityBottomBar,
                                R.id.cancel_change,
                                getString(R.string.title_activity_consegnati),
                                getString(R.string.showcase_consegnati_cancel))
                                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColorInt(colorOnPrimary)
                                .textColorInt(colorOnPrimary))
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
        mCantiViewModel.mIndexResult?.observe(viewLifecycleOwner) { cantos ->
            mCantiViewModel.titoli = cantos.sortedWith(compareBy(Collator.getInstance(getSystemLocale(resources))) { it.title?.getText(requireContext()) })
            cantoAdapter.set(mCantiViewModel.titoli)
            cantoAdapter.filter(mPopupMenu.menu.children.filter { item -> item.isChecked }
                    .map { item -> item.titleCondensed }
                    .joinToString("|"))
            binding.noConsegnati.isInvisible = cantoAdapter.adapterItemCount > 0
            binding.cantiRecycler.isInvisible = cantoAdapter.adapterItemCount == 0
        }

        dialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "dialogViewModel state $it")
            if (!dialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        dialogViewModel.handled = true
                        val consegnato = Consegnato().apply {
                            idConsegnato = mCantiViewModel.mIdConsegnatoSelected
                            idCanto = mCantiViewModel.mIdCantoSelected
                            numPassaggio = passaggiArray[dialogViewModel.index]
                        }
                        val mDao = RisuscitoDatabase.getInstance(requireContext()).consegnatiDao()
                        lifecycleScope.launch(Dispatchers.IO) { mDao.updateConsegnato(consegnato) }
                    }
                    is DialogState.Negative -> {
                        dialogViewModel.handled = true
                    }
                }
            }
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            CONFIRM_SAVE -> {
                                simpleDialogViewModel.handled = true
                                mCantiViewModel.editMode = false
                                backCallback?.isEnabled = false
                                lifecycleScope.launch { saveConsegnati() }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }
    }

    private suspend fun updateChooseList() {
        Log.i(TAG, "updateChooseList start")
        val mDao = RisuscitoDatabase.getInstance(requireContext()).consegnatiDao()
        val canti = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.choosen }
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
        mCantiViewModel.titoliChoose = newList.sortedWith(compareBy(Collator.getInstance(getSystemLocale(resources))) { it.title?.getText(requireContext()) })
        mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose
        selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)
    }

    private suspend fun saveConsegnati() {
        mMainActivity?.let { activity ->
            ProgressDialogFragment.show(ProgressDialogFragment.Builder(
                    activity, CONSEGNATI_SAVING)
                    .content(R.string.save_consegnati_running)
                    .progressIndeterminate(true),
                    requireActivity().supportFragmentManager)

        }

        val mSelected = selectExtension.selectedItems
        val mSelectedId = mSelected.mapTo(ArrayList()) { item -> item.id }

        //IMPORTANTE PER AGGIUNGERE ALLA LISTA DEGLI ID SELEZIONATI (FILTRATI) ANCHCE QUELLI CHE AL MOMENTO NON SONO VISIBILI (MA SELEZIONATI COMUNQUE)
        mCantiViewModel.titoliChoose.forEach { item ->
            if (item.isSelected)
                if (!mSelectedId.any { i -> i == item.id })
                    mSelectedId.add(item.id)
        }

        val mDao = RisuscitoDatabase.getInstance(requireContext()).consegnatiDao()
        val consegnati = ArrayList<Consegnato>()
        for ((i, id) in mSelectedId.withIndex()) {
            val tempConsegnato = Consegnato()
            tempConsegnato.idConsegnato = i
            tempConsegnato.idCanto = id
            tempConsegnato.numPassaggio = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.getNumPassaggio(id) }
            consegnati.add(tempConsegnato)
        }
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.emptyConsegnati()
            mDao.insertConsegnati(consegnati)
        }
        val fragment = ProgressDialogFragment.findVisible(
                mMainActivity, CONSEGNATI_SAVING)
        fragment?.dismiss()
        binding.chooseRecycler.isVisible = false
        enableBottombar(false)
        binding.selectedView.isVisible = true
        enableFab(true)
    }

    companion object {
        private val TAG = ConsegnatiFragment::class.java.canonicalName
        private const val CONSEGNATI_SAVING = "CONSEGNATI_SAVING"
        private const val ADD_PASSAGE = "ADD_PASSAGE"
        private const val CONFIRM_SAVE = "CONFIRM_SAVE"
    }
}
