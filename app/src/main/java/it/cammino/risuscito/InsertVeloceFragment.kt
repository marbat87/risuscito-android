package it.cammino.risuscito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.listeners.OnClickListener
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.items.InsertItem
import kotlinx.android.synthetic.main.activity_insert_search.*
import kotlinx.android.synthetic.main.activity_ricerca_titolo.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import java.lang.ref.WeakReference
import java.sql.Date
import java.util.*

class InsertVeloceFragment : Fragment() {

    internal lateinit var cantoAdapter: FastItemAdapter<InsertItem>

    private var titoli: MutableList<InsertItem>? = null
    private var searchTask: SearchTask? = null
    private var rootView: View? = null
    private var fromAdd: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_ricerca_titolo, container, false)

        val bundle = arguments
        fromAdd = bundle!!.getInt("fromAdd")
        idLista = bundle.getInt("idLista")
        listPosition = bundle.getInt("position")

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

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mOnClickListener = OnClickListener<InsertItem> { _, _, item, _ ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener true
            mLastClickTime = SystemClock.elapsedRealtime()

            if (fromAdd == 1) {
                Thread(
                        Runnable {
                            val mDao = RisuscitoDatabase.getInstance(context).customListDao()
                            val position = CustomList()
                            position.id = idLista
                            position.position = listPosition
                            position.idCanto = item.id
                            position.timestamp = Date(System.currentTimeMillis())
                            try {
                                mDao.insertPosition(position)
                            } catch (e: Exception) {
                                Snackbar.make(rootView!!, R.string.present_yet, Snackbar.LENGTH_SHORT)
                                        .show()
                            }

                            activity!!.setResult(Activity.RESULT_OK)
                            activity!!.finish()
                            activity!!.overridePendingTransition(0, R.anim.slide_out_right)
                        })
                        .start()
            } else {
                Thread(
                        Runnable {
                            val mDao = RisuscitoDatabase.getInstance(context).listePersDao()
                            val listaPers = mDao.getListById(idLista)
                            if (listaPers?.lista != null) {
                                listaPers.lista.addCanto(item.id.toString(), listPosition)
                                mDao.updateLista(listaPers)
                                activity!!.setResult(Activity.RESULT_OK)
                                activity!!.finish()
                                activity!!.overridePendingTransition(0, R.anim.slide_out_right)
                            }
                        })
                        .start()
            }
            true
        }

        val hookListener = object : ClickEventHook<InsertItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return (viewHolder as? InsertItem.ViewHolder)?.mPreview
            }

            override fun onClick(view: View, i: Int, fastAdapter: FastAdapter<InsertItem>, item: InsertItem) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return
                mLastClickTime = SystemClock.elapsedRealtime()

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della
                // pagina da visualizzare
                val bundle = Bundle()
                bundle.putString("pagina", item.source.toString())
                bundle.putInt("idCanto", item.id)

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle)
            }
        }

        titoli = ArrayList()
        cantoAdapter = FastItemAdapter()
        cantoAdapter.setHasStableIds(true)

        cantoAdapter.withOnClickListener(mOnClickListener).withEventHook(hookListener)

        matchedList.adapter = cantoAdapter
        val llm = LinearLayoutManager(context)
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

        consegnati_only_check.setOnCheckedChangeListener { _, isChecked ->
            if (!textfieldRicerca.text.toString().isEmpty())
                ricercaStringa(textfieldRicerca?.text.toString(), isChecked)
        }

        textfieldRicerca.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
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
                        ricercaStringa(s.toString(), consegnati_only_check.isChecked)
                    }
                }
        )

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
        if (isResumed && isVisibleToUser) {
            Log.d(TAG, "VISIBLE: ")
            // to hide soft keyboard
            (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(textfieldRicerca?.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
            searchTask!!.cancel(true)
    }

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity!!.applicationContext, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    private fun ricercaStringa(s: String, onlyConsegnati: Boolean) {
        val tempText = (activity!!.findViewById<View>(R.id.tempTextField) as EditText).text.toString()
        if (tempText != s) (activity!!.findViewById<View>(R.id.tempTextField) as EditText).setText(s)

        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            if (searchTask != null && searchTask!!.status == AsyncTask.Status.RUNNING)
                searchTask!!.cancel(true)
            searchTask = SearchTask(this@InsertVeloceFragment)
            searchTask!!.execute(textfieldRicerca.text.toString(), onlyConsegnati.toString())
        } else {
            if (s.isEmpty()) {
                search_no_results.visibility = View.GONE
                cantoAdapter.clear()
                search_progress.visibility = View.INVISIBLE
            }
        }
    }

    private class SearchTask internal constructor(fragment: InsertVeloceFragment) : AsyncTask<String, Void, Int>() {

        private val fragmentReference: WeakReference<InsertVeloceFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sParam: String): Int? {

            Log.d(javaClass.name, "STRINGA: " + sParam[0])

            val stringa = Utility.removeAccents(sParam[0]).toLowerCase()
            var titoloTemp: String
            Log.d(javaClass.name, "onTextChanged: stringa " + stringa)

            val mDb = RisuscitoDatabase.getInstance(fragmentReference.get()!!.activity)
            val elenco: List<Canto>
            val onlyConsegnati = java.lang.Boolean.parseBoolean(sParam[1])
            elenco = if (onlyConsegnati)
                mDb.cantoDao().allByNameOnlyConsegnati
            else
                mDb.cantoDao().allByName

            for (canto in elenco) {
                if (isCancelled) return 0
                titoloTemp = Utility.removeAccents(canto.titolo.toLowerCase())
                if (titoloTemp.contains(stringa)) {
                    val insertItem = InsertItem()
                    insertItem
                            .withTitle(canto.titolo)
                            .withColor(canto.color)
                            .withPage(canto.pagina.toString())
                            .withId(canto.id)
                            .withSource(canto.source)
                            .withNormalizedTitle(titoloTemp)
                            .withFilter(stringa)
                    fragmentReference.get()!!.titoli!!.add(insertItem)
                }
            }

            return 0
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (isCancelled) return
            fragmentReference.get()!!.search_no_results.visibility = View.GONE
            fragmentReference.get()!!.search_progress.visibility = View.VISIBLE
            fragmentReference.get()!!.titoli!!.clear()
            //      fragmentReference.get().cantoAdapter.clear();
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (isCancelled) return
            FastAdapterDiffUtil.set(fragmentReference.get()!!.cantoAdapter, fragmentReference.get()!!.titoli)
            fragmentReference.get()!!.search_progress.visibility = View.INVISIBLE
            fragmentReference
                    .get()!!
                    .search_no_results.visibility = if (fragmentReference.get()!!.cantoAdapter.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    companion object {
        private val TAG = InsertVeloceFragment::class.java.canonicalName
    }

}
