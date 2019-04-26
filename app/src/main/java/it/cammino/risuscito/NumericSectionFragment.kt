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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.IAdapter
import com.turingtechnologies.materialscrollbar.CustomIndicator
import it.cammino.risuscito.adapters.FastScrollIndicatorAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.HFFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.NumericIndexViewModel
import kotlinx.android.synthetic.main.index_list_fragment.*

class NumericSectionFragment : HFFragment(), SimpleDialogFragment.SimpleCallback {

    private var mAdapter: FastScrollIndicatorAdapter = FastScrollIndicatorAdapter(1)

    private var mCantiViewModel: NumericIndexViewModel? = null
    // create boolean for fetching data
    private var isViewShown = true
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: Activity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.index_list_fragment, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get<NumericIndexViewModel>(NumericIndexViewModel::class.java)

        mLUtils = LUtils.getInstance(activity!!)

        var sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "NUMERIC_REPLACE")
        sFragment?.setmCallback(this@NumericSectionFragment)
        sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "NUMERIC_REPLACE_2")
        sFragment?.setmCallback(this@NumericSectionFragment)

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

        mAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val bundle = Bundle()
                bundle.putCharSequence("pagina", item.source!!.text)
                bundle.putInt("idCanto", item.id)
                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle)
                consume = true
            }
            consume
        }

        mAdapter.onLongClickListener = { v: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            mCantiViewModel!!.idDaAgg = item.id
            mCantiViewModel!!.popupMenu(this@NumericSectionFragment, v!!, "NUMERIC_REPLACE", "NUMERIC_REPLACE_2", listePersonalizzate)
            true
        }

        val mMainActivity = activity as MainActivity?

        mAdapter.setHasStableIds(true)
        mAdapter.set(mCantiViewModel!!.titoli)
        val llm = LinearLayoutManager(context)
        val glm = GridLayoutManager(context, if (mMainActivity!!.hasThreeColumns) 3 else 2)
        cantiList!!.layoutManager = if (mMainActivity.isGridLayout) glm else llm
        cantiList!!.setHasFixedSize(true)
        cantiList!!.adapter = mAdapter
        val insetDivider = DividerItemDecoration(context!!, (if (mMainActivity.isGridLayout) glm else llm).orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(context!!, R.drawable.material_inset_divider)!!)
        cantiList!!.addItemDecoration(insetDivider)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        populateDb()
        subscribeUiFavorites()
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

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            "NUMERIC_REPLACE" -> {
                listePersonalizzate!![mCantiViewModel!!.idListaClick]
                        .lista!!
                        .addCanto((mCantiViewModel!!.idDaAgg).toString(), mCantiViewModel!!.idPosizioneClick)
                ListeUtils.updateListaPersonalizzata(this@NumericSectionFragment, listePersonalizzate!![mCantiViewModel!!.idListaClick])
            }
            "NUMERIC_REPLACE_2" ->
                ListeUtils.updatePosizione(this@NumericSectionFragment, mCantiViewModel!!.idDaAgg, mCantiViewModel!!.idListaDaAgg, mCantiViewModel!!.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {}

    private fun populateDb() {
        mCantiViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mCantiViewModel!!
                .indexResult!!
                .observe(
                        this,
                        Observer<List<Canto>> { canti ->
                            if (canti != null) {
                                val newList = ArrayList<SimpleItem>()
                                canti.sortedBy { resources.getString(LUtils.getResId(it.pagina, R.string::class.java)).toInt() }
                                        .forEach {
                                            newList.add(
                                                    SimpleItem()
                                                            .withTitle(resources.getString(LUtils.getResId(it.titolo, R.string::class.java)))
                                                            .withPage(resources.getString(LUtils.getResId(it.pagina, R.string::class.java)))
                                                            .withSource(resources.getString(LUtils.getResId(it.source, R.string::class.java)))
                                                            .withColor(it.color!!)
                                                            .withId(it.id)
                                            )
                                        }
                                mCantiViewModel!!.titoli = newList
                                mAdapter.set(mCantiViewModel!!.titoli)
                                dragScrollBar.setIndicator(CustomIndicator(context), true)
                                dragScrollBar.setAutoHide(false)
                            }
                        })
    }

    companion object {
        private val TAG = NumericSectionFragment::class.java.canonicalName
    }

}


