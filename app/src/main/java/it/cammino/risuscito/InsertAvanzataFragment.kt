package it.cammino.risuscito

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.AsyncTask.Status
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ListeUtils
import kotlinx.android.synthetic.main.activity_insert_search.*
import kotlinx.android.synthetic.main.ricerca_tab_layout.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference

class InsertAvanzataFragment : Fragment() {

    internal val cantoAdapter: FastItemAdapter<InsertItem> = FastItemAdapter()
    private lateinit var aTexts: Array<Array<String?>>

    private var titoli: List<InsertItem> = ArrayList()
    private var rootView: View? = null
    private var fromAdd: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0
    private var searchTask: SearchTask? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.ricerca_tab_layout, container, false)

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
        ricerca_subtitle.text = getString(R.string.advanced_search_subtitle)

        cantoAdapter.onClickListener = { _: View?, _: IAdapter<InsertItem>, item: InsertItem, _: Int ->
            if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                mLastClickTime = SystemClock.elapsedRealtime()

                if (fromAdd == 1) {
                    ListeUtils.addToListaDupAndFinish(activity!!, idLista, listPosition, item.id)
                } else {
                    ListeUtils.updateListaPersonalizzataAndFinish(activity!!, idLista, item.id, listPosition)
                }
            }
            true
        }

        cantoAdapter.addEventHook(object : ClickEventHook<InsertItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return (viewHolder as? InsertItem.ViewHolder)?.mPreview
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<InsertItem>, item: InsertItem) {
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
        })

        // Creating new adapter object
//        cantoAdapter = FastItemAdapter()
        cantoAdapter.setHasStableIds(true)

//        cantoAdapter.addEventHook(hookListener)

        matchedList.adapter = cantoAdapter
        val mMainActivity = activity as GeneralInsertSearch?
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

        textfieldRicerca.setText("")

        pulisci_ripple.setOnClickListener { textfieldRicerca.setText("") }

        consegnati_only_check.setOnCheckedChangeListener { _, isChecked ->
            if (textfieldRicerca.text.toString().isNotEmpty())
                ricercaStringa(textfieldRicerca.text.toString(), isChecked)
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
                        if (consegnati_only_check != null)
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
            (ContextCompat.getSystemService(context as Context, InputMethodManager::class.java) as InputMethodManager)
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
        val tempText = activity?.tempTextField?.text?.toString() ?: ""
        if (tempText != s) activity!!.tempTextField.setText(s)

        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            if (searchTask != null && searchTask!!.status == Status.RUNNING) searchTask!!.cancel(true)

            searchTask = SearchTask(this@InsertAvanzataFragment)
            searchTask!!.execute(textfieldRicerca.text.toString(), onlyConsegnati.toString())
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

    private class SearchTask internal constructor(fragment: InsertAvanzataFragment) : AsyncTask<String, Void, Int>() {

        private val fragmentReference: WeakReference<InsertAvanzataFragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: String): Int? {

            Log.d(TAG, "STRINGA: " + params[0])

            val words = params[0].split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var text: String

            val tempList = ArrayList<InsertItem>()
            for (aText in fragmentReference.get()!!.aTexts) {
                Log.d(TAG, "doInBackground: isCancelled? $isCancelled")
                if (isCancelled) {
//                    fragmentReference.get()?.titoli?.clear()
                    fragmentReference.get()?.titoli = ArrayList()
                    return 0
                }

                if (aText[0] == null || aText[0].equals("", ignoreCase = true)) break

                var found = true
                for (word in words) {
                    if (isCancelled) {
//                        fragmentReference.get()?.titoli?.clear()
                        fragmentReference.get()?.titoli = ArrayList()
                        return 0
                    }
                    if (word.trim { it <= ' ' }.length > 1) {
                        text = word.trim { it <= ' ' }
                        text = text.toLowerCase(
                                ThemeableActivity.getSystemLocalWrapper(
                                        fragmentReference.get()!!.activity!!.resources.configuration))
//                        val nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD)
//                        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
//                        text = pattern.matcher(nfdNormalizedString).replaceAll("")
                        text = Utility.removeAccents(text)

                        if (!aText[1]!!.contains(text)) found = false
                    }
                }

                Log.d(TAG, "doInBackground: isCancelled? $isCancelled")

                if (found) {
                    val mDb = RisuscitoDatabase.getInstance(fragmentReference.get()!!.activity as Context)
                    val elenco: List<Canto>?
                    val onlyConsegnati = java.lang.Boolean.parseBoolean(params[1])
                    elenco = if (onlyConsegnati)
                        mDb.cantoDao().getCantiWithSourceOnlyConsegnati(aText[0]!!)
                    else
                        mDb.cantoDao().getCantiWithSource(aText[0]!!)

                    elenco?.sortedBy { fragmentReference.get()!!.resources.getString(LUtils.getResId(it.titolo, R.string::class.java)) }
                            ?.forEach {
                                if (isCancelled) {
//                                    fragmentReference.get()!!.titoli.clear()
                                    fragmentReference.get()!!.titoli = ArrayList()
                                    return 0
                                }
//                                fragmentReference.get()!!.titoli.add(
                                tempList.add(
                                        InsertItem()
                                                .withTitle(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.titolo, R.string::class.java)))
                                                .withColor(it.color!!)
                                                .withPage(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.pagina, R.string::class.java)))
                                                .withId(it.id)
                                                .withSource(fragmentReference.get()!!.resources.getString(LUtils.getResId(it.source, R.string::class.java)))
                                )
                            }

                }
            }
            fragmentReference.get()!!.titoli = tempList
            return 0
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (isCancelled) return
            fragmentReference.get()?.search_no_results?.visibility = View.GONE
            fragmentReference.get()?.search_progress?.visibility = View.VISIBLE
//            fragmentReference.get()?.titoli?.clear()
            fragmentReference.get()?.titoli = ArrayList()

        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (isCancelled) {
//                fragmentReference.get()?.titoli?.clear()
                fragmentReference.get()?.titoli = ArrayList()
                return
            }
            fragmentReference.get()?.cantoAdapter?.set(fragmentReference.get()?.titoli
                    ?: ArrayList())
            fragmentReference.get()?.search_progress?.visibility = View.INVISIBLE
            fragmentReference.get()?.search_no_results?.visibility = if (fragmentReference.get()?.cantoAdapter?.adapterItemCount == 0)
                View.VISIBLE
            else
                View.GONE
        }
    }

    companion object {
        private val TAG = InsertAvanzataFragment::class.java.canonicalName
    }
}
