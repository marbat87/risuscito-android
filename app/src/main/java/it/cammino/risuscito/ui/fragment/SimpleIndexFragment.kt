package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.content.Intent
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
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.LayoutRecyclerBinding
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.activity.PaginaRenderActivity
import it.cammino.risuscito.utils.*
import it.cammino.risuscito.utils.extension.hasThreeColumns
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.startActivityWithTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Collator

class SimpleIndexFragment : Fragment() {

    private val mCantiViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private val mAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private var listePersonalizzate: List<ListaPers>? = null
    private var mLastClickTime: Long = 0
    private var mActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as? MainActivity
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        mAdapter = FastScrollIndicatorAdapter(mCantiViewModel.tipoLista, requireContext())

        subscribeUiChanges()

        mAdapter.onClickListener =
            { mView: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    // lancia l'activity che visualizza il canto passando il parametro creato
                    val intent = Intent(activity, PaginaRenderActivity::class.java)
                    intent.putExtras(
                        bundleOf(
                            Utility.PAGINA to item.source?.getText(requireContext()),
                            Utility.ID_CANTO to item.id
                        )
                    )
                    activity?.startActivityWithTransition(intent, mView)
                    consume = true
                }
                consume
            }

        mAdapter.onLongClickListener =
            { v: View, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                mCantiViewModel.idDaAgg = item.id
                when (mCantiViewModel.tipoLista) {
                    0 -> mCantiViewModel.popupMenu(
                        this,
                        v,
                        ALPHA_REPLACE + mCantiViewModel.tipoLista,
                        ALPHA_REPLACE_2 + mCantiViewModel.tipoLista,
                        listePersonalizzate
                    )
                    1 -> mCantiViewModel.popupMenu(
                        this,
                        v,
                        NUMERIC_REPLACE + mCantiViewModel.tipoLista,
                        NUMERIC_REPLACE_2 + mCantiViewModel.tipoLista,
                        listePersonalizzate
                    )
                    2 -> mCantiViewModel.popupMenu(
                        this,
                        v,
                        SALMI_REPLACE + mCantiViewModel.tipoLista,
                        SALMI_REPLACE_2 + mCantiViewModel.tipoLista,
                        listePersonalizzate
                    )
                }
                true
            }

        mAdapter.setHasStableIds(true)
        val llm = LinearLayoutManager(context)
        val glm = GridLayoutManager(context, if (context?.hasThreeColumns == true) 3 else 2)
        binding.recyclerView.layoutManager = if (context?.isGridLayout == true) glm else llm
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = mAdapter
//        if (mCantiViewModel.tipoLista != 2) {
//            binding.dragScrollBar.setRecyclerView(binding.cantiList)
//            if (ViewCompat.isAttachedToWindow(binding.dragScrollBar)) {
//                binding.dragScrollBar.setTextColor(
//                    MaterialColors.getColor(
//                        requireContext(),
//                        R.attr.colorOnTertiary,
//                        TAG
//                    )
//                )
//                binding.dragScrollBar.setHandleColor(
//                    MaterialColors.getColor(
//                        context, R.attr.colorTertiary, TAG
//                    )
//                )
//                binding.dragScrollBar.setIndicator(CustomIndicator(context), true)
//                binding.dragScrollBar.setAutoHide(false)
//            } else
//                binding.dragScrollBar.addOnAttachStateChangeListener(object :
//                    View.OnAttachStateChangeListener {
//                    override fun onViewDetachedFromWindow(p0: View?) {
//                        // no-op
//                    }
//
//                    override fun onViewAttachedToWindow(p0: View?) {
//                        (p0 as? TouchScrollBar)?.setTextColor(
//                            MaterialColors.getColor(
//                                requireContext(),
//                                R.attr.colorOnTertiary,
//                                TAG
//                            )
//                        )
//                        (p0 as? TouchScrollBar)?.setHandleColor(
//                            MaterialColors.getColor(
//                                context, R.attr.colorTertiary, TAG
//                            )
//                        )
//                        (p0 as? TouchScrollBar)?.setIndicator(CustomIndicator(context), true)
//                        (p0 as? TouchScrollBar)?.setAutoHide(false)
//                        p0?.removeOnAttachStateChangeListener(this)
//                    }
//                })
//        } else {
//            binding.dragScrollBar.isGone = true
//        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all
        }
    }

    private fun subscribeUiChanges() {
        mCantiViewModel.itemsResult?.observe(viewLifecycleOwner) { canti ->
            mAdapter.set(
                when (mCantiViewModel.tipoLista) {
                    0 -> canti.sortedWith(compareBy(Collator.getInstance(resources.systemLocale)) {
                        it.title?.getText(
                            requireContext()
                        )
                    })
                    1 -> canti.sortedBy { it.page?.getText(requireContext())?.toInt() }
                    2 -> canti
                    else -> canti
                }
            )
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            ALPHA_REPLACE + mCantiViewModel.tipoLista, NUMERIC_REPLACE + mCantiViewModel.tipoLista, SALMI_REPLACE + mCantiViewModel.tipoLista -> {
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
                            ALPHA_REPLACE_2 + mCantiViewModel.tipoLista, NUMERIC_REPLACE_2 + mCantiViewModel.tipoLista, SALMI_REPLACE_2 + mCantiViewModel.tipoLista -> {
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

    companion object {
        private val TAG = SimpleIndexFragment::class.java.canonicalName
        private const val ALPHA_REPLACE = "ALPHA_REPLACE"
        private const val ALPHA_REPLACE_2 = "ALPHA_REPLACE_2"
        private const val NUMERIC_REPLACE = "NUMERIC_REPLACE"
        private const val NUMERIC_REPLACE_2 = "NUMERIC_REPLACE_2"
        private const val SALMI_REPLACE = "SALMI_REPLACE"
        private const val SALMI_REPLACE_2 = "SALMI_REPLACE_2"
        private const val INDICE_LISTA = "indiceLista"

        fun newInstance(tipoLista: Int): SimpleIndexFragment {
            val f = SimpleIndexFragment()
            f.arguments = bundleOf(INDICE_LISTA to tipoLista)
            return f
        }
    }

}