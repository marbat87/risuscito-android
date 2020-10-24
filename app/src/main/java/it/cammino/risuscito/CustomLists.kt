package it.cammino.risuscito

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.leinardi.android.speeddial.SpeedDialView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import it.cammino.risuscito.CreaListaActivity.Companion.EDIT_EXISTING_LIST
import it.cammino.risuscito.CreaListaActivity.Companion.ID_DA_MODIF
import it.cammino.risuscito.CreaListaActivity.Companion.LIST_TITLE
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.TabsLayout2Binding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.InputTextDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.viewmodels.CustomListsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomLists : Fragment() {

    private val mCustomListsViewModel: CustomListsViewModel by viewModels()
    private val inputdialogViewModel: InputTextDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var titoliListe: Array<String?> = arrayOfNulls(0)
    private var idListe: IntArray = IntArray(0)
    private var movePage: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mRegularFont: Typeface? = null
    private var tabs: TabLayout? = null
    private var mLastClickTime: Long = 0
    private val mPageChange: ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            Log.d(TAG, "onPageSelected: $position")
            Log.d(TAG, "mCustomListsViewModel.indexToShow: ${mCustomListsViewModel.indexToShow}")
            if (mCustomListsViewModel.indexToShow != position) {
                mCustomListsViewModel.indexToShow = position
                mMainActivity?.actionMode?.finish()
            }
            initFabOptions(position >= 2)
        }
    }

    private var _binding: TabsLayout2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = TabsLayout2Binding.inflate(inflater, container, false)
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
        setHasOptionsMenu(true)
        mRegularFont = ResourcesCompat.getFont(requireContext(), R.font.googlesans_regular)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_custom_lists)
        mMainActivity?.enableBottombar(false)
        mMainActivity?.setTabVisible(true)
        mMainActivity?.enableFab(true)

        movePage = savedInstanceState != null

        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        Log.d(
                TAG,
                "onCreate - INTRO_CUSTOMLISTS: " + mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false))
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false)) playIntro()

        mSectionsPagerAdapter = SectionsPagerAdapter(this)

        tabs = mMainActivity?.getMaterialTabs()
        binding.viewPager.adapter = mSectionsPagerAdapter
        tabs?.let {
            TabLayoutMediator(it, binding.viewPager) { tab, position ->
                val l = getSystemLocale(resources)
                tab.text = when (position) {
                    0 -> getString(R.string.title_activity_canti_parola).toUpperCase(l)
                    1 -> getString(R.string.title_activity_canti_eucarestia).toUpperCase(l)
                    else -> titoliListe[position - 2]?.toUpperCase(l)
                }
            }.attach()
        }
        binding.viewPager.registerOnPageChangeCallback(mPageChange)
        subscribeUiListe()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        mMainActivity?.actionMode?.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        IconicsMenuInflaterUtil.inflate(
                requireActivity().menuInflater, requireContext(), R.menu.help_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                playIntro()
                return true
            }
        }
        return false
    }

    private val startListEditForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "mCustomListsViewModel.indDaModif: ${mCustomListsViewModel.indDaModif}")
            mCustomListsViewModel.indexToShow = mCustomListsViewModel.indDaModif
            movePage = true
        }
    }

    private fun playIntro() {
        mMainActivity?.enableFab(true)
        val doneDrawable = IconicsDrawable(requireContext(), CommunityMaterial.Icon.cmd_check).apply {
            //            colorInt = Color.WHITE
            sizeDp = 24
            paddingDp = 4
        }
        mMainActivity?.getFab()?.let { fab ->
            val colorOnPrimary = MaterialColors.getColor(requireContext(), R.attr.colorOnPrimary, TAG)
            TapTargetSequence(requireActivity())
                    .continueOnCancel(true)
                    .targets(
                            TapTarget.forView(
                                    fab,
                                    getString(R.string.showcase_listepers_title),
                                    getString(R.string.showcase_listepers_desc1))
                                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                    .textTypeface(mRegularFont) // Specify a typeface for the text
                                    .titleTextColorInt(colorOnPrimary)
                                    .textColorInt(colorOnPrimary)
                                    .descriptionTextSize(15)
                                    .tintTarget(false) // Whether to tint the target view's color
                            ,
                            TapTarget.forView(
                                    fab,
                                    getString(R.string.showcase_listepers_title),
                                    getString(R.string.showcase_listepers_desc3))
                                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                    .icon(doneDrawable)
                                    .textTypeface(mRegularFont) // Specify a typeface for the text
                                    .titleTextColorInt(colorOnPrimary)
                                    .textColorInt(colorOnPrimary))
                    .listener(
                            object : TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                                override fun onSequenceFinish() {
                                    if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CUSTOMLISTS, true) }
                                }

                                override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
                                    // no-op
                                }

                                override fun onSequenceCanceled(tapTarget: TapTarget) {
                                    if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CUSTOMLISTS, true) }
                                }
                            })
                    .start()
        }
    }

    private fun subscribeUiListe() {
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
                                startListEditForResult.launch(Intent(activity, CreaListaActivity::class.java).putExtras(bundleOf(LIST_TITLE to inputdialogViewModel.outputText, EDIT_EXISTING_LIST to false)))
                                Animatoo.animateSlideUp(activity)
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
                                binding.viewPager.findViewById<Button>(R.id.button_pulisci).performClick()
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
        val icon = IconicsDrawable(requireContext(), CommunityMaterial.Icon2.cmd_plus).apply {
            colorInt = Color.WHITE
            sizeDp = 24
            paddingDp = 4
        }
        val actionListener = SpeedDialView.OnActionSelectedListener {
            when (it.id) {
                R.id.fab_pulisci -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        SimpleDialogFragment.show(SimpleDialogFragment.Builder(
                                mActivity, RESET_LIST)
                                .title(R.string.dialog_reset_list_title)
                                .content(R.string.reset_list_question)
                                .positiveButton(R.string.reset_confirm)
                                .negativeButton(R.string.cancel),
                                mActivity.supportFragmentManager)
                    }
                    true
                }
                R.id.fab_add_lista -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        InputTextDialogFragment.show(InputTextDialogFragment.Builder(
                                mActivity, NEW_LIST)
                                .title(R.string.lista_add_desc)
                                .positiveButton(R.string.create_confirm)
                                .negativeButton(R.string.cancel), mActivity.supportFragmentManager)
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
                    startListEditForResult.launch(Intent(activity, CreaListaActivity::class.java).putExtras(bundleOf(ID_DA_MODIF to idListe[binding.viewPager.currentItem - 2], EDIT_EXISTING_LIST to true)))
                    Animatoo.animateSlideUp(activity)
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

        mMainActivity?.initFab(true, icon, click, actionListener, customList)
    }

    private suspend fun deleteListDialog() {
        mMainActivity?.let { mActivity ->
            closeFabMenu()
            mCustomListsViewModel.listaDaCanc = binding.viewPager.currentItem - 2
            mCustomListsViewModel.idDaCanc = idListe[mCustomListsViewModel.listaDaCanc]
            val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
            val lista = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.getListById(mCustomListsViewModel.idDaCanc) }
            mCustomListsViewModel.titoloDaCanc = lista?.titolo
            mCustomListsViewModel.celebrazioneDaCanc = lista?.lista
            SimpleDialogFragment.show(SimpleDialogFragment.Builder(
                    mActivity,
                    DELETE_LIST)
                    .title(R.string.action_remove_list)
                    .content(R.string.delete_list_dialog)
                    .positiveButton(R.string.delete_confirm)
                    .negativeButton(R.string.cancel),
                    mActivity.supportFragmentManager)
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
                    Snackbar.LENGTH_LONG)
                    .setAction(
                            getString(R.string.cancel).toUpperCase(getSystemLocale(resources))
                    ) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                            mLastClickTime = SystemClock.elapsedRealtime()
                            mCustomListsViewModel.indexToShow = mCustomListsViewModel.listaDaCanc + 2
                            movePage = true
                            val mListePersDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
                            val listaToRestore = ListaPers()
                            listaToRestore.id = mCustomListsViewModel.idDaCanc
                            listaToRestore.titolo = mCustomListsViewModel.titoloDaCanc
                            listaToRestore.lista = mCustomListsViewModel.celebrazioneDaCanc
                            lifecycleScope.launch(Dispatchers.IO) { mListePersDao.insertLista(listaToRestore) }
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
        private val TAG = CustomLists::class.java.canonicalName
    }
}
