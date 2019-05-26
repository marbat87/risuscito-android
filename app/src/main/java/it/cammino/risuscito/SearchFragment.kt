package it.cammino.risuscito

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.AsyncTask.Status
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
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
import com.crashlytics.android.Crashlytics
import com.github.zawadz88.materialpopupmenu.ViewBoundCallback
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.colorRes
import com.mikepenz.iconics.paddingDp
import com.mikepenz.iconics.sizeDp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.ui.makeClearableEditText
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.android.synthetic.main.tinted_progressbar.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference

class SearchFragment : Fragment(), SimpleDialogFragment.SimpleCallback {

    internal val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private lateinit var aTexts: Array<Array<String?>>

    private var rootView: View? = null
    private var listePersonalizzate: List<ListaPers>? = null
    private var mLUtils: LUtils? = null
    private var searchTask: SearchTask? = null
    private var mLastClickTime: Long = 0
    private var mMainActivity: MainActivity? = null

    private lateinit var mViewModel: SimpleIndexViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.search_layout, container, false)

        mMainActivity = activity as MainActivity?
        mMainActivity!!.setupToolbarTitle(R.string.title_activity_search)

        val args = Bundle().apply { putInt("tipoLista", 0) }
        mViewModel = ViewModelProviders.of(this, ViewModelWithArgumentsFactory(activity!!.application, args)).get(SimpleIndexViewModel::class.java)
        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0")!!)
            mViewModel.advancedSearch = currentItem != 0
        }

        try {
            val inputStream: InputStream = when (ThemeableActivity.getSystemLocalWrapper(
                    activity!!.resources.configuration)
                    .language) {
                "uk" -> activity!!.assets.open("fileout_uk.xml")
                "en" -> activity!!.assets.open("fileout_en.xml")
                else -> activity!!.assets.open("fileout_new.xml")
            }
            val parser = CantiXmlParser()
            aTexts = parser.parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Crashlytics.logException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Crashlytics.logException(e)
        }

        mLUtils = LUtils.getInstance(activity!!)

        var sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, SEARCH_REPLACE)
        sFragment?.setmCallback(this)
        sFragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, SEARCH_REPLACE_2)
        sFragment?.setmCallback(this)

        ioThread { listePersonalizzate = RisuscitoDatabase.getInstance(context!!).listePersDao().all }

        subscribeUiFavorites()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity!!.setTabVisible(false)
        mMainActivity!!.enableFab(false)
        mMainActivity!!.enableBottombar(false)

        ricerca_subtitle.text = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)

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
            mViewModel.idDaAgg = item.id
            mViewModel.popupMenu(this, v!!, SEARCH_REPLACE, SEARCH_REPLACE_2, listePersonalizzate)
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

        val icon = IconicsDrawable(context!!)
                .icon(CommunityMaterial.Icon.cmd_close_circle)
                .colorRes(R.color.text_color_secondary)
                .sizeDp(32)
                .paddingDp(8)
        icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        textfieldRicerca.makeClearableEditText(null, null, icon)

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

        more_options.setOnClickListener {
            val popupMenu = popupMenu {
                dropdownGravity = Gravity.END
                section {
                    customItem {
                        layoutResId = R.layout.view_custom_item_checkable
                        dismissOnSelect = false
                        viewBoundCallback = ViewBoundCallback { view ->
                            view.customItemCheckbox.isChecked = mViewModel.advancedSearch
                            view.customItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                                mViewModel.advancedSearch = isChecked
                                ricerca_subtitle.text = if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(R.string.fast_search_subtitle)
                                ricercaStringa(textfieldRicerca.text.toString())
                                dismissPopup()
                            }
                        }
                    }
                }
            }
            popupMenu.show(context!!, view)
        }

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        if (searchTask != null && searchTask!!.status == Status.RUNNING) searchTask!!.cancel(true)
        super.onDestroy()
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            SEARCH_REPLACE -> {
                listePersonalizzate!![mViewModel.idListaClick]
                        .lista!!
                        .addCanto(mViewModel.idDaAgg.toString(), mViewModel.idPosizioneClick)
                ListeUtils.updateListaPersonalizzata(this, listePersonalizzate!![mViewModel.idListaClick])
            }
            SEARCH_REPLACE_2 ->
                ListeUtils.updatePosizione(this, mViewModel.idDaAgg, mViewModel.idListaDaAgg, mViewModel.posizioneDaAgg)
        }
    }

    override fun onNegative(tag: String) {}

    private fun ricercaStringa(s: String) {
        // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim { it <= ' ' }.length >= 3) {
            if (searchTask != null && searchTask!!.status == Status.RUNNING) searchTask!!.cancel(true)
            searchTask = SearchTask(this)
            searchTask!!.execute(textfieldRicerca.text.toString(), mViewModel.advancedSearch)
        } else {
            if (s.isEmpty()) {
                if (searchTask != null && searchTask!!.status == Status.RUNNING)
                    searchTask!!.cancel(true)
                search_no_results.visibility = View.GONE
                cantoAdapter.clear()
                search_progress.visibility = View.INVISIBLE
            }
        }
    }

    private class SearchTask internal constructor(fragment: SearchFragment) : AsyncTask<Any, Void, ArrayList<SimpleItem>>() {

        private val fragmentReference: WeakReference<SearchFragment> = WeakReference(fragment)

        override fun doInBackground(vararg sSearchText: Any): ArrayList<SimpleItem> {

            val titoliResult = ArrayList<SimpleItem>()

            Log.d(TAG, "STRINGA: " + sSearchText[0])
            Log.d(TAG, "ADVANCED: " + sSearchText[1])
            val s = sSearchText[0] as String
            val advanced = sSearchText[1] as Boolean

            if (advanced) {
                val words = s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                var text: String

                for (aText in fragmentReference.get()!!.aTexts) {
                    if (isCancelled) return titoliResult

                    if (aText[0] == null || aText[0].equals("", ignoreCase = true)) break

                    var found = true
                    for (word in words) {
                        if (isCancelled) return titoliResult

                        if (word.trim { it <= ' ' }.length > 1) {
                            text = word.trim { it <= ' ' }
                            text = text.toLowerCase(
                                    ThemeableActivity.getSystemLocalWrapper(
                                            fragmentReference.get()!!.activity!!.resources.configuration))
                            text = Utility.removeAccents(text)

                            if (!aText[1]!!.contains(text)) found = false
                        }
                    }

                    if (found) {
                        Log.d(TAG, "aText[0]: ${aText[0]}")

                        fragmentReference.get()!!.mViewModel.titoli.sortedBy { it.title!!.getText(fragmentReference.get()!!.context) }
                                .filter { it.undecodedSource == aText[0]!! }
                                .forEach {
                                    if (isCancelled) return titoliResult
                                    titoliResult.add(it)
                                }

                    }
                }
            } else {
                val stringa = Utility.removeAccents(s).toLowerCase()
                Log.d(TAG, "onTextChanged: stringa $stringa")

                fragmentReference.get()!!.mViewModel.titoli.sortedBy { it.title!!.getText(fragmentReference.get()!!.context) }
                        .filter { Utility.removeAccents(it.title!!.getText(fragmentReference.get()!!.context)).toLowerCase().contains(stringa) }
                        .forEach {
                            if (isCancelled) return titoliResult
                            titoliResult.add(it.withFilter(stringa))
                        }
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

    private fun subscribeUiFavorites() {
        mViewModel
                .itemsResult!!
                .observe(
                        this,
                        Observer<List<SimpleItem>> { canti ->
                            if (canti != null) {
                                mViewModel.titoli = canti.sortedBy { it.title!!.getText(context) }
                            }
                        })
    }

    companion object {
        private val TAG = SearchFragment::class.java.canonicalName
        private const val SEARCH_REPLACE = "SEARCH_REPLACE"
        private const val SEARCH_REPLACE_2 = "SEARCH_REPLACE_2"
    }
}
