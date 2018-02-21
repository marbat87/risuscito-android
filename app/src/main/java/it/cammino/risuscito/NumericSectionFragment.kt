package it.cammino.risuscito

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.TextView
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.OnClickListener
import it.cammino.risuscito.adapters.FastScrollIndicatorAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.HFFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.viewmodels.NumericIndexViewModel
import kotlinx.android.synthetic.main.index_fragment.*

class NumericSectionFragment : HFFragment(), View.OnCreateContextMenuListener, SimpleDialogFragment.SimpleCallback {

    private lateinit var mAdapter: FastScrollIndicatorAdapter<SimpleItem>

    private var mCantiViewModel: NumericIndexViewModel? = null
    // create boolean for fetching data
    private var isViewShown = true
    private var titoloDaAgg: String? = null
    private var listePersonalizzate: List<ListaPers>? = null
    private var rootView: View? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: Activity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.index_fragment, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get<NumericIndexViewModel>(NumericIndexViewModel::class.java)

        mLUtils = LUtils.getInstance(activity!!)

        var sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "NUMERIC_REPLACE")
        sFragment?.setmCallback(this@NumericSectionFragment)
        sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "NUMERIC_REPLACE_2")
        sFragment?.setmCallback(this@NumericSectionFragment)

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
        val mOnClickListener = OnClickListener<SimpleItem> { _, _, item, _ ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener false
            mLastClickTime = SystemClock.elapsedRealtime()
            val bundle = Bundle()
            bundle.putCharSequence("pagina", item.source!!.text)
            bundle.putInt("idCanto", item.id)

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle)
            true
        }

        cantiList!!.layoutManager = LinearLayoutManager(activity)
        mAdapter = FastScrollIndicatorAdapter(1)
        mAdapter.withOnClickListener(mOnClickListener).setHasStableIds(true)
        FastAdapterDiffUtil.set<FastScrollIndicatorAdapter<SimpleItem>, SimpleItem>(mAdapter, mCantiViewModel!!.titoli)
        cantiList!!.adapter = mAdapter
        val llm = LinearLayoutManager(context)
        cantiList!!.layoutManager = llm
        cantiList!!.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(context!!, llm.orientation)
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
                Thread(
                        Runnable {
                            val mDao = RisuscitoDatabase.getInstance(context!!).listePersDao()
                            listePersonalizzate = mDao.all
                        })
                        .start()
            } else
                isViewShown = false
        }
    }

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
//        super.onCreateContextMenu(menu, v, menuInfo)
        titoloDaAgg = (v.findViewById<View>(R.id.text_title) as TextView).text.toString()
        mCantiViewModel!!.idDaAgg = Integer.valueOf((v.findViewById<View>(R.id.text_id_canto) as TextView).text.toString())!!
        menu.setHeaderTitle("Aggiungi canto a:")

        for (i in listePersonalizzate!!.indices) {
            val subMenu = menu.addSubMenu(
                    ID_FITTIZIO, Menu.NONE, 10 + i, listePersonalizzate!![i].lista!!.name)
            for (k in 0 until listePersonalizzate!![i].lista!!.numPosizioni) {
                subMenu.add(100 + i, k, k, listePersonalizzate!![i].lista!!.getNomePosizione(k))
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
        Log.d(TAG, "onContextItemSelected: " + item!!.itemId)
        if (userVisibleHint) when (item.itemId) {
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
                                                this@NumericSectionFragment,
                                                "NUMERIC_REPLACE")
                                                .title(R.string.dialog_replace_title)
                                                .content(
                                                        (getString(R.string.dialog_present_yet)
                                                                + " "
                                                                + cantoPresente.titolo
                                                                + getString(R.string.dialog_wonna_replace)))
                                                .positiveButton(android.R.string.yes)
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
        } else
            return false
    }

    // aggiunge il canto premuto ai preferiti
    //  public void addToFavorites() {
    //        SQLiteDatabase db = listaCanti.getReadableDatabase();
    //        String sql = "UPDATE ELENCO" + "  SET favourite = 1" + "  WHERE _id = " +
    // mCantiViewModel.idDaAgg;
    //        db.execSQL(sql);
    //        db.close();
    //        Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
    //  }

    // aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
    //  public void addToListaDup(int idLista, int listPosition) {
    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //
    //    String sql = "INSERT INTO CUST_LISTS ";
    //    sql += "VALUES (" + idLista + ", " + listPosition + ", " + mCantiViewModel.idDaAgg + ",
    // CURRENT_TIMESTAMP)";
    //
    //    try {
    //      db.execSQL(sql);
    //      Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
    //    } catch (SQLException e) {
    //      Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
    //    }
    //
    //    db.close();
    //  }

    // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
    //  public void addToListaNoDup(int idLista, int listPosition) {
    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //
    //    // cerca se la posizione nella lista è già occupata
    //    String query =
    //            "SELECT B.titolo"
    //                    + "		FROM CUST_LISTS A"
    //                    + "		   , ELENCO B"
    //                    + "		WHERE A._id = "
    //                    + idLista
    //                    + "         AND A.position = "
    //                    + listPosition
    //                    + "         AND A.id_canto = B._id";
    //    Cursor lista = db.rawQuery(query, null);
    //
    //    int total = lista.getCount();
    //
    //    if (total > 0) {
    //      lista.moveToFirst();
    //      String titoloPresente = lista.getString(0);
    //      lista.close();
    //      db.close();
    //
    //      if (titoloDaAgg.equalsIgnoreCase(titoloPresente)) {
    //        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
    //      } else {
    //        mCantiViewModel.idListaDaAgg = idLista;
    //        mCantiViewModel.posizioneDaAgg = listPosition;
    //        new SimpleDialogFragment.Builder(
    //                (AppCompatActivity) getActivity(), NumericSectionFragment.this,
    // "NUMERIC_REPLACE_2")
    //                .title(R.string.dialog_replace_title)
    //                .content(
    //                        getString(R.string.dialog_present_yet)
    //                                + " "
    //                                + titoloPresente
    //                                + getString(R.string.dialog_wonna_replace))
    //                .positiveButton(android.R.string.yes)
    //                .negativeButton(android.R.string.no)
    //                .show();
    //      }
    //      return;
    //    }
    //
    //    lista.close();
    //
    //    String sql =
    //            "INSERT INTO CUST_LISTS "
    //                    + "VALUES ("
    //                    + idLista
    //                    + ", "
    //                    + listPosition
    //                    + ", "
    //                    + mCantiViewModel.idDaAgg
    //                    + ", CURRENT_TIMESTAMP)";
    //    db.execSQL(sql);
    //    db.close();
    //    Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
    //  }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: " + tag)
        when (tag) {
            "NUMERIC_REPLACE" -> {
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
            "NUMERIC_REPLACE_2" ->
                Thread(
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

    override fun onNeutral(tag: String) {}

    private fun addToListaNoDup(idLista: Int, listPosition: Int) {
        Thread(
                Runnable {
                    val titoloPresente = ListeUtils.addToListaNoDup(
                            context!!,
                            rootView!!,
                            idLista,
                            listPosition,
                            titoloDaAgg!!,
                            mCantiViewModel!!.idDaAgg)
                    if (!titoloPresente.isEmpty()) {
                        mCantiViewModel!!.idListaDaAgg = idLista
                        mCantiViewModel!!.posizioneDaAgg = listPosition
                        SimpleDialogFragment.Builder(
                                (activity as AppCompatActivity?)!!,
                                this@NumericSectionFragment,
                                "NUMERIC_REPLACE_2")
                                .title(R.string.dialog_replace_title)
                                .content(
                                        (getString(R.string.dialog_present_yet)
                                                + " "
                                                + titoloPresente
                                                + getString(R.string.dialog_wonna_replace)))
                                .positiveButton(android.R.string.yes)
                                .negativeButton(android.R.string.no)
                                .show()
                    }
                })
                .start()
    }

    private fun populateDb() {
        mCantiViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mCantiViewModel!!
                .indexResult!!
                .observe(
                        this,
                        Observer<List<Canto>> { canti ->
                            mCantiViewModel!!.titoli.clear()
                            for (canto in canti!!) {
                                val sampleItem = SimpleItem()
                                sampleItem
                                        .withTitle(canto.titolo!!)
                                        .withPage((canto.pagina).toString())
                                        .withSource(canto.source!!)
                                        .withColor(canto.color!!)
                                        .withId(canto.id)
                                        .withContextMenuListener(this@NumericSectionFragment)
                                mCantiViewModel!!.titoli.add(sampleItem)
                            }
                            FastAdapterDiffUtil.set<FastScrollIndicatorAdapter<SimpleItem>, SimpleItem>(mAdapter, mCantiViewModel!!.titoli)
                        })
    }

    companion object {
        private val TAG = NumericSectionFragment::class.java.canonicalName
        private val ID_FITTIZIO = 99999999
    }
}


