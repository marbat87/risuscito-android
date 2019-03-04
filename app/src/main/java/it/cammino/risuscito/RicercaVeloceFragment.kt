package it.cammino.risuscito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.OnClickListener
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.GenericIndexViewModel
import kotlinx.android.synthetic.main.activity_general_search.*
import kotlinx.android.synthetic.main.ricerca_tab_layout.*
import kotlinx.android.synthetic.main.simple_row_item.view.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import java.lang.ref.WeakReference

class RicercaVeloceFragment : Fragment(), View.OnCreateContextMenuListener, SimpleDialogFragment.SimpleCallback {

    internal lateinit var cantoAdapter: FastItemAdapter<SimpleItem>

    // create boolean for fetching data
    private var isViewShown = true
    private var rootView: View? = null
    private var titoli: MutableList<SimpleItem> = ArrayList()
    private var listePersonalizzate: List<ListaPers>? = null
    private var searchTask: SearchTask? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: Activity

    private var mViewModel: GenericIndexViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.ricerca_tab_layout, container, false)

        mViewModel = ViewModelProviders.of(this).get<GenericIndexViewModel>(GenericIndexViewModel::class.java)

        activity!!.tempTextField
                .addTextChangedListener(
                        object : TextWatcher {

                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                val tempText = textfieldRicerca.text.toString()
                                if (tempText != s.toString()) textfieldRicerca.setText(s)
                            }

                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                            override fun afterTextChanged(s: Editable) {}
                        })

        mLUtils = LUtils.getInstance(activity!!)

        var sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "VELOCE_REPLACE")
        sFragment?.setmCallback(this@RicercaVeloceFragment)
        sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "VELOCE_REPLACE_2")
        sFragment?.setmCallback(this@RicercaVeloceFragment)

        if (!isViewShown)
            ioThread { if (context != null) listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        consegnati_only_view.visibility = View.GONE
        ricerca_subtitle.text = getString(R.string.fast_search_subtitle)

        val mOnClickListener = OnClickListener<SimpleItem> { _, _, item, _ ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener true
            mLastClickTime = SystemClock.elapsedRealtime()
            val bundle = Bundle()
            bundle.putCharSequence("pagina", item.source!!.text)
            bundle.putInt("idCanto", item.id)

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle)
            true
        }

        cantoAdapter = FastItemAdapter()
        cantoAdapter.setHasStableIds(true)
        cantoAdapter.withOnClickListener(mOnClickListener)

        matchedList.adapter = cantoAdapter
        val mMainActivity = activity as MainActivity?
        val llm = if (mMainActivity!!.isGridLayout)
            GridLayoutManager(context, if (mMainActivity.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        matchedList.layoutManager = llm
        matchedList.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(context!!, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(context!!, R.drawable.material_inset_divider)!!)
        matchedList.addItemDecoration(insetDivider)

        pulisci_ripple.setOnClickListener {
            textfieldRicerca.setText("")
            search_no_results.visibility = View.GONE
        }

        textfieldRicerca.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                (ContextCompat.getSystemService(context as Context, InputMethodManager::class.java) as InputMethodManager)
                        .hideSoftInputFromWindow(textfieldRicerca.windowToken, 0)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        textfieldRicerca.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        ricercaStringa(s.toString())
                    }
                }
        )
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity as Activity
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
                ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all }

                // to hide soft keyboard
                (ContextCompat.getSystemService(context as Context, InputMethodManager::class.java) as InputMethodManager)
                        .hideSoftInputFromWindow(textfieldRicerca?.windowToken, 0)
            } else
                isViewShown = false
        }
    }

    override fun onDestroy() {
        if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
            searchTask!!.cancel(true)
        super.onDestroy()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        mViewModel!!.idDaAgg = Integer.valueOf(v.text_id_canto.text.toString())
        menu.setHeaderTitle(getString(R.string.select_canto) + ":")

//        if (listePersonalizzate != null) {
        listePersonalizzate?.let {
            for (i in it.indices) {
                val subMenu = menu.addSubMenu(
                        ID_FITTIZIO, Menu.NONE, 10 + i, it[i].lista!!.name)
                for (k in 0 until it[i].lista!!.numPosizioni) {
                    subMenu.add(100 + i, k, k, it[i].lista!!.getNomePosizione(k))
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
                    ListeUtils.addToFavorites(this@RicercaVeloceFragment, mViewModel!!.idDaAgg)
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
                    ListeUtils.addToListaDup(this@RicercaVeloceFragment, 2, 3, mViewModel!!.idDaAgg)
                    return true
                }
                R.id.add_to_e_vino -> {
                    ListeUtils.addToListaDup(this@RicercaVeloceFragment, 2, 4, mViewModel!!.idDaAgg)
                    return true
                }
                R.id.add_to_e_fine -> {
                    addToListaNoDup(2, 5)
                    return true
                }
                else -> {
                    mViewModel!!.idListaClick = item.groupId
                    mViewModel!!.idPosizioneClick = item.itemId
                    if (mViewModel!!.idListaClick != ID_FITTIZIO && mViewModel!!.idListaClick >= 100) {
                        mViewModel!!.idListaClick -= 100

                        if (listePersonalizzate!![mViewModel!!.idListaClick]
                                        .lista!!
                                        .getCantoPosizione(mViewModel!!.idPosizioneClick) == "") {
                            listePersonalizzate!![mViewModel!!.idListaClick]
                                    .lista!!
                                    .addCanto((mViewModel!!.idDaAgg).toString(), mViewModel!!.idPosizioneClick)
                            ListeUtils.updateListaPersonalizzata(this@RicercaVeloceFragment, listePersonalizzate!![mViewModel!!.idListaClick])
                        } else {
                            if (listePersonalizzate!![mViewModel!!.idListaClick]
                                            .lista!!
                                            .getCantoPosizione(mViewModel!!.idPosizioneClick) == (mViewModel!!.idDaAgg).toString())
                                Snackbar.make(rootView!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                            else {
                                ListeUtils.manageReplaceDialog(this@RicercaVeloceFragment, Integer.parseInt(
                                        listePersonalizzate!![mViewModel!!.idListaClick]
                                                .lista!!
                                                .getCantoPosizione(mViewModel!!.idPosizioneClick)), "VELOCE_REPLACE")
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

    // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta
    // duplicati
    private fun addToListaNoDup(idLista: Int, listPosition: Int) {
        mViewModel!!.idListaDaAgg = idLista
        mViewModel!!.posizioneDaAgg = listPosition
        ListeUtils.addToListaNoDup(this@RicercaVeloceFragment, idLista, listPosition, mViewModel!!.idDaAgg, "VELOCE_REPLACE_2")
    }

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity!!.applicationContext, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            "VELOCE_REPLACE" -> {
                listePersonalizzate!![mViewModel!!.idListaClick]
                        .lista!!
                        .addCanto((mViewModel!!.idDaAgg).toString(), mViewModel!!.idPosizioneClick)
                ListeUtils.updateListaPersonalizzata(this@RicercaVeloceFragment, listePersonalizzate!![mViewModel!!.idListaClick])
            }
            "VELOCE_REPLACE_2" ->
                ListeUtils.updatePosizione(this@RicercaVeloceFragment, mViewModel!!.idDaAgg, mViewModel!!.idListaDaAgg, mViewModel!!.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {}

    private fun ricercaStringa(s: String) {
        val tempText = activity!!.tempTextField.text.toString()
        if (tempText != s) activity!!.tempTextField.setText(s)

        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
                searchTask!!.cancel(true)
            searchTask = SearchTask(this@RicercaVeloceFragment)
            searchTask!!.execute(textfieldRicerca.text.toString())
        } else {
            if (s.isEmpty()) {
                if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
                    searchTask!!.cancel(true)
                search_no_results.visibility = View.GONE
                cantoAdapter.clear()
                search_progress.visibility = View.INVISIBLE
            }
        }
    }

    private class SearchTask internal constructor(fragment: RicercaVeloceFragment) : AsyncTask<String, Void, Int>() {

        private val fragmentReference: WeakReference<RicercaVeloceFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sSearchText: String): Int? {

            Log.d(TAG, "STRINGA: " + sSearchText[0])
            val s = sSearchText[0]

            val stringa = Utility.removeAccents(s).toLowerCase()
            Log.d(TAG, "onTextChanged: stringa $stringa")

            val mDb = RisuscitoDatabase.getInstance(fragmentReference.get()!!.activity as Context)
            val elenco = mDb.cantoDao().allByName

            elenco.filter { Utility.removeAccents(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.titolo, R.string::class.java))).toLowerCase().contains(stringa) }
                    .sortedBy { fragmentReference.get()!!.resources.getString(LUtils.getResId(it.titolo, R.string::class.java)) }
                    .forEach {
                        if (isCancelled) {
                            fragmentReference.get()!!.titoli.clear()
                            return 0
                        }
                        fragmentReference.get()!!.titoli.add(
                                SimpleItem()
                                        .withTitle(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.titolo, R.string::class.java)))
                                        .withColor(it.color!!)
                                        .withPage(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.pagina, R.string::class.java)))
                                        .withId(it.id)
                                        .withSource(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.source, R.string::class.java)))
                                        .withNormalizedTitle(Utility.removeAccents(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.titolo, R.string::class.java))))
                                        .withFilter(stringa)
                                        .withContextMenuListener(fragmentReference.get() as RicercaVeloceFragment)
                        )
                    }
            return 0
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (isCancelled) return
            fragmentReference.get()?.search_no_results?.visibility = View.GONE
            fragmentReference.get()?.search_progress?.visibility = View.VISIBLE
            fragmentReference.get()?.titoli?.clear()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (isCancelled) {
                fragmentReference.get()?.titoli?.clear()
                return
            }
            fragmentReference.get()?.cantoAdapter?.set(fragmentReference.get()?.titoli)
            fragmentReference.get()?.search_progress?.visibility = View.INVISIBLE
            fragmentReference.get()?.search_no_results?.visibility = if (fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    companion object {
        private const val ID_FITTIZIO = 99999999
        private val TAG = RicercaVeloceFragment::class.java.canonicalName
    }

}
