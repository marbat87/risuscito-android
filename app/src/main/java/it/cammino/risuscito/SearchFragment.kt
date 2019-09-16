package it.cammino.risuscito

import android.content.Intent
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.crashlytics.android.Crashlytics
import com.github.zawadz88.materialpopupmenu.ViewBoundCallback
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference

class SearchFragment : Fragment(R.layout.search_layout), SimpleDialogFragment.SimpleCallback {

    private val mViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply { putInt(Utility.TIPO_LISTA, 0) })
    }

    internal val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private lateinit var aTexts: Array<Array<String?>>

    private var listePersonalizzate: List<ListaPers>? = null
    private var mLUtils: LUtils? = null
    private var searchTask: SearchTask? = null
    private var mLastClickTime: Long = 0
    private var mMainActivity: MainActivity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_search)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0") ?: "0")
            mViewModel.advancedSearch = currentItem != 0
        }

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.fileout)
            aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Crashlytics.logException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Crashlytics.logException(e)
        }

        mLUtils = LUtils.getInstance(requireActivity())

        var sFragment = SimpleDialogFragment.findVisible(mMainActivity, SEARCH_REPLACE)
        sFragment?.setmCallback(this)
        sFragment = SimpleDialogFragment.findVisible(mMainActivity, SEARCH_REPLACE_2)
        sFragment?.setmCallback(this)

        ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all }

        subscribeUiCanti()

        textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(requireActivity().applicationContext, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(Utility.PAGINA to item.source?.getText(context), Utility.ID_CANTO to item.id))
                mLUtils?.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        cantoAdapter.onLongClickListener = { v: View, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            mViewModel.idDaAgg = item.id
            mViewModel.popupMenu(this, v, SEARCH_REPLACE, SEARCH_REPLACE_2, listePersonalizzate)
            true
        }

        cantoAdapter.setHasStableIds(true)

        matchedList.adapter = cantoAdapter
        val llm = if (mMainActivity?.isGridLayout == true)
            GridLayoutManager(context, if (mMainActivity?.hasThreeColumns == true) 3 else 2)
        else
            LinearLayoutManager(context)
        matchedList.layoutManager = llm
        val insetDivider = DividerItemDecoration(requireContext(), llm.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider.setDrawable(it) }
        matchedList.addItemDecoration(insetDivider)

        textfieldRicerca.setOnKeyListener { _, keyCode, _ ->
            var returnValue = false
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)?.hideSoftInputFromWindow(textfieldRicerca.windowToken, 0)
                returnValue = true
            }
            returnValue
        }

        textfieldRicerca.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
            ricercaStringa(s.toString())
        }

        more_options.setOnClickListener {
            val popupMenu = popupMenu {
                dropdownGravity = Gravity.END
                section {
                    customItem {
                        layoutResId = R.layout.view_custom_item_checkable
                        dismissOnSelect = false
                        viewBoundCallback = ViewBoundCallback { view ->
                            view.customItemCheckbox.isChecked = mViewModel.advancedSearch
                            view.customItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                                mViewModel.advancedSearch = isChecked
                                textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)
                                ricercaStringa(textfieldRicerca.text.toString())
                                dismissPopup()
                            }
                        }
                    }
                }
            }
            popupMenu.show(requireContext(), view)
        }

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        searchTask?.let {
            if (it.status == Status.RUNNING) it.cancel(true)
        }
        super.onDestroy()
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            SEARCH_REPLACE -> {
                listePersonalizzate?.let {
                    it[mViewModel.idListaClick]
                            .lista?.addCanto(mViewModel.idDaAgg.toString(), mViewModel.idPosizioneClick)
                    ListeUtils.updateListaPersonalizzata(this, it[mViewModel.idListaClick])
                }
            }
            SEARCH_REPLACE_2 ->
                ListeUtils.updatePosizione(this, mViewModel.idDaAgg, mViewModel.idListaDaAgg, mViewModel.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {
        // no-op
    }

    private fun ricercaStringa(s: String) {
        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            searchTask?.let {
                if (it.status == Status.RUNNING) it.cancel(true)
            }
            searchTask = SearchTask(this)
            searchTask?.execute(textfieldRicerca.text.toString(), mViewModel.advancedSearch)
        } else {
            if (s.isEmpty()) {
                searchTask?.let {
                    if (it.status == Status.RUNNING) it.cancel(true)
                }
                search_no_results.isVisible = false
                matchedList.isVisible = false
                cantoAdapter.clear()
                search_progress.isVisible = false
            }
        }
    }

    private class SearchTask internal constructor(fragment: SearchFragment) : AsyncTask<Any, Void, ArrayList<SimpleItem>>() {

        private val fragmentReference: WeakReference<SearchFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sSearchText: Any): ArrayList<SimpleItem> {

            val titoliResult = ArrayList<SimpleItem>()

            Log.d(TAG, "STRINGA: ${sSearchText[0]}")
            Log.d(TAG, "ADVANCED: ${sSearchText[1]}")
            val s = sSearchText[0] as? String ?: ""
            val advanced = sSearchText[1] as? Boolean ?: false
            fragmentReference.get()?.let { fragment ->
                if (advanced) {
                    val words = s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (aText in fragment.aTexts) {
                        if (isCancelled) return titoliResult

                        if (aText[0] == null || aText[0].equals("", ignoreCase = true)) break

                        var found = true
                        for (word in words) {
                            if (isCancelled) return titoliResult

                            if (word.trim { it <= ' ' }.length > 1) {
                                var text = word.trim { it <= ' ' }
                                text = text.toLowerCase(getSystemLocale(fragment.resources))
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(TAG, "aText[0]: ${aText[0]}")
                            fragment.mViewModel.titoli.sortedBy { it.title?.getText(fragment.context) }
                                    .filter { (aText[0] ?: "") == it.undecodedSource }
                                    .forEach {
                                        if (isCancelled) return titoliResult
                                        titoliResult.add(it)
                                    }
                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).toLowerCase(getSystemLocale(fragment.resources))
                    Log.d(TAG, "onTextChanged: stringa $stringa")
                    fragment.mViewModel.titoli.sortedBy { it.title?.getText(fragment.context) }
                            .filter {
                                Utility.removeAccents(it.title?.getText(fragment.context)
                                        ?: "").toLowerCase(getSystemLocale(fragment.resources)).contains(stringa)
                            }
                            .forEach {
                                if (isCancelled) return titoliResult
                                titoliResult.add(it.apply { filter = stringa })
                            }
                }
            }
            return titoliResult
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (isCancelled) return
            fragmentReference.get()?.search_no_results?.isVisible = false
            fragmentReference.get()?.search_progress?.isVisible = true
        }

        override fun onPostExecute(titoliResult: ArrayList<SimpleItem>) {
            super.onPostExecute(titoliResult)
            if (isCancelled) return
            fragmentReference.get()?.cantoAdapter?.set(titoliResult)
            fragmentReference.get()?.search_progress?.isVisible = false
            fragmentReference.get()?.search_no_results?.isVisible = fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0
            fragmentReference.get()?.matchedList?.isGone = fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0
        }
    }

    private fun subscribeUiCanti() {
        mViewModel.itemsResult?.observe(this) { canti ->
            mViewModel.titoli = canti.sortedBy { it.title?.getText(context) }
        }
    }

    companion object {
        private val TAG = SearchFragment::class.java.canonicalName
        private const val SEARCH_REPLACE = "SEARCH_REPLACE"
        private const val SEARCH_REPLACE_2 = "SEARCH_REPLACE_2"
    }
}
