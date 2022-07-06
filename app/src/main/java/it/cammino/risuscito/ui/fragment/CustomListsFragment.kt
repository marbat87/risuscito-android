package it.cammino.risuscito.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.leinardi.android.speeddial.SpeedDialView
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.TabsLayoutBinding
import it.cammino.risuscito.ui.activity.CreaListaActivity
import it.cammino.risuscito.ui.activity.CreaListaActivity.Companion.EDIT_EXISTING_LIST
import it.cammino.risuscito.ui.activity.CreaListaActivity.Companion.ID_DA_MODIF
import it.cammino.risuscito.ui.activity.CreaListaActivity.Companion.LIST_TITLE
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.InputTextDialogFragment
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.getTypedValueResId
import it.cammino.risuscito.utils.extension.slideInRight
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.CustomListsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomListsFragment : AccountMenuFragment() {

    private val mCustomListsViewModel: CustomListsViewModel by viewModels()
    private val inputdialogViewModel: InputTextDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var titoliListe: Array<String?> = arrayOfNulls(0)
    private var idListe: IntArray = IntArray(0)
    private var movePage: Boolean = false
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null
    private var tabs: TabLayout? = null
    private var mLastClickTime: Long = 0
    private val mPageChange: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected: $position")
                Log.d(
                    TAG,
                    "mCustomListsViewModel.indexToShow: ${mCustomListsViewModel.indexToShow}"
                )
                if (mCustomListsViewModel.indexToShow != position) {
                    mCustomListsViewModel.indexToShow = position
                    mMainActivity?.actionMode?.finish()
                }
                initFabOptions(position >= 2)
            }
        }

    private var _binding: TabsLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        binding.viewPager.unregisterOnPageChangeCallback(mPageChange)
        _binding = null
        super.onDestroyView()
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

        mMainActivity?.setupToolbarTitle(R.string.title_activity_custom_lists)
        mMainActivity?.enableBottombar(false)
        mMainActivity?.setTabVisible(true)
        mMainActivity?.enableFab(true)

        movePage = savedInstanceState != null

        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Log.d(
            TAG,
            "onCreate - INTRO_CUSTOMLISTS: " + mSharedPrefs.getBoolean(
                Utility.INTRO_CUSTOMLISTS,
                false
            )
        )
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false)) playIntro()

        mSectionsPagerAdapter = SectionsPagerAdapter(this)

        tabs = mMainActivity?.getMaterialTabs()
        binding.viewPager.adapter = mSectionsPagerAdapter
        tabs?.let {
            TabLayoutMediator(it, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.title_activity_canti_parola)
                    1 -> getString(R.string.title_activity_canti_eucarestia)
                    else -> titoliListe[position - 2]
                }
            }.attach()
        }
        binding.viewPager.registerOnPageChangeCallback(mPageChange)

        mMainActivity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.help_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_help -> {
                        playIntro()
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        subscribeUiChanges()
    }

    private val startListEditForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "mCustomListsViewModel.indDaModif: ${mCustomListsViewModel.indDaModif}")
                mCustomListsViewModel.indexToShow = mCustomListsViewModel.indDaModif
                movePage = true
            }
        }

    private fun playIntro() {
        mMainActivity?.enableFab(true)
        mMainActivity?.getFab()?.let { fab ->
            val colorOnPrimary =
                MaterialColors.getColor(requireContext(), R.attr.colorOnPrimary, TAG)
            TapTargetSequence(requireActivity())
                .continueOnCancel(true)
                .targets(
                    TapTarget.forView(
                        fab,
                        getString(R.string.showcase_listepers_title),
                        getString(R.string.showcase_listepers_desc1)
                    )
                        .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                        .descriptionTypeface(mRegularFont) // Specify a typeface for the text
                        .titleTypeface(mMediumFont) // Specify a typeface for the text
                        .titleTextColorInt(colorOnPrimary)
                        .textColorInt(colorOnPrimary)
                        .descriptionTextSize(15)
                        .tintTarget(false) // Whether to tint the target view's color
                    ,
                    TapTarget.forView(
                        fab,
                        getString(R.string.showcase_listepers_title),
                        getString(R.string.showcase_listepers_desc3)
                    )
                        .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                        .icon(
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.check_24px
                            )
                        )
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
                                    .edit { putBoolean(Utility.INTRO_CUSTOMLISTS, true) }
                            }
                        }

                        override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
                            // no-op
                        }

                        override fun onSequenceCanceled(tapTarget: TapTarget) {
                            context?.let {
                                PreferenceManager.getDefaultSharedPreferences(it)
                                    .edit { putBoolean(Utility.INTRO_CUSTOMLISTS, true) }
                            }
                        }
                    })
                .start()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeUiChanges() {
        mCustomListsViewModel.customListResult?.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "list size ${list.size}")
            titoliListe = arrayOfNulls(list.size)
            idListe = IntArray(list.size)

            for (i in list.indices) {
                titoliListe[i] = list[i].titolo
                idListe[i] = list[i].id
            }
            mSectionsPagerAdapter?.notifyDataSetChanged()
            Log.d(TAG, "movePage: $movePage")
            Log.d(TAG, "mCustomListsViewModel.indexToShow: ${mCustomListsViewModel.indexToShow}")
            if (movePage) {
                binding.viewPager.currentItem = mCustomListsViewModel.indexToShow
                movePage = false
            }
        }

        inputdialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "inputdialogViewModel state $it")
            if (!inputdialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (inputdialogViewModel.mTag) {
                            NEW_LIST -> {
                                inputdialogViewModel.handled = true
                                mCustomListsViewModel.indDaModif = 2 + idListe.size
                                mMainActivity?.let { act ->
                                    if (OSUtils.isObySamsung()) {
                                        startListEditForResult.launch(
                                            Intent(
                                                act,
                                                CreaListaActivity::class.java
                                            ).putExtras(
                                                bundleOf(
                                                    LIST_TITLE to inputdialogViewModel.outputText,
                                                    EDIT_EXISTING_LIST to false
                                                )
                                            )
                                        )
                                        act.slideInRight()
                                    } else {
                                        act.getFab().transitionName = "shared_element_crealista"
                                        val options =
                                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                                act,
                                                act.getFab(),
                                                "shared_element_crealista" // The transition name to be matched in Activity B.
                                            )
                                        startListEditForResult.launch(
                                            Intent(
                                                act,
                                                CreaListaActivity::class.java
                                            ).putExtras(
                                                bundleOf(
                                                    LIST_TITLE to inputdialogViewModel.outputText,
                                                    EDIT_EXISTING_LIST to false
                                                )
                                            ),
                                            options
                                        )
                                    }
                                }
//                                startListEditForResult.launch(
//                                    Intent(
//                                        activity,
//                                        CreaListaActivity::class.java
//                                    ).putExtras(
//                                        bundleOf(
//                                            LIST_TITLE to inputdialogViewModel.outputText,
//                                            EDIT_EXISTING_LIST to false
//                                        )
//                                    )
//                                )
//                                Animations.enterDown(activity)
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        inputdialogViewModel.handled = true
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
                            RESET_LIST -> {
                                simpleDialogViewModel.handled = true
                                binding.viewPager.findViewById<Button>(R.id.button_pulisci)
                                    .performClick()
                            }
                            DELETE_LIST -> {
                                simpleDialogViewModel.handled = true
                                binding.viewPager.currentItem = binding.viewPager.currentItem - 1
                                lifecycleScope.launch { deleteList() }
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

    private inner class SectionsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> ListaPredefinitaFragment.newInstance(1)
                1 -> ListaPredefinitaFragment.newInstance(2)
                else -> ListaPersonalizzataFragment.newInstance(idListe[position - 2])
            }

        override fun getItemCount(): Int = 2 + titoliListe.size

    }

    private fun closeFabMenu() {
        mMainActivity?.closeFabMenu()
    }

    private fun toggleFabMenu() {
        mMainActivity?.toggleFabMenu()
    }

    fun initFabOptions(customList: Boolean) {
        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.add_24px)
        val actionListener = SpeedDialView.OnActionSelectedListener {
            when (it.id) {
                R.id.fab_pulisci -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        SimpleDialogFragment.show(
                            SimpleDialogFragment.Builder(
                                RESET_LIST
                            )
                                .title(R.string.dialog_reset_list_title)
                                .icon(R.drawable.cleaning_services_24px)
                                .content(R.string.reset_list_question)
                                .positiveButton(R.string.reset_confirm)
                                .negativeButton(R.string.cancel),
                            mActivity.supportFragmentManager
                        )
                    }
                    true
                }
                R.id.fab_add_lista -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        InputTextDialogFragment.show(
                            InputTextDialogFragment.Builder(
                                NEW_LIST
                            ).apply {
                                title = R.string.lista_add_desc
                                positiveButton = R.string.create_confirm
                                negativeButton = R.string.cancel
                            }, mActivity.supportFragmentManager
                        )
                    }
                    true
                }
                R.id.fab_condividi -> {
                    closeFabMenu()
                    binding.viewPager.findViewById<Button>(R.id.button_condividi).performClick()
                    true
                }
                R.id.fab_edit_lista -> {
                    closeFabMenu()
                    mCustomListsViewModel.indDaModif = binding.viewPager.currentItem
                    mMainActivity?.let { act ->
                        if (OSUtils.isObySamsung()) {
                            startListEditForResult.launch(
                                Intent(
                                    act,
                                    CreaListaActivity::class.java
                                ).putExtras(
                                    bundleOf(
                                        ID_DA_MODIF to idListe[binding.viewPager.currentItem - 2],
                                        EDIT_EXISTING_LIST to true
                                    )
                                )
                            )
                            act.slideInRight()
                        } else {
                            act.getFab().transitionName = "shared_element_crealista"
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                act,
                                act.getFab(),
                                "shared_element_crealista" // The transition name to be matched in Activity B.
                            )
                            startListEditForResult.launch(
                                Intent(
                                    act,
                                    CreaListaActivity::class.java
                                ).putExtras(
                                    bundleOf(
                                        ID_DA_MODIF to idListe[binding.viewPager.currentItem - 2],
                                        EDIT_EXISTING_LIST to true
                                    )
                                ),
                                options
                            )
                        }
                    }
                    true
                }
                R.id.fab_delete_lista -> {
                    lifecycleScope.launch { deleteListDialog() }
                    true
                }
                R.id.fab_condividi_file -> {
                    closeFabMenu()
                    binding.viewPager.findViewById<Button>(R.id.button_invia_file).performClick()
                    true
                }
                else -> {
                    closeFabMenu()
                    false
                }
            }
        }

        val click = View.OnClickListener {
            mMainActivity?.actionMode?.finish()
            toggleFabMenu()
        }

        icon?.let {
            mMainActivity?.initFab(true, it, click, actionListener, customList)
        }
    }

    private suspend fun deleteListDialog() {
        mMainActivity?.let { mActivity ->
            closeFabMenu()
            mCustomListsViewModel.listaDaCanc = binding.viewPager.currentItem - 2
            mCustomListsViewModel.idDaCanc = idListe[mCustomListsViewModel.listaDaCanc]
            val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
            val lista = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.getListById(mCustomListsViewModel.idDaCanc)
            }
            mCustomListsViewModel.titoloDaCanc = lista?.titolo
            mCustomListsViewModel.celebrazioneDaCanc = lista?.lista
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(
                    DELETE_LIST
                )
                    .title(R.string.action_remove_list)
                    .icon(R.drawable.delete_24px)
                    .content(R.string.delete_list_dialog)
                    .positiveButton(R.string.delete_confirm)
                    .negativeButton(R.string.cancel),
                mActivity.supportFragmentManager
            )
        }
    }

    private suspend fun deleteList() {
        val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
        val listToDelete = ListaPers()
        listToDelete.id = mCustomListsViewModel.idDaCanc
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.deleteList(listToDelete) }
        mMainActivity?.activityMainContent?.let { mainContent ->
            Snackbar.make(
                mainContent,
                getString(R.string.list_removed)
                        + mCustomListsViewModel.titoloDaCanc
                        + "'!",
                Snackbar.LENGTH_LONG
            )
                .setAction(
                    getString(R.string.cancel).uppercase(resources.systemLocale)
                ) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        mCustomListsViewModel.indexToShow = mCustomListsViewModel.listaDaCanc + 2
                        movePage = true
                        val mListePersDao =
                            RisuscitoDatabase.getInstance(requireContext()).listePersDao()
                        val listaToRestore = ListaPers()
                        listaToRestore.id = mCustomListsViewModel.idDaCanc
                        listaToRestore.titolo = mCustomListsViewModel.titoloDaCanc
                        listaToRestore.lista = mCustomListsViewModel.celebrazioneDaCanc
                        lifecycleScope.launch(Dispatchers.IO) {
                            mListePersDao.insertLista(
                                listaToRestore
                            )
                        }
                    }
                }.show()
        }
    }

    companion object {
        const val RESULT_OK = 0
        const val RESULT_KO = -1
        const val RESULT_CANCELED = -2
        private const val RESET_LIST = "RESET_LIST"
        private const val NEW_LIST = "NEW_LIST"
        private const val DELETE_LIST = "DELETE_LIST"
        private val TAG = CustomListsFragment::class.java.canonicalName
    }
}
