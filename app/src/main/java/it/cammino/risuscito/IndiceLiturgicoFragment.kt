package it.cammino.risuscito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import it.cammino.risuscito.viewmodels.LiturgicIndexViewModel
import kotlinx.android.synthetic.main.layout_recycler.*
import java.util.*

class IndiceLiturgicoFragment : HFFragment(), SimpleDialogFragment.SimpleCallback {

    private var mCantiViewModel: LiturgicIndexViewModel? = null

    // create boolean for fetching data
    private var isViewShown = true
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private val mAdapter: GenericFastItemAdapter = FastItemAdapter()
    private var llm: LinearLayoutManager? = null
    private var glm: GridLayoutManager? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: Activity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.layout_recycler, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get<LiturgicIndexViewModel>(LiturgicIndexViewModel::class.java)

        mLUtils = LUtils.getInstance(activity!!)

        var sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "LITURGICO_REPLACE")
        sFragment?.setmCallback(this@IndiceLiturgicoFragment)
        sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "LITURGICO_REPLACE_2")
        sFragment?.setmCallback(this@IndiceLiturgicoFragment)

        if (!isViewShown)
            ioThread { if (context != null) listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all }

        return rootView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemExpandableExtension = mAdapter.getExpandableExtension()
        itemExpandableExtension.isOnlyOneExpandedItem = true

        mAdapter.onClickListener = { _: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val bundle = Bundle()
                bundle.putCharSequence("pagina", (item as SimpleSubItem).source!!.text)
                bundle.putInt("idCanto", item.id)
                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle)
                consume = true
            }
            consume
        }

        mAdapter.onLongClickListener = { v: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
            if (item is SimpleSubItem) {
                mCantiViewModel!!.idDaAgg = item.id
                mCantiViewModel!!.popupMenu(this@IndiceLiturgicoFragment, v!!, "LITURGICO_REPLACE", "LITURGICO_REPLACE_2", listePersonalizzate)
            }
            true
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
            val mDao = RisuscitoDatabase.getInstance(context!!).indiceLiturgicoDao()
            val canti = mDao.all
            mCantiViewModel!!.titoliList.clear()
            var subItems = LinkedList<ISubItem<*>>()
            var totCanti = 0
            var totListe = 0

            for (i in canti.indices) {
                val simpleItem = SimpleSubItem()
                        .withTitle(mActivity.resources.getString(LUtils.getResId(canti[i].titolo, R.string::class.java)))
                        .withPage(mActivity.resources.getString(LUtils.getResId(canti[i].pagina, R.string::class.java)))
                        .withSource(mActivity.resources.getString(LUtils.getResId(canti[i].source, R.string::class.java)))
                        .withColor(canti[i].color!!)
                        .withId(canti[i].id)

//                simpleItem
//                        .withContextMenuListener(this@IndiceLiturgicoFragment)
                simpleItem.identifier = (i * 1000).toLong()
                subItems.add(simpleItem)
                totCanti++

                if ((i == (canti.size - 1) || canti[i].idIndice != canti[i + 1].idIndice)) {
                    // serve a non mettere il divisore sull'ultimo elemento della lista
                    simpleItem.withHasDivider(false)
                    val expandableItem = SimpleSubExpandableItem()
                    expandableItem
                            .withTitle(mActivity.resources.getString(LUtils.getResId(canti[i].nome, R.string::class.java)) + " ($totCanti)")
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
                    mCantiViewModel!!.titoliList.add(expandableItem)
                    subItems = LinkedList()
                    totCanti = 0
                } else {
                    simpleItem.withHasDivider(true)
                }
            }
            mAdapter.set(mCantiViewModel!!.titoliList)
            mAdapter.withSavedInstanceState(savedInstanceState)
        }
    }

    /**
     * Set a hint to the system about whether this fragment's UI is currently visible to the user.
     * This hint defaults to true and is persistent across fragment instance state save and restore.
     *
     *
     *
     *
     *
     * An app may set this to false to indicate that the fragment's UI is scrolled out of
     * visibility or is otherwise not directly visible to the user. This may be used by the system to
     * prioritize operations such as fragment lifecycle updates or loader ordering behavior.
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     * false if it is not.
     */
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
        if (userVisibleHint) {
            mOutState = mAdapter.saveInstanceState(mOutState)!!
        }
        super.onSaveInstanceState(mOutState)
    }

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            "LITURGICO_REPLACE" -> {
                listePersonalizzate!![mCantiViewModel!!.idListaClick]
                        .lista!!
                        .addCanto((mCantiViewModel!!.idDaAgg).toString(), mCantiViewModel!!.idPosizioneClick)
                ListeUtils.updateListaPersonalizzata(this@IndiceLiturgicoFragment, listePersonalizzate!![mCantiViewModel!!.idListaClick])
            }
            "LITURGICO_REPLACE_2" ->
                ListeUtils.updatePosizione(this@IndiceLiturgicoFragment, mCantiViewModel!!.idDaAgg, mCantiViewModel!!.idListaDaAgg, mCantiViewModel!!.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {}

    companion object {
        private val TAG = IndiceLiturgicoFragment::class.java.canonicalName
    }
}
