package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.GenericAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.adapters.GenericFastItemAdapter
import com.mikepenz.fastadapter.expandable.getExpandableExtension
import com.mikepenz.itemanimators.SlideDownAlphaAnimator
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.LayoutRecyclerBinding
import it.cammino.risuscito.items.SimpleSubExpandableItem
import it.cammino.risuscito.items.SimpleSubItem
import it.cammino.risuscito.items.simpleSubExpandableItem
import it.cammino.risuscito.items.simpleSubItem
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.OLD_PAGE_SUFFIX
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.utils.extension.useOldIndex
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.*

class SectionedIndexFragment : Fragment() {

    private val mCantiViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var listePersonalizzate: List<ListaPers>? = null
    private val mAdapter: GenericFastItemAdapter = FastItemAdapter()
    private var llm: LinearLayoutManager? = null
    private var glm: GridLayoutManager? = null
    private var mLastClickTime: Long = 0
    private var mActivity: MainActivity? = null

    private var _binding: LayoutRecyclerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as? MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemExpandableExtension = mAdapter.getExpandableExtension()
        itemExpandableExtension.isOnlyOneExpandedItem = true

        mAdapter.onClickListener =
            { mView: View?, _: GenericAdapter, item: GenericItem, _: Int ->
                var consume = false
                if (item is SimpleSubItem) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        mActivity?.openCanto(
                            TAG,
                            mView,
                            item.id,
                            item.source?.getText(
                                requireContext()
                            ),
                            false
                        )
                        consume = true
                    }
                }
                consume
            }

        mAdapter.onLongClickListener =
            { v: View, _: GenericAdapter, item: GenericItem, _: Int ->
                if (item is SimpleSubItem) {
                    mCantiViewModel.idDaAgg = item.id
                    when (mCantiViewModel.tipoLista) {
                        0 -> mCantiViewModel.popupMenu(
                            this,
                            v,
                            LITURGICO_REPLACE + mCantiViewModel.tipoLista,
                            LITURGICO_REPLACE_2 + mCantiViewModel.tipoLista,
                            listePersonalizzate
                        )
                    }
                }
                true
            }

        mAdapter.onPreClickListener =
            { _: View?, _: GenericAdapter, item: GenericItem, _: Int ->
                if (item is SimpleSubExpandableItem) {
                    Log.i(TAG, "item.position ${item.position}")
                    if (!item.isExpanded) {
                        if (context?.isGridLayout == true)
                            glm?.scrollToPositionWithOffset(
                                item.position, 0
                            )
                        else
                            llm?.scrollToPositionWithOffset(
                                item.position, 0
                            )
                    }
                }
                false
            }

        if (context?.isGridLayout == true) {
            glm = GridLayoutManager(context, 2)
            glm?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (mAdapter.getItemViewType(position)) {
                        R.id.fastadapter_expandable_item_id -> 2
                        R.id.fastadapter_sub_item_id -> 1
                        else -> -1
                    }
                }
            }
            binding.recyclerView.layoutManager = glm
        } else {
            llm = LinearLayoutManager(context)
            binding.recyclerView.layoutManager = llm
        }

        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.setHasFixedSize(true) // Size of RV will not change
        binding.recyclerView.itemAnimator = SlideDownAlphaAnimator()

        lifecycleScope.launch { updateLists(savedInstanceState) }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            LITURGICO_REPLACE + mCantiViewModel.tipoLista -> {
                                simpleDialogViewModel.handled = true
                                listePersonalizzate?.let { lista ->
                                    lista[mCantiViewModel.idListaClick]
                                        .lista?.addCanto(
                                            (mCantiViewModel.idDaAgg).toString(),
                                            mCantiViewModel.idPosizioneClick
                                        )
                                    ListeUtils.updateListaPersonalizzata(
                                        this,
                                        lista[mCantiViewModel.idListaClick]
                                    )
                                }
                            }
                            LITURGICO_REPLACE_2 + mCantiViewModel.tipoLista -> {
                                simpleDialogViewModel.handled = true
                                ListeUtils.updatePosizione(
                                    this,
                                    mCantiViewModel.idDaAgg,
                                    mCantiViewModel.idListaDaAgg,
                                    mCantiViewModel.posizioneDaAgg
                                )
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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val mOutState = mAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(mOutState)
    }

    private suspend fun updateLists(savedInstanceState: Bundle?) {
        if (mCantiViewModel.tipoLista == 0) {
            val useOldIndex = requireContext().useOldIndex()
            val mDao = RisuscitoDatabase.getInstance(requireContext()).indiceLiturgicoDao()
            val canti = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.all }
            mCantiViewModel.titoliList.clear()
            var mSubItems = LinkedList<ISubItem<*>>()
            var totCanti = 0

            for (i in canti.indices) {
                mSubItems.add(
                    simpleSubItem {
                        setTitle = Utility.getResId(canti[i].titolo, R.string::class.java)
                        setPage = Utility.getResId(
                            if (useOldIndex) canti[i].pagina + OLD_PAGE_SUFFIX else canti[i].pagina,
                            R.string::class.java
                        )
                        setPage = Utility.getResId(
                            if (useOldIndex) canti[i].pagina + OLD_PAGE_SUFFIX else canti[i].pagina,
                            R.string::class.java
                        )
                        setSource = Utility.getResId(canti[i].source, R.string::class.java)
                        setColor = canti[i].color
                        id = canti[i].id
                        identifier = ((canti[i].idIndice * 10000) + canti[i].id).toLong()
                    }
                )
                totCanti++

                if ((i == (canti.size - 1) || canti[i].idIndice != canti[i + 1].idIndice)) {
                    // serve a non mettere il divisore sull'ultimo elemento della lista
                    mCantiViewModel.titoliList.add(
                        simpleSubExpandableItem {
                            setTitle = Utility.getResId(canti[i].nome, R.string::class.java)
                            totItems = totCanti
                            id = canti[i].idIndice
                            subItems = mSubItems
                            subItems.sortWith(compareBy(Collator.getInstance(resources.systemLocale)) {
                                (it as? SimpleSubItem)?.title?.getText(
                                    requireContext()
                                )
                            })
                        }
                    )
                    mSubItems = LinkedList()
                    totCanti = 0
                }
            }
        }

        var totListe = 0
//        mCantiViewModel.titoliList.sortWith(compareBy(Collator.getInstance(getSystemLocale(resources))) { (it as? SimpleSubExpandableItem)?.title?.getText(requireContext()) })
        mCantiViewModel.titoliList.forEach {
            (it as? SimpleSubExpandableItem)?.position = totListe++
        }
        mAdapter.set(mCantiViewModel.titoliList)
        mAdapter.withSavedInstanceState(savedInstanceState)
    }

    companion object {
        private val TAG = SectionedIndexFragment::class.java.canonicalName
        private const val LITURGICO_REPLACE = "LITURGICO_REPLACE"
        private const val LITURGICO_REPLACE_2 = "LITURGICO_REPLACE_2"
        private const val INDICE_LISTA = "indiceLista"

        fun newInstance(tipoLista: Int): SectionedIndexFragment {
            val f = SectionedIndexFragment()
            f.arguments = bundleOf(INDICE_LISTA to tipoLista)
            return f
        }
    }
}
