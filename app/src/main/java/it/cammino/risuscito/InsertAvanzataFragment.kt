package it.cammino.risuscito

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
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
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_insert_search.*
import kotlinx.android.synthetic.main.activity_ricerca_avanzata.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.sql.Date
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

class InsertAvanzataFragment : Fragment() {

    internal lateinit var cantoAdapter: FastItemAdapter<InsertItem>
    private lateinit var aTexts: Array<Array<String?>>

    private var titoli: MutableList<InsertItem>? = null
    private var rootView: View? = null
    private var fromAdd: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0
    private var searchTask: SearchTask? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_ricerca_avanzata, container, false)

        val bundle = arguments
        fromAdd = bundle!!.getInt("fromAdd")
        idLista = bundle.getInt("idLista")
        listPosition = bundle.getInt("position")

        try {
            val `in`: InputStream = when (ThemeableActivity.getSystemLocalWrapper(
                    activity!!.resources.configuration)
                    .language) {
                "uk" -> activity!!.assets.open("fileout_uk.xml")
                "en" -> activity!!.assets.open("fileout_en.xml")
                else -> activity!!.assets.open("fileout_new.xml")
            }
            val parser = CantiXmlParser()
            aTexts = parser.parse(`in`)
            `in`.close()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

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
                            val mDao = RisuscitoDatabase.getInstance(context!!).customListDao()
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
                            val mDao = RisuscitoDatabase.getInstance(context!!).listePersDao()
                            val listaPers = mDao.getListById(idLista)
                            if (listaPers?.lista != null) {
                                listaPers.lista!!.addCanto(item.id.toString(), listPosition)
                                mDao.updateLista(listaPers)
                                activity!!.setResult(Activity.RESULT_OK)
                                activity!!.finish()
                                activity!!.overridePendingTransition(0, R.anim.slide_out_right)
                            }
                        })
                        .start()
            }

            activity!!.setResult(Activity.RESULT_OK)
            activity!!.finish()
            activity!!.overridePendingTransition(0, R.anim.slide_out_right)
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

        // Creating new adapter object
        titoli = ArrayList()
        cantoAdapter = FastItemAdapter()
        cantoAdapter.setHasStableIds(true)

        cantoAdapter.withOnClickListener(mOnClickListener).withEventHook(hookListener)

        matchedList.adapter = cantoAdapter
//        val llm = LinearLayoutManager(context)
        val mMainActivity = activity as GeneralInsertSearch?
        val llm = if (mMainActivity!!.isOnTablet)
            GridLayoutManager(context, if (mMainActivity.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        matchedList.layoutManager = llm
        matchedList.setHasFixedSize(true)
        val insetDivider = DividerItemDecoration(context!!, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(context!!, R.drawable.material_inset_divider)!!)
        matchedList.addItemDecoration(insetDivider)

        textfieldRicerca.setText("")

        pulisci_ripple.setOnClickListener { textfieldRicerca.setText("") }

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
            Log.d(TAG, "VISIBLE")
            // to hide soft keyboard
            (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(textfieldRicerca.windowToken, 0)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        if (searchTask != null && searchTask!!.status == Status.RUNNING) searchTask!!.cancel(true)
        super.onDestroy()
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
            if (searchTask != null && searchTask!!.status == Status.RUNNING) searchTask!!.cancel(true)

            searchTask = SearchTask(this@InsertAvanzataFragment)
            searchTask!!.execute(textfieldRicerca.text.toString(), onlyConsegnati.toString())
        } else {
            if (s.isEmpty()) {
                search_no_results.visibility = View.GONE
                cantoAdapter.clear()
                search_progress.visibility = View.INVISIBLE
            }
        }
    }

    private class SearchTask internal constructor(fragment: InsertAvanzataFragment) : AsyncTask<String, Void, Int>() {

        private val fragmentReference: WeakReference<InsertAvanzataFragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: String): Int? {

            Log.d(TAG, "STRINGA: " + params[0])

            val words = params[0].split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var text: String

            for (aText in fragmentReference.get()!!.aTexts) {

                Log.d(TAG, "doInBackground: isCancelled? $isCancelled")
                if (isCancelled) return 0

                if (aText[0] == null || aText[0].equals("", ignoreCase = true)) break

                var found = true
                for (word in words) {
                    if (isCancelled) return 0
                    if (word.trim { it <= ' ' }.length > 1) {
                        text = word.trim { it <= ' ' }
                        text = text.toLowerCase(
                                ThemeableActivity.getSystemLocalWrapper(
                                        fragmentReference.get()!!.activity!!.resources.configuration))
                        val nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD)
                        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                        text = pattern.matcher(nfdNormalizedString).replaceAll("")

                        if (!aText[1]!!.contains(text)) found = false
                    }
                }

                Log.d(TAG, "doInBackground: isCancelled? $isCancelled")

                if (found && !isCancelled) {
                    val mDb = RisuscitoDatabase.getInstance(fragmentReference.get()!!.activity as Context)
                    val elenco: List<Canto>?
                    val onlyConsegnati = java.lang.Boolean.parseBoolean(params[1])
                    elenco = if (onlyConsegnati)
                        mDb.cantoDao().getCantiWithSourceOnlyConsegnati(aText[0]!!)
                    else
                        mDb.cantoDao().getCantiWithSource(aText[0]!!)

                    if (elenco != null) {
                        for (canto in elenco) {
                            if (isCancelled) return 0
                            val insertItem = InsertItem()
                            insertItem
                                    .withTitle(canto.titolo!!)
                                    .withColor(canto.color!!)
                                    .withPage(canto.pagina.toString())
                                    .withId(canto.id)
                                    .withSource(canto.source!!)
                            fragmentReference.get()!!.titoli!!.add(insertItem)
                        }
                    }
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
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (isCancelled) return
            FastAdapterDiffUtil.set(fragmentReference.get()!!.cantoAdapter, fragmentReference.get()!!.titoli)
            fragmentReference.get()!!.search_progress.visibility = View.INVISIBLE
            fragmentReference.get()!!
                    .search_no_results.visibility = if (fragmentReference.get()!!.cantoAdapter.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    companion object {
        private val TAG = InsertAvanzataFragment::class.java.canonicalName
    }
}
