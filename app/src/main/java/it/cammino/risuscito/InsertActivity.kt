package it.cammino.risuscito

import android.content.Intent
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.binding.listeners.addClickListener
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.ActivityInsertSearchBinding
import it.cammino.risuscito.databinding.RowItemToInsertBinding
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
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
    private lateinit var mPopupMenu: PopupMenu

    private val mViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply { putInt(Utility.TIPO_LISTA, 3) })
    }

    private lateinit var binding: ActivityInsertSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_insert_search)

        binding.risuscitoToolbar.risuscitoToolbar.title = getString(R.string.title_activity_inserisci_titolo)
        setSupportActionBar(binding.risuscitoToolbar.risuscitoToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        listaPredefinita = bundle?.getInt(FROM_ADD) ?: 0
        idLista = bundle?.getInt(ID_LISTA) ?: 0
        listPosition = bundle?.getInt(POSITION) ?: 0

        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
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

        mLUtils = LUtils.getInstance(this)

        ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(this).listePersDao().all }

        binding.searchLayout.textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)

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

        cantoAdapter.addClickListener<RowItemToInsertBinding, InsertItem>({ binding -> binding.preview }) { _, _, _, item ->
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(applicationContext, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(Utility.PAGINA to item.source?.getText(this@InsertActivity), Utility.ID_CANTO to item.id))
                mLUtils?.startActivityWithTransition(intent)
            }
        }

        cantoAdapter.setHasStableIds(true)

        binding.searchLayout.matchedList.adapter = cantoAdapter
        val glm = GridLayoutManager(this, if (mLUtils?.hasThreeColumns == true) 3 else 2)
        val llm = LinearLayoutManager(this)
        binding.searchLayout.matchedList.layoutManager = if (mLUtils?.isGridLayout == true) glm else llm
        val insetDivider = DividerItemDecoration(this, if (mLUtils?.isGridLayout == true) glm.orientation else llm.orientation)
        ContextCompat.getDrawable(this, R.drawable.material_inset_divider)?.let { insetDivider.setDrawable(it) }
        binding.searchLayout.matchedList.addItemDecoration(insetDivider)

        binding.searchLayout.textFieldRicerca.setOnKeyListener { _, keyCode, _ ->
            var returnValue = false
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                ContextCompat.getSystemService(this, InputMethodManager::class.java)?.hideSoftInputFromWindow(binding.searchLayout.textFieldRicerca.windowToken, 0)
                returnValue = true
            }
            returnValue
        }

        binding.searchLayout.textFieldRicerca.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
            ricercaStringa(s.toString())
        }

        val wrapper = ContextThemeWrapper(this, R.style.Widget_MaterialComponents_PopupMenu_Risuscito)
        mPopupMenu = if (LUtils.hasK()) PopupMenu(wrapper, binding.searchLayout.moreOptions, Gravity.END) else PopupMenu(wrapper, binding.searchLayout.moreOptions)
        mPopupMenu.inflate(R.menu.search_option_menu)
        mPopupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.advanced_search -> {
                    it.isChecked = !it.isChecked
                    mViewModel.advancedSearch = it.isChecked
                    binding.searchLayout.textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)
                    ricercaStringa(binding.searchLayout.textFieldRicerca.text.toString())
                    true
                }
                R.id.consegnaty_only -> {
                    it.isChecked = !it.isChecked
                    mViewModel.consegnatiOnly = it.isChecked
                    ricercaStringa(binding.searchLayout.textFieldRicerca.text.toString())
                    true
                }
                else -> false
            }
        }
//        if (LUtils.hasM())
//            mPopupMenu.gravity = Gravity.END

        binding.searchLayout.moreOptions.setOnClickListener {
            mPopupMenu.show()
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
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

    private fun onBackPressedAction() {
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
            searchTask?.execute(binding.searchLayout.textFieldRicerca.text.toString(), mViewModel.advancedSearch, mViewModel.consegnatiOnly)
        } else {
            if (s.isEmpty()) {
                searchTask?.let {
                    if (it.status == Status.RUNNING) it.cancel(true)
                }
                binding.searchLayout.searchNoResults.isVisible = false
                binding.searchLayout.matchedList.isVisible = false
                cantoAdapter.clear()
                binding.searchLayout.searchProgress.isVisible = false
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
                                        titoliResult.add(it.apply { filter = "" })
                                    }

                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).toLowerCase(getSystemLocale(fragment.resources))
                    Log.d(TAG, "onTextChanged: stringa $stringa")
                    fragment.mViewModel.titoliInsert.sortedBy { it.title?.getText(fragment) }
                            .filter {
                                Utility.removeAccents(it.title?.getText(fragment)
                                        ?: "").toLowerCase(getSystemLocale(fragment.resources)).contains(stringa) && (!consegnatiOnly || it.consegnato == 1)
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
            fragmentReference.get()?.binding?.searchLayout?.searchNoResults?.isVisible = false
            fragmentReference.get()?.binding?.searchLayout?.searchProgress?.isVisible = true
        }

        override fun onPostExecute(titoliResult: ArrayList<InsertItem>) {
            super.onPostExecute(titoliResult)
            if (isCancelled) return
            fragmentReference.get()?.cantoAdapter?.set(titoliResult)
            fragmentReference.get()?.binding?.searchLayout?.searchProgress?.isVisible = false
            fragmentReference.get()?.binding?.searchLayout?.searchNoResults?.isVisible = fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0
            fragmentReference.get()?.binding?.searchLayout?.matchedList?.isGone = fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0
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
