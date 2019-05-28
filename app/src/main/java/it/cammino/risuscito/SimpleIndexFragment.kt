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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.IAdapter
import com.turingtechnologies.materialscrollbar.CustomIndicator
import it.cammino.risuscito.adapters.FastScrollIndicatorAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.HFFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.index_list_fragment.*

class SimpleIndexFragment : HFFragment(), SimpleDialogFragment.SimpleCallback {

    private lateinit var mAdapter: FastScrollIndicatorAdapter
    private lateinit var mCantiViewModel: SimpleIndexViewModel
    private var isViewShown = true
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: MainActivity

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribeUiFavorites()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.index_list_fragment, container, false)

        val args = Bundle().apply { putInt("tipoLista", arguments?.getInt("tipoLista", 0) ?: 0) }
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

        if (!isViewShown)
            ioThread { if (context != null) listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all }

        return rootView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity as MainActivity
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
                        "pagina" to item.source?.getText(context),
                        "idCanto" to item.id
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

        val mMainActivity = activity as MainActivity?

        mAdapter.setHasStableIds(true)
        mAdapter.set(mCantiViewModel.titoli)
        val llm = LinearLayoutManager(context)
        val glm = GridLayoutManager(context, if (mMainActivity?.hasThreeColumns == true) 3 else 2)
        cantiList?.layoutManager = if (mMainActivity?.isGridLayout == true) glm else llm
        cantiList?.setHasFixedSize(true)
        cantiList?.adapter = mAdapter
        val insetDivider = DividerItemDecoration(requireContext(), (if (mMainActivity?.isGridLayout == true) glm else llm).orientation)
        insetDivider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)!!)
        cantiList?.addItemDecoration(insetDivider)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (view != null) {
                isViewShown = true
                Log.d(TAG, "VISIBLE")
                ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all }
            } else
                isViewShown = false
        }
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

    private fun subscribeUiFavorites() {
        mCantiViewModel.itemsResult?.observe(
                this,
                Observer<List<SimpleItem>> { canti ->
                    Log.d(TAG, "variazione canti")
                    when (mCantiViewModel.tipoLista) {
                        0 -> mCantiViewModel.titoli = canti.sortedBy { it.title?.getText(context) }
                        1 -> mCantiViewModel.titoli = canti.sortedBy { it.page?.getText(context)?.toInt() }
                        2 -> mCantiViewModel.titoli = canti
                        else -> mCantiViewModel.titoli = canti
                    }
                    mAdapter.set(mCantiViewModel.titoli)
                    dragScrollBar.setIndicator(CustomIndicator(context), true)
                    dragScrollBar.setAutoHide(false)
                })
    }

    companion object {
        private const val ALPHA_REPLACE = "ALPHA_REPLACE"
        private const val ALPHA_REPLACE_2 = "ALPHA_REPLACE_2"
        private const val NUMERIC_REPLACE = "NUMERIC_REPLACE"
        private const val NUMERIC_REPLACE_2 = "NUMERIC_REPLACE_2"
        private const val SALMI_REPLACE = "SALMI_REPLACE"
        private const val SALMI_REPLACE_2 = "SALMI_REPLACE_2"
        private val TAG = SimpleIndexFragment::class.java.canonicalName

        fun newInstance(tipoLista: Int): SimpleIndexFragment {
            val f = SimpleIndexFragment()
            f.arguments = bundleOf("tipoLista" to tipoLista)
            return f
        }
    }

}
