package it.cammino.risuscito

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.adapters.GenericFastItemAdapter
import com.mikepenz.fastadapter.expandable.getExpandableExtension
import com.mikepenz.itemanimators.SlideDownAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleSubExpandableItem
import it.cammino.risuscito.items.SimpleSubItem
import it.cammino.risuscito.ui.HFFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.layout_recycler.*
import java.util.*

class SectionedIndexFragment : HFFragment(), SimpleDialogFragment.SimpleCallback {

    private lateinit var mCantiViewModel: SimpleIndexViewModel

    private var isViewShown = true
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private val mAdapter: GenericFastItemAdapter = FastItemAdapter()
    private var llm: LinearLayoutManager? = null
    private var glm: GridLayoutManager? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: MainActivity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.layout_recycler, container, false)

        val args = Bundle().apply { putInt("tipoLista", arguments!!.getInt("tipoLista", 0)) }
        mCantiViewModel = ViewModelProviders.of(this, ViewModelWithArgumentsFactory(activity!!.application, args)).get(SimpleIndexViewModel::class.java)

        mLUtils = LUtils.getInstance(activity!!)

        var sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, when (mCantiViewModel.tipoLista) {
            0 -> ARGUMENT_REPLACE
            1 -> LITURGICO_REPLACE
            else -> ""
        })
        sFragment?.setmCallback(this)
        sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, when (mCantiViewModel.tipoLista) {
            0 -> ARGUMENT_REPLACE_2
            1 -> LITURGICO_REPLACE_2
            else -> ""
        })
        sFragment?.setmCallback(this)

        if (!isViewShown)
            ioThread { if (context != null) listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all }

        return rootView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemExpandableExtension = mAdapter.getExpandableExtension()
        itemExpandableExtension.isOnlyOneExpandedItem = true

        mAdapter.onClickListener = { _: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(activity, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf("pagina" to (item as SimpleSubItem).source!!.getText(context), "idCanto" to item.id))
                mLUtils!!.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        mAdapter.onLongClickListener = { v: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
            if (item is SimpleSubItem) {
                mCantiViewModel.idDaAgg = item.id
                when (mCantiViewModel.tipoLista) {
                    0 -> mCantiViewModel.popupMenu(this, v!!, ARGUMENT_REPLACE, ARGUMENT_REPLACE_2, listePersonalizzate)
                    1 -> mCantiViewModel.popupMenu(this, v!!, LITURGICO_REPLACE, LITURGICO_REPLACE_2, listePersonalizzate)
                }
            }
            true
        }

        mAdapter.onPreClickListener = { _: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
            if (item is SimpleSubExpandableItem) {
                Log.i(TAG, "item.position ${item.position}")
                if (!item.isExpanded) {
                    if (mActivity.isGridLayout)
                        glm!!.scrollToPositionWithOffset(
                                item.position, 0)
                    else
                        llm!!.scrollToPositionWithOffset(
                                item.position, 0)
                }
            }
            false
        }

        val mMainActivity = mActivity as MainActivity?
        if (mMainActivity!!.isGridLayout) {
            glm = GridLayoutManager(context, if (mMainActivity.hasThreeColumns) 3 else 2)
            glm!!.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (mAdapter.getItemViewType(position)) {
                        R.id.fastadapter_expandable_item_id -> if (mMainActivity.hasThreeColumns) 3 else 2
                        R.id.fastadapter_sub_item_id -> 1
                        else -> -1
                    }
                }
            }
            recycler_view!!.layoutManager = glm
        } else {
            llm = LinearLayoutManager(context)
            recycler_view!!.layoutManager = llm
        }

        recycler_view!!.adapter = mAdapter
        recycler_view!!.setHasFixedSize(true) // Size of RV will not change
        recycler_view!!.itemAnimator = SlideDownAlphaAnimator()

        ioThread {
            when (mCantiViewModel.tipoLista) {
                0 -> {
                    val mDao = RisuscitoDatabase.getInstance(context!!).argomentiDao()
                    val canti = mDao.all
                    mCantiViewModel.titoliList.clear()
                    var subItems = LinkedList<ISubItem<*>>()
                    var totCanti = 0
                    var totListe = 0

                    for (i in canti.indices) {
                        val simpleItem = SimpleSubItem()
                                .withTitle(LUtils.getResId(canti[i].titolo, R.string::class.java))
                                .withPage(LUtils.getResId(canti[i].pagina, R.string::class.java))
                                .withSource(LUtils.getResId(canti[i].source, R.string::class.java))
                                .withColor(canti[i].color!!)
                                .withId(canti[i].id)
                        simpleItem.identifier = (i * 1000).toLong()
                        subItems.add(simpleItem)
                        totCanti++

                        if ((i == (canti.size - 1) || canti[i].idArgomento != canti[i + 1].idArgomento)) {
                            // serve a non mettere il divisore sull'ultimo elemento della lista
                            simpleItem.withHasDivider(false)
                            val expandableItem = SimpleSubExpandableItem()
                            expandableItem
                                    .withTitle(LUtils.getResId(canti[i].nomeArgomento, R.string::class.java))
                                    .witTotItems(totCanti)
                                    .withPosition(totListe++)
                                    .onPreItemClickListener = { _: View?, _: IAdapter<SimpleSubExpandableItem>, item: SimpleSubExpandableItem, _: Int ->
                                if (!item.isExpanded) {
                                    if (mMainActivity.isGridLayout)
                                        glm!!.scrollToPositionWithOffset(
                                                item.position, 0)
                                    else
                                        llm!!.scrollToPositionWithOffset(
                                                item.position, 0)
                                }
                                false
                            }
                            expandableItem.identifier = canti[i].idArgomento.toLong()

                            expandableItem.subItems = subItems
                            expandableItem.subItems.sortBy { (it as SimpleSubItem).title!!.getText(context) }
                            mCantiViewModel.titoliList.add(expandableItem)
                            subItems = LinkedList()
                            totCanti = 0
                        } else {
                            simpleItem.withHasDivider(true)
                        }
                    }
                }
                else -> {
                    val mDao = RisuscitoDatabase.getInstance(context!!).indiceLiturgicoDao()
                    val canti = mDao.all
                    mCantiViewModel.titoliList.clear()
                    var subItems = LinkedList<ISubItem<*>>()
                    var totCanti = 0
                    var totListe = 0

                    for (i in canti.indices) {
                        val simpleItem = SimpleSubItem()
                                .withTitle(LUtils.getResId(canti[i].titolo, R.string::class.java))
                                .withPage(LUtils.getResId(canti[i].pagina, R.string::class.java))
                                .withSource(LUtils.getResId(canti[i].source, R.string::class.java))
                                .withColor(canti[i].color!!)
                                .withId(canti[i].id)
                        simpleItem.identifier = (i * 1000).toLong()
                        subItems.add(simpleItem)
                        totCanti++

                        if ((i == (canti.size - 1) || canti[i].idIndice != canti[i + 1].idIndice)) {
                            // serve a non mettere il divisore sull'ultimo elemento della lista
                            simpleItem.withHasDivider(false)
                            val expandableItem = SimpleSubExpandableItem()
                            expandableItem
                                    .withTitle(LUtils.getResId(canti[i].nome, R.string::class.java))
                                    .witTotItems(totCanti)
                                    .withPosition(totListe++)
                                    .onPreItemClickListener = { _: View?, _: IAdapter<SimpleSubExpandableItem>, item: SimpleSubExpandableItem, _: Int ->
                                if (!item.isExpanded) {
                                    if (mMainActivity.isGridLayout)
                                        glm!!.scrollToPositionWithOffset(
                                                item.position, 0)
                                    else
                                        llm!!.scrollToPositionWithOffset(
                                                item.position, 0)
                                }
                                false
                            }
                            expandableItem.identifier = canti[i].idIndice.toLong()

                            expandableItem.subItems = subItems
                            expandableItem.subItems.sortBy { (it as SimpleSubItem).title!!.getText(context) }
                            mCantiViewModel.titoliList.add(expandableItem)
                            subItems = LinkedList()
                            totCanti = 0
                        } else {
                            simpleItem.withHasDivider(true)
                        }
                    }
                }
            }
            mCantiViewModel.titoliList.sortBy { (it as SimpleSubExpandableItem).title!!.getText(context) }
            mAdapter.set(mCantiViewModel.titoliList)
            mAdapter.withSavedInstanceState(savedInstanceState)
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (view != null) {
                isViewShown = true
                Log.d(javaClass.name, "VISIBLE")
                ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all }
            } else
                isViewShown = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var mOutState = outState
        if (userVisibleHint)
            mOutState = mAdapter.saveInstanceState(mOutState)!!
        super.onSaveInstanceState(mOutState)
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            ARGUMENT_REPLACE, LITURGICO_REPLACE -> {
                listePersonalizzate!![mCantiViewModel.idListaClick]
                        .lista!!
                        .addCanto((mCantiViewModel.idDaAgg).toString(), mCantiViewModel.idPosizioneClick)
                ListeUtils.updateListaPersonalizzata(this, listePersonalizzate!![mCantiViewModel.idListaClick])
            }
            ARGUMENT_REPLACE_2, LITURGICO_REPLACE_2 ->
                ListeUtils.updatePosizione(this, mCantiViewModel.idDaAgg, mCantiViewModel.idListaDaAgg, mCantiViewModel.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {}

    companion object {
        private val TAG = SectionedIndexFragment::class.java.canonicalName
        private const val ARGUMENT_REPLACE = "ARGUMENT_REPLACE"
        private const val ARGUMENT_REPLACE_2 = "ARGUMENT_REPLACE_2"
        private const val LITURGICO_REPLACE = "LITURGICO_REPLACE"
        private const val LITURGICO_REPLACE_2 = "LITURGICO_REPLACE_2"

        fun newInstance(tipoLista: Int): SectionedIndexFragment {
            val f = SectionedIndexFragment()
            f.arguments = bundleOf("tipoLista" to tipoLista)
            return f
        }
    }
}
