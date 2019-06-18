package it.cammino.risuscito

import android.content.Intent
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.github.zawadz88.materialpopupmenu.ViewBoundCallback
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.ui.LocaleManager
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.risuscito_toolbar_noelevation.*
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference

class InsertActivity : ThemeableActivity() {

    internal val cantoAdapter: FastItemAdapter<InsertItem> = FastItemAdapter()
    private lateinit var aTexts: Array<Array<String?>>

    private var listePersonalizzate: List<ListaPers>? = null
    private var mLUtils: LUtils? = null
    private var searchTask: SearchTask? = null
    private var mLastClickTime: Long = 0
    private var listaPredefinita: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0

    private lateinit var mViewModel: SimpleIndexViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_search)

        risuscito_toolbar.title = getString(R.string.title_activity_inserisci_titolo)
        setSupportActionBar(risuscito_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        listaPredefinita = bundle?.getInt(FROM_ADD) ?: 0
        idLista = bundle?.getInt(ID_LISTA) ?: 0
        listPosition = bundle?.getInt(POSITION) ?: 0

        val args = Bundle().apply { putInt(Utility.TIPO_LISTA, 3) }
        mViewModel = ViewModelProviders.of(this, ViewModelWithArgumentsFactory(application, args)).get(SimpleIndexViewModel::class.java)
        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0") ?: "0")
            mViewModel.advancedSearch = currentItem != 0
        }

        try {
//            val inputStream: InputStream = when (ThemeableActivity.getSystemLocalWrapper(resources.configuration)
            val inputStream: InputStream = when (LocaleManager.getSystemLocale(resources)
                    .language) {
                LANGUAGE_UKRAINIAN -> assets.open("fileout_uk.xml")
                LANGUAGE_ENGLISH -> assets.open("fileout_en.xml")
                else -> assets.open("fileout_new.xml")
            }
            aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Crashlytics.logException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Crashlytics.logException(e)
        }

        mLUtils = LUtils.getInstance(this)

        ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(this).listePersDao().all }

        textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<InsertItem>, item: InsertItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                if (listaPredefinita == 1) {
                    ListeUtils.addToListaDupAndFinish(this, idLista, listPosition, item.id)
                } else {
                    ListeUtils.updateListaPersonalizzataAndFinish(this, idLista, item.id, listPosition)
                }
                consume = true
            }
            consume
        }

        cantoAdapter.addEventHook(object : ClickEventHook<InsertItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return (viewHolder as? InsertItem.ViewHolder)?.mPreview
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<InsertItem>, item: InsertItem) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(applicationContext, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(Utility.PAGINA to item.source?.getText(this@InsertActivity), Utility.ID_CANTO to item.id))
                mLUtils?.startActivityWithTransition(intent)
            }
        })

        cantoAdapter.setHasStableIds(true)

        matchedList.adapter = cantoAdapter
        val glm = GridLayoutManager(this, if (mLUtils?.hasThreeColumns == true) 3 else 2)
        val llm = LinearLayoutManager(this)
        matchedList.layoutManager = if (mLUtils?.isGridLayout == true) glm else llm
        matchedList.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(this, if (mLUtils?.isGridLayout == true) glm.orientation else llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(this, R.drawable.material_inset_divider)!!)
        matchedList.addItemDecoration(insetDivider)

        textfieldRicerca.setOnKeyListener { _, keyCode, _ ->
            var returnValue = false
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                ContextCompat.getSystemService(this, InputMethodManager::class.java)?.hideSoftInputFromWindow(textfieldRicerca.windowToken, 0)
                returnValue = true
            }
            returnValue
        }

        textfieldRicerca.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        ricercaStringa(s.toString())
                    }
                }
        )

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
                    customItem {
                        layoutResId = R.layout.view_custom_item_checkable
                        dismissOnSelect = false
                        viewBoundCallback = ViewBoundCallback { view ->
                            view.customItemText.text = getString(R.string.consegnati_only)
                            view.customItemCheckbox.isChecked = mViewModel.consegnatiOnly
                            view.customItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                                mViewModel.consegnatiOnly = isChecked
                                ricercaStringa(textfieldRicerca.text.toString())
                                dismissPopup()
                            }
                        }
                    }
                }
            }
            popupMenu.show(this, it)
        }

        subscribeUiFavorites()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(CustomLists.RESULT_CANCELED)
                finish()
                Animatoo.animateShrink(this)
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        searchTask?.let {
            if (it.status == Status.RUNNING) it.cancel(true)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        setResult(CustomLists.RESULT_CANCELED)
        finish()
        Animatoo.animateShrink(this)
    }

    private fun ricercaStringa(s: String) {
        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            searchTask?.let {
                if (it.status == Status.RUNNING) it.cancel(true)
            }
            searchTask = SearchTask(this)
            searchTask?.execute(textfieldRicerca.text.toString(), mViewModel.advancedSearch, mViewModel.consegnatiOnly)
        } else {
            if (s.isEmpty()) {
                searchTask?.let {
                    if (it.status == Status.RUNNING) it.cancel(true)
                }
                search_no_results.visibility = View.GONE
                cantoAdapter.clear()
                search_progress.visibility = View.INVISIBLE
            }
        }
    }

    private class SearchTask internal constructor(fragment: InsertActivity) : AsyncTask<Any, Void, ArrayList<InsertItem>>() {

        private val fragmentReference: WeakReference<InsertActivity> = WeakReference(fragment)

        override fun doInBackground(vararg sSearchText: Any): ArrayList<InsertItem> {

            val titoliResult = ArrayList<InsertItem>()

            Log.d(TAG, "STRINGA: " + sSearchText[0])
            Log.d(TAG, "ADVANCED: " + sSearchText[1])
            Log.d(TAG, "CONSEGNATI ONLY: " + sSearchText[2])
            val s = sSearchText[0] as? String ?: ""
            val advanced = sSearchText[1] as? Boolean ?: false
            val consegnatiOnly = sSearchText[2] as? Boolean ?: false
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
//                                        getSystemLocalWrapper(
//                                                fragment.resources.configuration))
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(TAG, "aText[0]: ${aText[0]}")
                            fragment.mViewModel.titoliInsert.sortedBy { it.title?.getText(fragment) }
                                    .filter {
                                        (aText[0]
                                                ?: "") == it.undecodedSource && (!consegnatiOnly || it.consegnato == 1)
                                    }
                                    .forEach {
                                        if (isCancelled) return titoliResult
                                        titoliResult.add(it)
                                    }

                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).toLowerCase()
                    Log.d(TAG, "onTextChanged: stringa $stringa")
                    fragment.mViewModel.titoliInsert.sortedBy { it.title?.getText(fragment) }
                            .filter {
                                Utility.removeAccents(it.title?.getText(fragment)
                                        ?: "").toLowerCase().contains(stringa) && (!consegnatiOnly || it.consegnato == 1)
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
            fragmentReference.get()?.search_no_results?.visibility = View.GONE
            fragmentReference.get()?.search_progress?.visibility = View.VISIBLE
        }

        override fun onPostExecute(titoliResult: ArrayList<InsertItem>) {
            super.onPostExecute(titoliResult)
            if (isCancelled) return
            fragmentReference.get()?.cantoAdapter?.set(titoliResult)
            fragmentReference.get()?.search_progress?.visibility = View.INVISIBLE
            fragmentReference.get()?.search_no_results?.visibility = if (fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    private fun subscribeUiFavorites() {
        mViewModel.insertItemsResult?.observe(this) { canti ->
            mViewModel.titoliInsert = canti.sortedBy { it.title?.getText(this) }
        }
    }

    companion object {
        private val TAG = InsertActivity::class.java.canonicalName
        internal const val FROM_ADD = "fromAdd"
        internal const val ID_LISTA = "idLista"
        internal const val POSITION = "position"
    }
}
