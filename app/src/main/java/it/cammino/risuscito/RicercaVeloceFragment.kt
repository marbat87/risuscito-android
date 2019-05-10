package it.cammino.risuscito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import kotlinx.android.synthetic.main.activity_general_search.*
import kotlinx.android.synthetic.main.ricerca_tab_layout.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import java.lang.ref.WeakReference

class RicercaVeloceFragment : Fragment(), SimpleDialogFragment.SimpleCallback {

    internal val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()

    // create boolean for fetching data
    private var isViewShown = true
    private var rootView: View? = null
    private var listePersonalizzate: List<ListaPers>? = null
    private var searchTask: SearchTask? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0
    private lateinit var mActivity: Activity

    private var mViewModel: SimpleIndexViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.ricerca_tab_layout, container, false)

        mViewModel = ViewModelProviders.of(this).get(SimpleIndexViewModel::class.java)
        if (mViewModel!!.tipoLista == -1) mViewModel!!.tipoLista = 0

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

        populateDb()
        subscribeUiFavorites()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        consegnati_only_view.visibility = View.GONE
        ricerca_subtitle.text = getString(R.string.fast_search_subtitle)

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            var consume = false
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()
                val intent = Intent(activity!!.applicationContext, PaginaRenderActivity::class.java)
                intent.putExtras(bundleOf("pagina" to item.source!!.getText(context), "idCanto" to item.id))
                mLUtils!!.startActivityWithTransition(intent)
                consume = true
            }
            consume
        }

        cantoAdapter.onLongClickListener = { v: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
            mViewModel!!.idDaAgg = item.id
            mViewModel!!.popupMenu(this@RicercaVeloceFragment, v!!, "VELOCE_REPLACE", "VELOCE_REPLACE_2", listePersonalizzate)
            true
        }

        cantoAdapter.setHasStableIds(true)

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
        val tempText = activity?.tempTextField?.text?.toString() ?: ""
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

    private class SearchTask internal constructor(fragment: RicercaVeloceFragment) : AsyncTask<String, Void, ArrayList<SimpleItem>>() {

        private val fragmentReference: WeakReference<RicercaVeloceFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sSearchText: String): ArrayList<SimpleItem>? {

            val titoliResult = ArrayList<SimpleItem>()

            Log.d(TAG, "STRINGA: " + sSearchText[0])
            val s = sSearchText[0]

            val stringa = Utility.removeAccents(s).toLowerCase()
            Log.d(TAG, "onTextChanged: stringa $stringa")

            fragmentReference.get()!!.mViewModel!!.titoli.sortedBy { it.title!!.getText(fragmentReference.get()!!.context) }
                    .filter { Utility.removeAccents(it.title!!.getText(fragmentReference.get()!!.context)).toLowerCase().contains(stringa) }
                    .forEach {
                        if (isCancelled) return titoliResult
                        titoliResult.add(it.withFilter(stringa))
                    }

            return titoliResult
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (isCancelled) return
            fragmentReference.get()?.search_no_results?.visibility = View.GONE
            fragmentReference.get()?.search_progress?.visibility = View.VISIBLE
        }

        override fun onPostExecute(titoliResult: ArrayList<SimpleItem>) {
            super.onPostExecute(titoliResult)
            if (isCancelled) return
            fragmentReference.get()?.cantoAdapter?.set(titoliResult)
            fragmentReference.get()?.search_progress?.visibility = View.INVISIBLE
            fragmentReference.get()?.search_no_results?.visibility = if (fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    private fun populateDb() {
        mViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mViewModel!!
                .itemsResult!!
                .observe(
                        this,
                        Observer<List<SimpleItem>> { canti ->
                            if (canti != null) {
                                mViewModel!!.titoli = canti.sortedBy { it.title!!.getText(context) }
                            }
                        })
    }

    companion object {
        private val TAG = RicercaVeloceFragment::class.java.canonicalName
    }

}
