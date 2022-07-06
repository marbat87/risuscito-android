package it.cammino.risuscito.ui.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
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
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato
import it.cammino.risuscito.databinding.CheckableRowItemBinding
import it.cammino.risuscito.databinding.LayoutConsegnatiBinding
import it.cammino.risuscito.databinding.RowItemNotableBinding
import it.cammino.risuscito.items.CheckableItem
import it.cammino.risuscito.items.NotableItem
import it.cammino.risuscito.items.checkableItem
import it.cammino.risuscito.ui.activity.PaginaRenderActivity
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.ListChoiceDialogFragment
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.*
import it.cammino.risuscito.viewmodels.ConsegnatiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class ConsegnatiFragment : AccountMenuFragment() {

    private var cantoAdapter: FastItemAdapter<NotableItem> = FastItemAdapter()

    private val mCantiViewModel: ConsegnatiViewModel by viewModels()
    private val dialogViewModel: ListChoiceDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val selectableAdapter: FastItemAdapter<CheckableItem> = FastItemAdapter()
    private lateinit var mPopupMenu: PopupMenu
    private val selectExtension: SelectExtension<CheckableItem> = SelectExtension(selectableAdapter)
    private var mLastClickTime: Long = 0
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null
    private lateinit var passaggiArray: IntArray
    private val passaggiValues: MutableMap<Int, Int> = mutableMapOf()
    private var backCallback: OnBackPressedCallback? = null

    private var _binding: LayoutConsegnatiBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutConsegnatiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRegularFont = ResourcesCompat.getFont(
            requireContext(),
            requireContext().getTypedValueResId(R.attr.risuscito_regular_font)
        )
        mMediumFont = ResourcesCompat.getFont(
            requireContext(),
            requireContext().getTypedValueResId(R.attr.risuscito_medium_font)
        )

        mMainActivity?.setupToolbarTitle(R.string.title_activity_consegnati)
        mMainActivity?.setTabVisible(false)
        initFab()

        passaggiArray = resources.getIntArray(R.array.passaggi_values)
        for (i in passaggiArray.indices)
            passaggiValues[passaggiArray[i]] = i

        mMainActivity?.activityBottomBar?.let {
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
                            SimpleDialogFragment.show(
                                SimpleDialogFragment.Builder(
                                    CONFIRM_SAVE
                                )
                                    .title(R.string.dialog_save_consegnati_title)
                                    .icon(R.drawable.save_24px)
                                    .content(R.string.dialog_save_consegnati_desc)
                                    .positiveButton(R.string.action_salva)
                                    .negativeButton(R.string.cancel),
                                mainActivity.supportFragmentManager
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        subscribeUiConsegnati()

        cantoAdapter.onClickListener =
            { mView: View?, _: IAdapter<NotableItem>, item: NotableItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    val intent = Intent(activity, PaginaRenderActivity::class.java)
                    intent.putExtras(
                        bundleOf(
                            Utility.PAGINA to item.source?.getText(requireContext()),
                            Utility.ID_CANTO to item.id
                        )
                    )
                    mMainActivity?.startActivityWithTransition(intent, mView)
                    consume = true
                }
                consume
            }

        cantoAdapter.addClickListener<RowItemNotableBinding, NotableItem>({ binding -> binding.editNote }) { _, _, _, item ->
            openPassageModal(item)
        }

        cantoAdapter.addClickListener<RowItemNotableBinding, NotableItem>({ binding -> binding.editNoteFilled }) { _, _, _, item ->
            openPassageModal(item)
        }

        cantoAdapter.set(mCantiViewModel.titoli)
        cantoAdapter.itemFilter.filterPredicate = { item: NotableItem, constraint: CharSequence? ->
            val found = constraint?.split("|")?.filter { it.toInt() == item.numPassaggio }
            !found.isNullOrEmpty()
        }
        binding.cantiRecycler.adapter = cantoAdapter
        val glm = GridLayoutManager(context, if (context?.hasThreeColumns == true) 3 else 2)
        val llm = LinearLayoutManager(context)
        binding.cantiRecycler.layoutManager = if (context?.isGridLayout == true) glm else llm
        binding.cantiRecycler.itemAnimator = SlideRightAlphaAnimator()

        // Creating new adapter object
//        selectExtension = SelectExtension(selectableAdapter)
        selectExtension.isSelectable = true
        selectableAdapter.setHasStableIds(true)

        selectableAdapter.onPreClickListener =
            { _: View?, _: IAdapter<CheckableItem>, _: CheckableItem, position: Int ->
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
        val llm2 = if (context?.isGridLayout == true)
            GridLayoutManager(context, if (context?.hasThreeColumns == true) 3 else 2)
        else
            LinearLayoutManager(context)
        binding.chooseRecycler.layoutManager = llm2
        binding.chooseRecycler.itemAnimator = SlideRightAlphaAnimator()

        mMainActivity?.activitySearchView?.setOnQueryTextListener(object :
            SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val simplifiedString =
                    Utility.removeAccents(newText).lowercase(resources.systemLocale)
                Log.d(TAG, "onQueryTextChange: simplifiedString $simplifiedString")
                if (simplifiedString.isNotEmpty()) {
                    mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose.filter {
                        Utility.removeAccents(
                            it.title?.getText(requireContext()).orEmpty()
                        ).lowercase(resources.systemLocale).contains(simplifiedString)
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

        mMainActivity?.let {
            mPopupMenu = PopupMenu(requireContext(), it.activityToolbar, Gravity.END)
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

        mMainActivity?.let { act ->
            act.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    if (mCantiViewModel.editMode) {
                        menuInflater.inflate(R.menu.consegnati_menu_edit_mode, menu)
                        val item = menu.findItem(R.id.action_search)
                        mMainActivity?.activitySearchView?.setMenuItem(item)
                    } else {
                        menuInflater.inflate(if (mPopupMenu.menu.children.toList()
                                .any { it.isChecked }
                        ) R.menu.consegnati_menu_reset_filter else R.menu.consegnati_menu, menu)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_filter -> {
                            mPopupMenu.show()
                            return true
                        }
                        R.id.action_filter_remove -> {
                            mPopupMenu.menu.children.forEach { it.isChecked = false }
                            cantoAdapter.filter(StringUtils.EMPTY)
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
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI, false)) {
            fabIntro()
        }
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
        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.edit_24px)
        val onClick = View.OnClickListener {
            mCantiViewModel.editMode = true
            backCallback?.isEnabled = true
            lifecycleScope.launch { updateChooseList() }
            binding.selectedView.isVisible = false
            binding.chooseRecycler.isVisible = true
            enableBottombar(true)
            enableFab(false)
            val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI_2, false)) {
                managerIntro()
            }
        }
        icon?.let {
            mMainActivity?.initFab(false, it, onClick, null, false)
        }
    }

    private fun fabIntro() {
        mMainActivity?.getFab()?.let { fab ->
            val colorOnPrimary =
                MaterialColors.getColor(requireContext(), R.attr.colorOnPrimary, TAG)
            TapTargetView.showFor(
                requireActivity(), // `this` is an Activity
                TapTarget.forView(
                    fab,
                    getString(R.string.title_activity_consegnati),
                    getString(R.string.showcase_consegnati_howto)
                )
                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
                    .titleTypeface(mMediumFont) // Specify a typeface for the text
                    .titleTextColorInt(colorOnPrimary)
                    .textColorInt(colorOnPrimary)
                    .tintTarget(false) // Whether to tint the target view's color
                ,
                object :
                    TapTargetView.Listener() { // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        context?.let {
                            PreferenceManager.getDefaultSharedPreferences(it)
                                .edit { putBoolean(Utility.INTRO_CONSEGNATI, true) }
                        }
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
                    getString(R.string.showcase_consegnati_confirm)
                )
                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
                    .titleTypeface(mMediumFont) // Specify a typeface for the text
                    .titleTextColorInt(colorOnPrimary)
                    .textColorInt(colorOnPrimary),
                TapTarget.forToolbarMenuItem(
                    mMainActivity?.activityBottomBar,
                    R.id.cancel_change,
                    getString(R.string.title_activity_consegnati),
                    getString(R.string.showcase_consegnati_cancel)
                )
                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
                    .titleTypeface(mMediumFont) // Specify a typeface for the text
                    .titleTextColorInt(colorOnPrimary)
                    .textColorInt(colorOnPrimary)
            )
            .listener(
                object :
                    TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                    override fun onSequenceFinish() {
                        context?.let {
                            PreferenceManager.getDefaultSharedPreferences(it)
                                .edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
                        }
                    }

                    override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
                        // no-op
                    }

                    override fun onSequenceCanceled(tapTarget: TapTarget) {
                        context?.let {
                            PreferenceManager.getDefaultSharedPreferences(it)
                                .edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
                        }
                    }
                })
            .start()
    }

    private fun subscribeUiConsegnati() {
        mCantiViewModel.mIndexResult?.observe(viewLifecycleOwner) { cantos ->
            mCantiViewModel.titoli =
                cantos.sortedWith(compareBy(Collator.getInstance(resources.systemLocale)) {
                    it.title?.getText(requireContext())
                })
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
                    isSelected = canto.consegnato != -1
                    setTitle = Utility.getResId(canto.titolo, R.string::class.java)
                    setPage = Utility.getResId(canto.pagina, R.string::class.java)
                    setColor = canto.color
                    id = canto.id
                }
            )
        }
        mCantiViewModel.titoliChoose =
            newList.sortedWith(compareBy(Collator.getInstance(resources.systemLocale)) {
                it.title?.getText(requireContext())
            })
        mCantiViewModel.titoliChooseFiltered = mCantiViewModel.titoliChoose
        selectableAdapter.set(mCantiViewModel.titoliChooseFiltered)
    }

    private fun showProgress(show: Boolean) {
        binding.consegnatiOverlay.isVisible = show
        if (show)
            mMainActivity?.showProgressDialog()
        else
            mMainActivity?.hideProgressDialog()
    }

    private suspend fun saveConsegnati() {
        enableBottombar(false)
        showProgress(true)

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
            tempConsegnato.numPassaggio =
                withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.getNumPassaggio(id)
                }
            consegnati.add(tempConsegnato)
        }
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.emptyConsegnati()
            mDao.insertConsegnati(consegnati)
        }
        binding.chooseRecycler.isVisible = false
        binding.selectedView.isVisible = true
        enableFab(true)
        showProgress(false)
    }

    private fun openPassageModal(item: NotableItem) {
        mMainActivity?.let { activity ->
            mCantiViewModel.mIdConsegnatoSelected = item.idConsegnato
            mCantiViewModel.mIdCantoSelected = item.id
            val prefill = passaggiValues[item.numPassaggio] ?: -1
            ListChoiceDialogFragment.show(
                ListChoiceDialogFragment.Builder(
                    ADD_PASSAGE
                ).apply {
                    title = R.string.passage_title
                    listArrayId = R.array.passaggi_entries
                    initialSelection = prefill
                    positiveButton = R.string.action_salva
                    negativeButton = R.string.cancel
                }, activity.supportFragmentManager
            )
        }
    }

    companion object {
        private val TAG = ConsegnatiFragment::class.java.canonicalName
        private const val ADD_PASSAGE = "ADD_PASSAGE"
        private const val CONFIRM_SAVE = "CONFIRM_SAVE"
    }
}
