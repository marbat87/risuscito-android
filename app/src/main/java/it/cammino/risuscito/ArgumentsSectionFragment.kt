package it.cammino.risuscito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.listeners.OnClickListener
import com.mikepenz.itemanimators.SlideDownAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleSubExpandableItem
import it.cammino.risuscito.items.SimpleSubItem
import it.cammino.risuscito.ui.HFFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.viewmodels.ArgumentIndexViewModel
import kotlinx.android.synthetic.main.layout_recycler.*
import kotlinx.android.synthetic.main.simple_sub_item.view.*
import java.lang.ref.WeakReference
import java.util.*

class ArgumentsSectionFragment : HFFragment(), View.OnCreateContextMenuListener, SimpleDialogFragment.SimpleCallback {

    private var mCantiViewModel: ArgumentIndexViewModel? = null

    // create boolean for fetching data
    private var isViewShown = true
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private var mAdapter: FastItemAdapter<IItem<*, *>> = FastItemAdapter()
    private var mLayoutManager: LinearLayoutManager? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: Activity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.layout_recycler, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get<ArgumentIndexViewModel>(ArgumentIndexViewModel::class.java)

        mLUtils = LUtils.getInstance(activity!!)

        var fragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "ARGUMENT_REPLACE")
        fragment?.setmCallback(this@ArgumentsSectionFragment)
        fragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "ARGUMENT_REPLACE_2")
        fragment?.setmCallback(this@ArgumentsSectionFragment)

        if (!isViewShown) {
            Thread(
                    Runnable {
                        val mDao = RisuscitoDatabase.getInstance(context!!).listePersDao()
                        listePersonalizzate = mDao.all
                    })
                    .start()
        }
        return rootView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemExpandableExtension = ExpandableExtension<IItem<*, *>>()
        itemExpandableExtension.withOnlyOneExpandedItem(true)
        mAdapter.addExtension(itemExpandableExtension)

        val mMainActivity = mActivity as MainActivity?
        if (mMainActivity!!.isGridLayout) {
            mLayoutManager = GridLayoutManager(context, if (mMainActivity.hasThreeColumns) 3 else 2)
            (mLayoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (mAdapter.getItemViewType(position)) {
                        R.id.fastadapter_expandable_item_id -> if (mMainActivity.hasThreeColumns) 3 else 2
                        R.id.fastadapter_sub_item_id -> 1
                        else -> -1
                    }
                }
            }
        } else
            mLayoutManager = LinearLayoutManager(context)
        recycler_view!!.layoutManager = mLayoutManager

        recycler_view!!.adapter = mAdapter
        recycler_view!!.setHasFixedSize(true) // Size of RV will not change
        recycler_view!!.itemAnimator = SlideDownAlphaAnimator()

        val mOnClickListener = OnClickListener<SimpleSubItem<*>> { _, _, item, _ ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener false
            mLastClickTime = SystemClock.elapsedRealtime()
            val bundle = Bundle()
            bundle.putCharSequence("pagina", item.source!!.text)
            bundle.putInt("idCanto", item.id)

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle)
            true
        }

        Thread(
                Runnable {
                    val mDao = RisuscitoDatabase.getInstance(context!!).argomentiDao()
                    val canti = mDao.all
                    mCantiViewModel!!.titoliList.clear()
                    var subItems: LinkedList<SimpleSubItem<*>> = LinkedList()
                    var totCanti = 0

                    for (i in canti.indices) {
                        val simpleItem = SimpleSubItem<SimpleSubItem<*>>()
                                .withTitle(mActivity.resources.getString(LUtils.getResId(canti[i].titolo, R.string::class.java)))
                                .withPage(mActivity.resources.getString(LUtils.getResId(canti[i].pagina, R.string::class.java)))
                                .withSource(mActivity.resources.getString(LUtils.getResId(canti[i].source, R.string::class.java)))
                                .withColor(canti[i].color!!)
                                .withId(canti[i].id)

                        simpleItem
                                .withContextMenuListener(this@ArgumentsSectionFragment)
                                .withOnItemClickListener(mOnClickListener)
                        simpleItem.withIdentifier((i * 1000).toLong())
                        subItems.add(simpleItem)
                        totCanti++

                        if ((i == (canti.size - 1) || canti[i].idArgomento != canti[i + 1].idArgomento)) {
                            // serve a non mettere il divisore sull'ultimo elemento della lista
                            simpleItem.withHasDivider(false)
                            val expandableItem = SimpleSubExpandableItem<SimpleSubExpandableItem<*, *>, SimpleSubItem<*>>()
                            expandableItem
                                    .withTitle(mActivity.resources.getString(LUtils.getResId(canti[i].nomeArgomento, R.string::class.java)) + " ($totCanti)")
                                    .withOnClickListener(OnClickListener { mView, _, mItem, _ ->
                                        if (mItem.isExpanded) {
                                            Log.d(
                                                    TAG,
                                                    "onClick: " + recycler_view!!.getChildAdapterPosition(mView!!))
                                            mLayoutManager!!.scrollToPositionWithOffset(
                                                    recycler_view!!.getChildAdapterPosition(mView), 0)
                                        }
                                        false
                                    })
                                    .withIdentifier(canti[i].idArgomento.toLong())

                            @Suppress("INACCESSIBLE_TYPE")
                            expandableItem.withSubItems(subItems)
                            mCantiViewModel!!.titoliList.add(expandableItem)
                            subItems = LinkedList()
                            totCanti = 0
                        } else {
                            simpleItem.withHasDivider(true)
                        }
                    }

                    FastAdapterDiffUtil.set(mAdapter, mCantiViewModel!!.titoliList)
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    mAdapter.withSavedInstanceState(savedInstanceState)
                })
                .start()
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
                Log.d(TAG, "VISIBLE")
                Thread(
                        Runnable {
                            if (context != null)
                                listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all
                        })
                        .start()
            } else
                isViewShown = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var mOutState = outState
        if (userVisibleHint) {
            mOutState = mAdapter.saveInstanceState(mOutState)
        }
        super.onSaveInstanceState(mOutState)
    }

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        mCantiViewModel!!.idDaAgg = Integer.valueOf(v.text_id_canto.text.toString())
        menu.setHeaderTitle(getString(R.string.select_canto) + ":")

        if (listePersonalizzate != null) {
            for (i in listePersonalizzate!!.indices) {
                val subMenu = menu.addSubMenu(
                        ID_FITTIZIO, Menu.NONE, 10 + i, listePersonalizzate!![i].lista!!.name)
                for (k in 0 until listePersonalizzate!![i].lista!!.numPosizioni) {
                    subMenu.add(100 + i, k, k, listePersonalizzate!![i].lista!!.getNomePosizione(k))
                }
            }
        }

        val inflater = mActivity.menuInflater
        inflater.inflate(R.menu.add_to, menu)

        val pref = PreferenceManager.getDefaultSharedPreferences(mActivity)
        menu.findItem(R.id.add_to_p_pace).isVisible = pref.getBoolean(Utility.SHOW_PACE, false)
        menu.findItem(R.id.add_to_e_seconda).isVisible = pref.getBoolean(Utility.SHOW_SECONDA, false)
        menu.findItem(R.id.add_to_e_offertorio).isVisible = pref.getBoolean(Utility.SHOW_OFFERTORIO, false)
        menu.findItem(R.id.add_to_e_santo).isVisible = pref.getBoolean(Utility.SHOW_SANTO, false)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (userVisibleHint) {
            when (item!!.itemId) {
                R.id.add_to_favorites -> {
                    ListeUtils.addToFavorites(context!!, rootView!!, mCantiViewModel!!.idDaAgg)
                    return true
                }
                R.id.add_to_p_iniziale -> {
                    addToListaNoDup(1, 1)
                    return true
                }
                R.id.add_to_p_prima -> {
                    addToListaNoDup(1, 2)
                    return true
                }
                R.id.add_to_p_seconda -> {
                    addToListaNoDup(1, 3)
                    return true
                }
                R.id.add_to_p_terza -> {
                    addToListaNoDup(1, 4)
                    return true
                }
                R.id.add_to_p_pace -> {
                    addToListaNoDup(1, 6)
                    return true
                }
                R.id.add_to_p_fine -> {
                    addToListaNoDup(1, 5)
                    return true
                }
                R.id.add_to_e_iniziale -> {
                    addToListaNoDup(2, 1)
                    return true
                }
                R.id.add_to_e_seconda -> {
                    addToListaNoDup(2, 6)
                    return true
                }
                R.id.add_to_e_pace -> {
                    addToListaNoDup(2, 2)
                    return true
                }
                R.id.add_to_e_offertorio -> {
                    addToListaNoDup(2, 8)
                    return true
                }
                R.id.add_to_e_santo -> {
                    addToListaNoDup(2, 7)
                    return true
                }
                R.id.add_to_e_pane -> {
                    ListeUtils.addToListaDup(context!!, rootView!!, 2, 3, mCantiViewModel!!.idDaAgg)
                    return true
                }
                R.id.add_to_e_vino -> {
                    ListeUtils.addToListaDup(context!!, rootView!!, 2, 4, mCantiViewModel!!.idDaAgg)
                    return true
                }
                R.id.add_to_e_fine -> {
                    addToListaNoDup(2, 5)
                    return true
                }
                else -> {
                    mCantiViewModel!!.idListaClick = item.groupId
                    mCantiViewModel!!.idPosizioneClick = item.itemId
                    if (mCantiViewModel!!.idListaClick != ID_FITTIZIO && mCantiViewModel!!.idListaClick >= 100) {
                        mCantiViewModel!!.idListaClick -= 100

                        if (listePersonalizzate!![mCantiViewModel!!.idListaClick]
                                        .lista!!
                                        .getCantoPosizione(mCantiViewModel!!.idPosizioneClick) == "") {
                            listePersonalizzate!![mCantiViewModel!!.idListaClick]
                                    .lista!!
                                    .addCanto(
                                            (mCantiViewModel!!.idDaAgg).toString(), mCantiViewModel!!.idPosizioneClick)
                            Thread(
                                    Runnable {
                                        val mDao = RisuscitoDatabase.getInstance(context!!).listePersDao()
                                        mDao.updateLista(listePersonalizzate!![mCantiViewModel!!.idListaClick])
                                        Snackbar.make(rootView!!, R.string.list_added, Snackbar.LENGTH_SHORT)
                                                .show()
                                    })
                                    .start()
                        } else {
                            if (listePersonalizzate!![mCantiViewModel!!.idListaClick]
                                            .lista!!
                                            .getCantoPosizione(mCantiViewModel!!.idPosizioneClick) == (mCantiViewModel!!.idDaAgg).toString()) {
                                Snackbar.make(rootView!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                            } else {
                                Thread(
                                        Runnable {
                                            val mDao = RisuscitoDatabase.getInstance(context!!).cantoDao()
                                            val cantoPresente = mDao.getCantoById(
                                                    Integer.parseInt(
                                                            listePersonalizzate!![mCantiViewModel!!.idListaClick]
                                                                    .lista!!
                                                                    .getCantoPosizione(mCantiViewModel!!.idPosizioneClick)))
                                            SimpleDialogFragment.Builder(
                                                    (activity as AppCompatActivity?)!!,
                                                    this@ArgumentsSectionFragment,
                                                    "ARGUMENT_REPLACE")
                                                    .title(R.string.dialog_replace_title)
                                                    .content(
                                                            (getString(R.string.dialog_present_yet)
                                                                    + " "
                                                                    + resources.getString(LUtils.getResId(cantoPresente.titolo, R.string::class.java))
                                                                    + getString(R.string.dialog_wonna_replace)))
                                                    .positiveButton(R.string.replace_confirm)
                                                    .negativeButton(android.R.string.no)
                                                    .show()
                                        })
                                        .start()
                            }
                        }
                        return true
                    } else
                        return super.onContextItemSelected(item)
                }
            }
        } else
            return false
    }

    override fun onPositive(tag: String) {
        when (tag) {
            "ARGUMENT_REPLACE" -> {
                listePersonalizzate!![mCantiViewModel!!.idListaClick]
                        .lista!!
                        .addCanto((mCantiViewModel!!.idDaAgg).toString(), mCantiViewModel!!.idPosizioneClick)
                Thread(
                        Runnable {
                            val mDao = RisuscitoDatabase.getInstance(context!!).listePersDao()
                            mDao.updateLista(listePersonalizzate!![mCantiViewModel!!.idListaClick])
                            Snackbar.make(rootView!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
                        })
                        .start()
            }
            "ARGUMENT_REPLACE_2" -> Thread(
                    Runnable {
                        val mCustomListDao = RisuscitoDatabase.getInstance(context!!).customListDao()
                        mCustomListDao.updatePositionNoTimestamp(
                                mCantiViewModel!!.idDaAgg,
                                mCantiViewModel!!.idListaDaAgg,
                                mCantiViewModel!!.posizioneDaAgg)
                        Snackbar.make(rootView!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
                    })
                    .start()
        }
    }

    override fun onNegative(tag: String) {}

//    override fun onNeutral(tag: String) {}

    private fun addToListaNoDup(idLista: Int, listPosition: Int) {
        AddToListaNoDupTask(this@ArgumentsSectionFragment, idLista, listPosition).execute()
//        Thread(
//                Runnable {
//                    val titoloPresente = ListeUtils.addToListaNoDup(
//                            context!!,
//                            rootView!!,
//                            idLista,
//                            listPosition,
//                            mCantiViewModel!!.idDaAgg)
//                    if (!titoloPresente.isEmpty()) {
//                        mCantiViewModel!!.idListaDaAgg = idLista
//                        mCantiViewModel!!.posizioneDaAgg = listPosition
//                        SimpleDialogFragment.Builder(
//                                (activity as AppCompatActivity?)!!,
//                                this@ArgumentsSectionFragment,
//                                "ARGUMENT_REPLACE_2")
//                                .title(R.string.dialog_replace_title)
//                                .content(
//                                        (getString(R.string.dialog_present_yet)
//                                                + " "
//                                                + titoloPresente
//                                                + getString(R.string.dialog_wonna_replace)))
//                                .positiveButton(R.string.replace_confirm)
//                                .negativeButton(android.R.string.no)
//                                .show()
//                    }
//                })
//                .start()
    }

    private class AddToListaNoDupTask internal constructor(fragment: ArgumentsSectionFragment, private val idLista: Int, private val listPosition: Int) : AsyncTask<Any, Void, String>() {

        private val fragmentReference: WeakReference<ArgumentsSectionFragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): String? {

            return ListeUtils.addToListaNoDup(
                    fragmentReference.get()!!.context!!,
                    fragmentReference.get()!!.rootView!!,
                    idLista,
                    listPosition,
                    fragmentReference.get()!!.mCantiViewModel!!.idDaAgg)
        }

        override fun onPostExecute(titoloPresente: String?) {
            super.onPostExecute(titoloPresente)
            if (titoloPresente != null && titoloPresente.isNotEmpty()) {
                fragmentReference.get()!!.mCantiViewModel!!.idListaDaAgg = idLista
                fragmentReference.get()!!.mCantiViewModel!!.posizioneDaAgg = listPosition
                SimpleDialogFragment.Builder(
                        (fragmentReference.get()!!.activity as AppCompatActivity?)!!,
                        fragmentReference.get()!!,
                        "ARGUMENT_REPLACE_2")
                        .title(R.string.dialog_replace_title)
                        .content(
                                (fragmentReference.get()!!.getString(R.string.dialog_present_yet)
                                        + " "
                                        + titoloPresente
                                        + fragmentReference.get()!!.getString(R.string.dialog_wonna_replace)))
                        .positiveButton(R.string.replace_confirm)
                        .negativeButton(android.R.string.no)
                        .show()
            } else
                Snackbar.make(fragmentReference.get()!!.rootView!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val TAG = ArgumentsSectionFragment::class.java.canonicalName
        private const val ID_FITTIZIO = 99999999
    }
}
