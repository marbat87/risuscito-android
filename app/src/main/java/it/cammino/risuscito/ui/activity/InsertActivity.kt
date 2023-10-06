package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.binding.listeners.addClickListener
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.ActivityInsertSearchBinding
import it.cammino.risuscito.databinding.RowItemToInsertBinding
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.utils.CantiXmlParser
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.setEnterTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.Collator

class InsertActivity : ThemeableActivity() {

    private val cantoAdapter: FastItemAdapter<InsertItem> = FastItemAdapter()

    private var mLastClickTime: Long = 0
    private var listaPredefinita: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0

    private var job: Job = Job()

    private val simpleIndexViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply { putInt(Utility.TIPO_LISTA, 3) })
    }

    private lateinit var binding: ActivityInsertSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
        super.onCreate(savedInstanceState)
        binding = ActivityInsertSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.risuscitoToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        listaPredefinita = bundle?.getInt(FROM_ADD) ?: 0
        idLista = bundle?.getInt(ID_LISTA) ?: 0
        listPosition = bundle?.getInt(POSITION) ?: 0

        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0") ?: "0")
            simpleIndexViewModel.advancedSearch = currentItem != 0
        }

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.fileout)
            simpleIndexViewModel.aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        }

//        binding.textBoxRicerca.hint =
//            if (simpleIndexViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(
//                R.string.fast_search_subtitle
//            )

        cantoAdapter.onClickListener =
            { _: View?, _: IAdapter<InsertItem>, item: InsertItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    if (listaPredefinita == 1) {
                        ListeUtils.addToListaDupAndFinish(this, idLista, listPosition, item.id)
                    } else {
                        ListeUtils.updateListaPersonalizzataAndFinish(
                            this,
                            idLista,
                            item.id,
                            listPosition
                        )
                    }
                    consume = true
                }
                consume
            }

        cantoAdapter.addClickListener<RowItemToInsertBinding, InsertItem>({ binding -> binding.preview }) { _, _, _, item ->
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                openCanto(TAG, item.id, item.source?.getText(this@InsertActivity), true)
            }
        }

        cantoAdapter.setHasStableIds(true)

        binding.matchedList.adapter = cantoAdapter
        val glm = GridLayoutManager(this, 2)
        val llm = LinearLayoutManager(this)
        binding.matchedList.layoutManager = if (isGridLayout) glm else llm

        binding.searchView
            .editText
            .setOnEditorActionListener { _, keyCode, _ ->
                var returnValue = false
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    // to hide soft keyboard
                    ContextCompat.getSystemService(this, InputMethodManager::class.java)
                        ?.hideSoftInputFromWindow(
                            binding.searchView
                                .editText.windowToken, 0
                        )
                    returnValue = true
                }
                returnValue
            }

        binding.searchView
            .editText.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
                job.cancel()
                ricercaStringa(s.toString())
            }

        binding.advancedSearchChip.isChecked = simpleIndexViewModel.advancedSearch
        binding.advancedSearchChip.setOnCheckedChangeListener { _, checked ->
            simpleIndexViewModel.advancedSearch = checked
            job.cancel()
            ricercaStringa(binding.searchView.text.toString())
        }

        binding.consegnatiOnlySearchChip.isChecked =
            simpleIndexViewModel.consegnatiOnly
        binding.consegnatiOnlySearchChip.setOnCheckedChangeListener { _, checked ->
            simpleIndexViewModel.consegnatiOnly = checked
            job.cancel()
            ricercaStringa(binding.searchView.text.toString())
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        binding.searchView.show()
        binding.searchView.toolbar.setNavigationOnClickListener {
            onBackPressedAction()
        }

        subscribeObservers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(CustomListsFragment.RESULT_CANCELED)
                finishAfterTransitionWrapper()
                return true
            }
        }
        return false
    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        setResult(CustomListsFragment.RESULT_CANCELED)
        finishAfterTransitionWrapper()
    }

    private fun ricercaStringa(s: String) {
        job = lifecycleScope.launch {
            // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
            if (s.trim { it <= ' ' }.length >= 3) {
                binding.searchNoResults.isVisible = false
                binding.searchProgress.isVisible = true
                val titoliResult = ArrayList<InsertItem>()

                Log.d(TAG, "performInsertSearch STRINGA: $s")
                Log.d(TAG, "performInsertSearch ADVANCED: ${simpleIndexViewModel.advancedSearch}")
                Log.d(
                    TAG,
                    "performInsertSearch CONSEGNATI ONLY: ${simpleIndexViewModel.consegnatiOnly}"
                )
                if (simpleIndexViewModel.advancedSearch) {
                    val words =
                        s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (aText in simpleIndexViewModel.aTexts) {
                        if (!isActive) return@launch

                        if (aText[0] == null || aText[0].isNullOrEmpty()) break

                        var found = true
                        for (word in words) {
                            if (!isActive) return@launch

                            if (word.trim { it <= ' ' }.length > 1) {
                                var text = word.trim { it <= ' ' }
                                text = text.lowercase(systemLocale)
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(TAG, "aText[0]: ${aText[0]}")
                            simpleIndexViewModel.titoliInsert
                                .filter {
                                    (aText[0].orEmpty()) == it.undecodedSource && (!simpleIndexViewModel.consegnatiOnly || it.consegnato == 1)
                                }
                                .forEach {
                                    if (!isActive) return@launch
                                    titoliResult.add(it.apply { filter = StringUtils.EMPTY })
                                }

                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).lowercase(systemLocale)
                    Log.d(TAG, "performInsertSearch onTextChanged: stringa $stringa")
                    simpleIndexViewModel.titoliInsert
                        .filter {
                            Utility.removeAccents(
                                it.title?.getText(this@InsertActivity).orEmpty()
                            ).lowercase(systemLocale)
                                .contains(stringa) && (!simpleIndexViewModel.consegnatiOnly || it.consegnato == 1)
                        }
                        .forEach {
                            if (!isActive) return@launch
                            titoliResult.add(it.apply { filter = stringa })
                        }
                }
                if (isActive) {
                    cantoAdapter.set(
                        titoliResult.sortedWith(
                            compareBy(
                                Collator.getInstance(systemLocale)
                            ) { it.title?.getText(this@InsertActivity) })
                    )
                    binding.searchProgress.isVisible = false
                    binding.searchNoResults.isVisible =
                        cantoAdapter.adapterItemCount == 0
                    binding.matchedList.isGone = cantoAdapter.adapterItemCount == 0
                }
            } else {
                if (s.isEmpty()) {
                    binding.searchNoResults.isVisible = false
                    binding.matchedList.isVisible = false
                    cantoAdapter.clear()
                    binding.searchProgress.isVisible = false
                    binding.appBarLayout.setExpanded(true, true)
                }
            }
        }
    }

    private fun subscribeObservers() {
        simpleIndexViewModel.insertItemsResult?.observe(this) { canti ->
            simpleIndexViewModel.titoliInsert =
                canti.sortedWith(compareBy(Collator.getInstance(systemLocale)) {
                    it.title?.getText(this)
                })
        }

    }

    companion object {
        private val TAG = InsertActivity::class.java.canonicalName
        internal const val FROM_ADD = "fromAdd"
        internal const val ID_LISTA = "idLista"
        internal const val POSITION = "position"
    }
}
