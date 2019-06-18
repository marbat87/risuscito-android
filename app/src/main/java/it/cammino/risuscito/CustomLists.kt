package it.cammino.risuscito

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialcab.MaterialCab.Companion.destroy
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.leinardi.android.speeddial.SpeedDialView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.colorInt
import com.mikepenz.iconics.paddingDp
import com.mikepenz.iconics.sizeDp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.InputTextDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.CustomListsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.lista_pers_button.view.*
import kotlinx.android.synthetic.main.tabs_layout2.*

class CustomLists : Fragment(), InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback {

    private lateinit var mCustomListsViewModel: CustomListsViewModel
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
            super.onPageSelected(position)
            Log.i(TAG, "onPageSelected: $position")
            destroy()
            initFabOptions(position >= 2)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.tabs_layout2, container, false)

        mCustomListsViewModel = ViewModelProviders.of(this).get(CustomListsViewModel::class.java)

        mMainActivity = activity as? MainActivity

        mRegularFont = ResourcesCompat.getFont(requireContext(), R.font.googlesans_regular)

        mMainActivity?.setupToolbarTitle(R.string.title_activity_custom_lists)

        movePage = savedInstanceState != null

        val iFragment = InputTextDialogFragment.findVisible(mMainActivity, NEW_LIST)
        iFragment?.setmCallback(this)
        var sFragment = SimpleDialogFragment.findVisible(mMainActivity, RESET_LIST)
        sFragment?.setmCallback(this)
        sFragment = SimpleDialogFragment.findVisible(mMainActivity, DELETE_LIST)
        sFragment?.setmCallback(this)

        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        Log.d(
                TAG,
                "onCreate - INTRO_CUSTOMLISTS: " + mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false))
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false)) playIntro()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSectionsPagerAdapter = SectionsPagerAdapter(this)
        mMainActivity?.enableBottombar(false)

        mMainActivity?.setTabVisible(true)
        tabs = mMainActivity?.getMaterialTabs()
        tabs?.visibility = View.VISIBLE
        view_pager.adapter = mSectionsPagerAdapter
        tabs?.let {
            TabLayoutMediator(it, view_pager) { tab, position ->
                //                val l = ThemeableActivity.getSystemLocalWrapper(requireActivity().resources.configuration)
                val l = getSystemLocale(resources)
                tab.text = when (position) {
                    0 -> getString(R.string.title_activity_canti_parola).toUpperCase(l)
                    1 -> getString(R.string.title_activity_canti_eucarestia).toUpperCase(l)
                    else -> titoliListe[position - 2]?.toUpperCase(l)
                }
            }.attach()
        }
        view_pager.registerOnPageChangeCallback(mPageChange)
        subscribeUiListe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view_pager.unregisterOnPageChangeCallback(mPageChange)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
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

    /** @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mCustomListsViewModel.indexToShow = view_pager.currentItem
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult requestCode: $requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == TAG_CREA_LISTA || requestCode == TAG_MODIFICA_LISTA) && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "mCustomListsViewModel.indDaModif: ${mCustomListsViewModel.indDaModif}")
            mCustomListsViewModel.indexToShow = mCustomListsViewModel.indDaModif
            movePage = true
        }
        if (requestCode == ListaPredefinitaFragment.TAG_INSERT_PAROLA
                || requestCode == ListaPredefinitaFragment.TAG_INSERT_EUCARESTIA
                || requestCode == ListaPersonalizzataFragment.TAG_INSERT_PERS) {
            Log.i(TAG, "onActivityResult resultCode: $resultCode")
            if (resultCode == RESULT_OK || resultCode == RESULT_KO)
                Snackbar.make(requireActivity().main_content, if (resultCode == RESULT_OK) R.string.list_added else R.string.present_yet, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onPositive(tag: String, dialog: MaterialDialog) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            NEW_LIST -> {
                val mEditText = dialog.getInputField()
                mCustomListsViewModel.indDaModif = 2 + idListe.size
                startActivityForResult(
                        Intent(activity, CreaListaActivity::class.java).putExtras(bundleOf("titolo" to mEditText.text.toString(), "modifica" to false)), TAG_CREA_LISTA)
                Animatoo.animateSlideUp(activity)
            }
        }
    }

    override fun onNegative(tag: String, dialog: MaterialDialog) {}

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            RESET_LIST -> {
                view_pager.button_pulisci.performClick()
            }
            DELETE_LIST ->
                ioThread {
                    val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
                    val listToDelete = ListaPers()
                    listToDelete.id = mCustomListsViewModel.idDaCanc
                    mDao.deleteList(listToDelete)
                    mCustomListsViewModel.indexToShow = 0
                    movePage = true
                    Snackbar.make(
                            requireActivity().main_content,
                            getString(R.string.list_removed)
                                    + mCustomListsViewModel.titoloDaCanc
                                    + "'!",
                            Snackbar.LENGTH_LONG)
                            .setAction(
                                    getString(R.string.cancel).toUpperCase()
                            ) {
                                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                                    return@setAction
                                mLastClickTime = SystemClock.elapsedRealtime()
                                mCustomListsViewModel.indexToShow = mCustomListsViewModel.listaDaCanc + 2
                                movePage = true
                                ioThread {
                                    val mListePersDao = RisuscitoDatabase.getInstance(requireContext())
                                            .listePersDao()
                                    val listaToRestore = ListaPers()
                                    listaToRestore.id = mCustomListsViewModel.idDaCanc
                                    listaToRestore.titolo = mCustomListsViewModel.titoloDaCanc
                                    listaToRestore.lista = mCustomListsViewModel.celebrazioneDaCanc
                                    mListePersDao.insertLista(listaToRestore)
                                }
                            }.show()
                }
        }
    }

    override fun onNegative(tag: String) {}

    private fun playIntro() {
        enableFab(true)
        val doneDrawable = IconicsDrawable(requireContext(), CommunityMaterial.Icon.cmd_check)
                .sizeDp(24)
                .paddingDp(4)
        TapTargetSequence(requireActivity())
                .continueOnCancel(true)
                .targets(
                        TapTarget.forView(
                                getFab(),
                                getString(R.string.showcase_listepers_title),
                                getString(R.string.showcase_listepers_desc1))
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .descriptionTextSize(15)
                                .tintTarget(false) // Whether to tint the target view's color
                        ,
                        TapTarget.forView(
                                getFab(),
                                getString(R.string.showcase_listepers_title),
                                getString(R.string.showcase_listepers_desc3))
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .icon(doneDrawable)
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark))
                .listener(
                        object : TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                            override fun onSequenceFinish() {
                                if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CUSTOMLISTS, true) }
                            }

                            override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {}

                            override fun onSequenceCanceled(tapTarget: TapTarget) {
                                if (context != null) PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.INTRO_CUSTOMLISTS, true) }
                            }
                        })
                .start()
    }

    private fun subscribeUiListe() {
        mCustomListsViewModel.customListResult?.observe(
                this,
                Observer { list ->
                    list?.let {
                        Log.d(TAG, "list size ${list.size}")
                        titoliListe = arrayOfNulls(it.size)
                        idListe = IntArray(it.size)

                        for (i in it.indices) {
                            titoliListe[i] = it[i].titolo
                            idListe[i] = it[i].id
                        }
                        Log.i(TAG, "movePage: $movePage")
                        Log.i(TAG, "mCustomListsViewModel.indexToShow: ${mCustomListsViewModel.indexToShow}")
                        if (movePage) {
                            view_pager.currentItem = mCustomListsViewModel.indexToShow
                            mCustomListsViewModel.indexToShow = 0
                            movePage = false
                        }
                        mSectionsPagerAdapter?.notifyDataSetChanged()
                    } ?: Log.d(TAG, "lista NULLA")
                })
    }

    private inner class SectionsPagerAdapter internal constructor(fm: Fragment) : FragmentStateAdapter(fm) {

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ListaPredefinitaFragment.newInstance(1)
                1 -> ListaPredefinitaFragment.newInstance(2)
                else -> ListaPersonalizzataFragment.newInstance(idListe[position - 2])
            }
        }

        override fun getItemCount(): Int {
            return 2 + titoliListe.size
        }

    }

    private fun getFab(): FloatingActionButton {
        return mMainActivity?.getFab()!!
    }

    private fun enableFab(enabled: Boolean) {
        mMainActivity?.enableFab(enabled)
    }

    private fun closeFabMenu() {
        mMainActivity?.closeFabMenu()
    }

    private fun toggleFabMenu() {
        mMainActivity?.toggleFabMenu()
    }

    fun initFabOptions(customList: Boolean) {
        val icon = IconicsDrawable(requireContext(), CommunityMaterial.Icon2.cmd_plus)
                .colorInt(Color.RED)
                .sizeDp(24)
                .paddingDp(4)

        val actionListener = SpeedDialView.OnActionSelectedListener {
            when (it.id) {
                R.id.fab_pulisci -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        SimpleDialogFragment.Builder(
                                mActivity, this, RESET_LIST)
                                .title(R.string.dialog_reset_list_title)
                                .content(R.string.reset_list_question)
                                .positiveButton(R.string.reset_confirm)
                                .negativeButton(R.string.cancel)
                                .show()
                    }
                    true
                }
                R.id.fab_add_lista -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        InputTextDialogFragment.Builder(
                                mActivity, this, NEW_LIST)
                                .title(R.string.lista_add_desc)
                                .positiveButton(R.string.create_confirm)
                                .negativeButton(R.string.cancel)
                                .show()
                    }
                    true
                }
                R.id.fab_condividi -> {
                    closeFabMenu()
                    view_pager?.button_condividi?.performClick()
                    true
                }
                R.id.fab_edit_lista -> {
                    closeFabMenu()
                    mCustomListsViewModel.indDaModif = view_pager.currentItem
                    startActivityForResult(
                            Intent(activity, CreaListaActivity::class.java).putExtras(bundleOf("idDaModif" to idListe[view_pager.currentItem - 2], "modifica" to true)),
                            TAG_MODIFICA_LISTA)
                    Animatoo.animateSlideUp(activity)
                    true
                }
                R.id.fab_delete_lista -> {
                    mMainActivity?.let { mActivity ->
                        closeFabMenu()
                        mCustomListsViewModel.listaDaCanc = view_pager.currentItem - 2
                        mCustomListsViewModel.idDaCanc = idListe[mCustomListsViewModel.listaDaCanc]
                        ioThread {
                            val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
                            val lista = mDao.getListById(mCustomListsViewModel.idDaCanc)
                            mCustomListsViewModel.titoloDaCanc = lista?.titolo
                            mCustomListsViewModel.celebrazioneDaCanc = lista?.lista
                            SimpleDialogFragment.Builder(
                                    mActivity,
                                    this,
                                    DELETE_LIST)
                                    .title(R.string.action_remove_list)
                                    .content(R.string.delete_list_dialog)
                                    .positiveButton(R.string.delete_confirm)
                                    .negativeButton(R.string.cancel)
                                    .show()
                        }
                    }
                    true
                }
                R.id.fab_condividi_file -> {
                    closeFabMenu()
                    view_pager?.button_invia_file?.performClick()
                    true
                }
                else -> {
                    closeFabMenu()
                    false
                }
            }
        }

        val click = View.OnClickListener {
            destroy()
            toggleFabMenu()
        }

        mMainActivity?.initFab(true, icon, click, actionListener, customList)
    }

    companion object {
        const val TAG_CREA_LISTA = 111
        const val TAG_MODIFICA_LISTA = 222
        const val RESULT_OK = 0
        const val RESULT_KO = -1
        const val RESULT_CANCELED = -2
        private const val RESET_LIST = "RESET_LIST"
        private const val NEW_LIST = "NEW_LIST"
        private const val DELETE_LIST = "DELETE_LIST"
        private val TAG = CustomLists::class.java.canonicalName
    }
}
