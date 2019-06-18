package it.cammino.risuscito

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.IAdapter
import com.turingtechnologies.materialscrollbar.CustomIndicator
import com.turingtechnologies.materialscrollbar.TouchScrollBar
import it.cammino.risuscito.adapters.FastScrollIndicatorAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.index_list_fragment.*

class SimpleIndexFragment : Fragment(), SimpleDialogFragment.SimpleCallback {

    private lateinit var mAdapter: FastScrollIndicatorAdapter
    private lateinit var mCantiViewModel: SimpleIndexViewModel
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private var mActivity: MainActivity? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribeUiChanges()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.index_list_fragment, container, false)

        val args = Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        }
        mCantiViewModel = ViewModelProviders.of(this, ViewModelWithArgumentsFactory(requireActivity().application, args)).get(SimpleIndexViewModel::class.java)

        mLUtils = LUtils.getInstance(requireActivity())
        mAdapter = FastScrollIndicatorAdapter(mCantiViewModel.tipoLista, requireContext())

        var fragment = SimpleDialogFragment.findVisible(mActivity, when (mCantiViewModel.tipoLista) {
            0 -> ALPHA_REPLACE
            1 -> NUMERIC_REPLACE
            2 -> SALMI_REPLACE
            else -> ""
        })
        fragment?.setmCallback(this)
        fragment = SimpleDialogFragment.findVisible(mActivity, when (mCantiViewModel.tipoLista) {
            0 -> ALPHA_REPLACE_2
            1 -> NUMERIC_REPLACE_2
            2 -> SALMI_REPLACE_2
            else -> ""
        })
        fragment?.setmCallback(this)

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as? MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                // lancia l'activity che visualizza il canto passando il parametro creato
                val intent = Intent(activity, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf(
                        Utility.PAGINA to item.source?.getText(context),
                        Utility.ID_CANTO to item.id
                ))
                mLUtils?.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        mAdapter.onLongClickListener = { v: View, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            mCantiViewModel.idDaAgg = item.id
            when (mCantiViewModel.tipoLista) {
                0 -> mCantiViewModel.popupMenu(this, v, ALPHA_REPLACE, ALPHA_REPLACE_2, listePersonalizzate)
                1 -> mCantiViewModel.popupMenu(this, v, NUMERIC_REPLACE, NUMERIC_REPLACE_2, listePersonalizzate)
                2 -> mCantiViewModel.popupMenu(this, v, SALMI_REPLACE, SALMI_REPLACE_2, listePersonalizzate)
            }
            true
        }

        mAdapter.setHasStableIds(true)
        val llm = LinearLayoutManager(context)
        val glm = GridLayoutManager(context, if (mActivity?.hasThreeColumns == true) 3 else 2)
        cantiList?.layoutManager = if (mActivity?.isGridLayout == true) glm else llm
        cantiList?.setHasFixedSize(true)
        cantiList?.adapter = mAdapter
        val insetDivider = DividerItemDecoration(requireContext(), (if (mActivity?.isGridLayout == true) glm else llm).orientation)
        insetDivider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)!!)
        cantiList?.addItemDecoration(insetDivider)
        dragScrollBar?.let {
            if (ViewCompat.isAttachedToWindow(it)) {
                it.setIndicator(CustomIndicator(context), true)
                it.setAutoHide(false)
            } else
                it.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewDetachedFromWindow(p0: View?) {}

                    override fun onViewAttachedToWindow(p0: View?) {
                        (p0 as? TouchScrollBar)?.setIndicator(CustomIndicator(context), true)
                        (p0 as? TouchScrollBar)?.setAutoHide(false)
                        p0?.removeOnAttachStateChangeListener(this)
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all }
    }

    override fun onPositive(tag: String) {
        Log.d(javaClass.name, "onPositive: $tag")
        when (tag) {
            ALPHA_REPLACE, NUMERIC_REPLACE, SALMI_REPLACE -> {
                listePersonalizzate?.let {
                    it[mCantiViewModel.idListaClick]
                            .lista?.addCanto((mCantiViewModel.idDaAgg).toString(), mCantiViewModel.idPosizioneClick)
                    ListeUtils.updateListaPersonalizzata(this, it[mCantiViewModel.idListaClick])
                }
            }
            ALPHA_REPLACE_2, NUMERIC_REPLACE_2, SALMI_REPLACE_2 ->
                ListeUtils.updatePosizione(this, mCantiViewModel.idDaAgg, mCantiViewModel.idListaDaAgg, mCantiViewModel.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {}

    private fun subscribeUiChanges() {
        mCantiViewModel.itemsResult?.observe(
                this,
                Observer<List<SimpleItem>> { canti ->
                    mAdapter.set(
                            when (mCantiViewModel.tipoLista) {
                                0 -> canti.sortedBy { it.title?.getText(context) }
                                1 -> canti.sortedBy { it.page?.getText(context)?.toInt() }
                                2 -> canti
                                else -> canti
                            }
                    )
                })
    }

    companion object {
        private const val ALPHA_REPLACE = "ALPHA_REPLACE"
        private const val ALPHA_REPLACE_2 = "ALPHA_REPLACE_2"
        private const val NUMERIC_REPLACE = "NUMERIC_REPLACE"
        private const val NUMERIC_REPLACE_2 = "NUMERIC_REPLACE_2"
        private const val SALMI_REPLACE = "SALMI_REPLACE"
        private const val SALMI_REPLACE_2 = "SALMI_REPLACE_2"
        private const val INDICE_LISTA = "indiceLista"
//        private val TAG = SimpleIndexFragment::class.java.canonicalName

        fun newInstance(tipoLista: Int): SimpleIndexFragment {
            val f = SimpleIndexFragment()
            f.arguments = bundleOf(INDICE_LISTA to tipoLista)
            return f
        }
    }

}