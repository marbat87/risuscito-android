package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.SearchLayoutBinding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.Collator

class SearchFragment : Fragment() {

    private val mViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply { putInt(Utility.TIPO_LISTA, 0) })
    }
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val activityViewModel: MainActivityViewModel by viewModels({ requireActivity() })

    private var job: Job = Job()

    private val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()

    private var listePersonalizzate: List<ListaPers>? = null

    private var mLastClickTime: Long = 0
    private var mMainActivity: MainActivity? = null
    private lateinit var mPopupMenu: PopupMenu

    private var _binding: SearchLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
            mViewModel.aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        lifecycleScope.launch(Dispatchers.IO) { listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all }

        subscribeUiCanti()

        binding.textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(requireActivity().applicationContext, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(Utility.PAGINA to item.source?.getText(requireContext()), Utility.ID_CANTO to item.id))
                activityViewModel.mLUtils.startActivityWithTransition(intent)
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

        binding.matchedList.adapter = cantoAdapter
        val llm = if (activityViewModel.isGridLayout)
            GridLayoutManager(context, if (activityViewModel.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        binding.matchedList.layoutManager = llm
        val insetDivider = DividerItemDecoration(requireContext(), llm.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider.setDrawable(it) }
        binding.matchedList.addItemDecoration(insetDivider)

        binding.textFieldRicerca.setOnKeyListener { _, keyCode, _ ->
            var returnValue = false
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)?.hideSoftInputFromWindow(binding.textFieldRicerca.windowToken, 0)
                returnValue = true
            }
            returnValue
        }

        binding.textFieldRicerca.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
            job.cancel()
            ricercaStringa(s.toString())
        }

        val wrapper = ContextThemeWrapper(requireContext(), R.style.Widget_MaterialComponents_PopupMenu_Risuscito)
        mPopupMenu = if (LUtils.hasK()) PopupMenu(wrapper, binding.moreOptions, Gravity.END) else PopupMenu(wrapper, binding.moreOptions)
        mPopupMenu.inflate(R.menu.search_option_menu)
        mPopupMenu.menu.findItem(R.id.consegnaty_only).isVisible = false
        mPopupMenu.setOnMenuItemClickListener {
            it.isChecked = !it.isChecked
            mViewModel.advancedSearch = it.isChecked
            binding.textBoxRicerca.hint = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)
            job.cancel()
            ricercaStringa(binding.textFieldRicerca.text.toString())
            true
        }

        binding.moreOptions.setOnClickListener {
            mPopupMenu.menu.findItem(R.id.advanced_search).isChecked = mViewModel.advancedSearch
            mPopupMenu.show()
        }

    }

    private fun ricercaStringa(s: String) {
        job = lifecycleScope.launch {
            // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
            if (s.trim { it <= ' ' }.length >= 3) {
                binding.searchNoResults.isVisible = false
                binding.searchProgress.isVisible = true
                val titoliResult = ArrayList<SimpleItem>()

                Log.d(TAG, "performSearch STRINGA: $s")
                Log.d(TAG, "performSearch ADVANCED: ${mViewModel.advancedSearch}")
                if (mViewModel.advancedSearch) {
                    val words = s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (aText in mViewModel.aTexts) {
                        if (!isActive) return@launch

                        if (aText[0] == null || aText[0].equals("", ignoreCase = true)) break

                        var found = true
                        for (word in words) {
                            if (!isActive) return@launch

                            if (word.trim { it <= ' ' }.length > 1) {
                                var text = word.trim { it <= ' ' }
                                text = text.toLowerCase(getSystemLocale(resources))
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(tag, "aText[0]: ${aText[0]}")
                            mViewModel.titoli
                                    .filter { (aText[0] ?: "") == it.undecodedSource }
                                    .forEach {
                                        if (!isActive) return@launch
                                        titoliResult.add(it.apply { filter = "" })
                                    }
                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).toLowerCase(getSystemLocale(resources))
                    Log.d(tag, "performSearch onTextChanged: stringa $stringa")
                    mViewModel.titoli
                            .filter {
                                Utility.removeAccents(it.title?.getText(requireContext())
                                        ?: "").toLowerCase(getSystemLocale(resources)).contains(stringa)
                            }
                            .forEach {
                                if (!isActive) return@launch
                                titoliResult.add(it.apply { filter = stringa })
                            }
                }
                if (isActive) {
                    cantoAdapter.set(titoliResult.sortedWith(compareBy(Collator.getInstance(getSystemLocale(resources))) { it.title?.getText(requireContext()) }))
                    binding.searchProgress.isVisible = false
                    binding.searchNoResults.isVisible = cantoAdapter.adapterItemCount == 0
                    binding.matchedList.isGone = cantoAdapter.adapterItemCount == 0
                }
            } else {
                if (s.isEmpty()) {
                    binding.searchNoResults.isVisible = false
                    binding.matchedList.isVisible = false
                    cantoAdapter.clear()
                    binding.searchProgress.isVisible = false
                }
            }
        }
    }

    private fun subscribeUiCanti() {
        mViewModel.itemsResult?.observe(viewLifecycleOwner) { canti ->
            mViewModel.titoli = canti.sortedWith(compareBy(Collator.getInstance(getSystemLocale(resources))) { it.title?.getText(requireContext()) })
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            SEARCH_REPLACE -> {
                                simpleDialogViewModel.handled = true
                                listePersonalizzate?.let { lista ->
                                    lista[mViewModel.idListaClick]
                                            .lista?.addCanto(mViewModel.idDaAgg.toString(), mViewModel.idPosizioneClick)
                                    ListeUtils.updateListaPersonalizzata(this, lista[mViewModel.idListaClick])
                                }
                            }
                            SEARCH_REPLACE_2 -> {
                                simpleDialogViewModel.handled = true
                                ListeUtils.updatePosizione(this, mViewModel.idDaAgg, mViewModel.idListaDaAgg, mViewModel.posizioneDaAgg)
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

    companion object {
        private val TAG = SearchFragment::class.java.canonicalName
        private const val SEARCH_REPLACE = "SEARCH_REPLACE"
        private const val SEARCH_REPLACE_2 = "SEARCH_REPLACE_2"
    }
}
