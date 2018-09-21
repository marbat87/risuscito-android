package it.cammino.risuscito

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialcab.MaterialCab
import com.crashlytics.android.Crashlytics
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.ListaPersonalizzataViewModel
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.lista_pers_button.*

class ListaPersonalizzataFragment : Fragment() {

    private lateinit var cantoDaCanc: String

    private var mCantiViewModel: ListaPersonalizzataViewModel? = null
    // create boolean for fetching data
    private var isViewShown = true
    private var posizioneDaCanc: Int = 0
    private var rootView: View? = null
    private var idLista: Int = 0
    private var mSwhitchMode: Boolean = false
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private var cantoAdapter: FastItemAdapter<ListaPersonalizzataItem> = FastItemAdapter()
    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    private val shareIntent: Intent
        get() = Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, titlesList)
                .setType("text/plain")

    private val titlesList: String
        get() {

            val l = ThemeableActivity.getSystemLocalWrapper(activity!!.resources.configuration)
            val result = StringBuilder()
            result.append("-- ").append(mCantiViewModel!!.listaPersonalizzata!!.name!!.toUpperCase(l)).append(" --\n")
            for (i in 0 until mCantiViewModel!!.listaPersonalizzata!!.numPosizioni) {
                result.append(mCantiViewModel!!.listaPersonalizzata!!.getNomePosizione(i).toUpperCase(l)).append("\n")
                if (!mCantiViewModel!!.listaPersonalizzata!!.getCantoPosizione(i).equals("", ignoreCase = true)) {
                    for (tempItem in mCantiViewModel!!.posizioniList[i].listItem!!) {
                        result
                                .append(tempItem.titolo)
                                .append(" - ")
                                .append(getString(R.string.page_contracted))
                                .append(tempItem.pagina)
                        result.append("\n")
                    }
                } else {
                    result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<")
                    result.append("\n")
                }
                if (i < mCantiViewModel!!.listaPersonalizzata!!.numPosizioni - 1) result.append("\n")
            }

            return result.toString()
        }

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener
        mLastClickTime = SystemClock.elapsedRealtime()
        val parent = v.parent.parent as View
        if (parent.findViewById<View>(R.id.addCantoGenerico).visibility == View.VISIBLE) {
            if (mSwhitchMode) {
                scambioConVuoto(
                        Integer.valueOf(
                                (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                        .text
                                        .toString()))
            } else {
                if (!MaterialCab.isActive) {
                    val bundle = Bundle()
                    bundle.putInt("fromAdd", 0)
                    bundle.putInt("idLista", idLista)
                    bundle.putInt(
                            "position",
                            Integer.valueOf(
                                    (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                            .text
                                            .toString()))
                    val intent = Intent(activity, GeneralInsertSearch::class.java)
                    intent.putExtras(bundle)
                    parentFragment!!.startActivityForResult(intent, TAG_INSERT_PERS + idLista)
                    activity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on)
                }
            }
        } else {
            if (!mSwhitchMode)
                if (MaterialCab.isActive) {
                    posizioneDaCanc = Integer.valueOf(
                            (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                    .text
                                    .toString())
                    snackBarRimuoviCanto(v)
                } else
                    openPagina(v)
            else {
                scambioCanto(
                        Integer.valueOf(
                                (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                        .text
                                        .toString()))
            }
        }
    }

    private val longClick = OnLongClickListener { v ->
        val parent = v.parent.parent as View
        posizioneDaCanc = Integer.valueOf(
                (parent.findViewById<View>(R.id.text_id_posizione) as TextView).text.toString())
        snackBarRimuoviCanto(v)
        true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        populateDb()
        subscribeUiChanges()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get<ListaPersonalizzataViewModel>(ListaPersonalizzataViewModel::class.java)

        mMainActivity = activity as MainActivity?

        mLUtils = LUtils.getInstance(activity!!)
        mSwhitchMode = false

        idLista = arguments!!.getInt("idLista")

        if (!isViewShown) {
            if (MaterialCab.isActive) MaterialCab.destroy()
            val fab1 = (parentFragment as CustomLists).getFab()
            fab1.show()
            (parentFragment as CustomLists).initFabOptions(true)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Creating new adapter object
        cantoAdapter.setHasStableIds(true)
        cantoAdapter.set(mCantiViewModel!!.posizioniList)
        recycler_list!!.adapter = cantoAdapter

        // Setting the layoutManager
        recycler_list!!.layoutManager = LinearLayoutManager(activity)

        button_pulisci.setOnClickListener {
            for (i in 0 until mCantiViewModel!!.listaPersonalizzata!!.numPosizioni)
                mCantiViewModel!!.listaPersonalizzata!!.removeCanto(i)
            runUpdate()
        }

        button_condividi.setOnClickListener {
            val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, shareIntent)
            bottomSheetDialog.show(fragmentManager!!, null)
        }

        button_invia_file.setOnClickListener {
            val exportUri = mLUtils!!.listToXML(mCantiViewModel!!.listaPersonalizzata!!)
            Log.d(TAG, "onClick: exportUri = " + exportUri!!)
            @Suppress("SENSELESS_COMPARISON")
            if (exportUri != null) {
                val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, getSendIntent(exportUri))
                bottomSheetDialog.show(fragmentManager!!, null)
            } else
                Snackbar.make(
                        activity!!.findViewById(R.id.main_content),
                        R.string.xml_error,
                        Snackbar.LENGTH_LONG)
                        .show()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (view != null) {
                isViewShown = true
                if (MaterialCab.isActive) MaterialCab.destroy()
                val fab1 = (parentFragment as CustomLists).getFab()
                fab1.show()
                (parentFragment as CustomLists).initFabOptions(true)
            } else
                isViewShown = false
        }
    }

    override fun onDestroy() {
        if (MaterialCab.isActive) MaterialCab.destroy()
        super.onDestroy()
    }

    private fun getSendIntent(exportUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, exportUri)
                .setType("text/xml")
    }

    private fun openPagina(v: View) {
        // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da
        // visualizzare
        val bundle = Bundle()
        bundle.putString(
                "pagina", (v.findViewById<View>(R.id.text_source_canto) as TextView).text.toString())
        bundle.putInt(
                "idCanto",
                Integer.valueOf((v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString()))

        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    private fun snackBarRimuoviCanto(view: View) {
        if (MaterialCab.isActive) MaterialCab.destroy()
        val parent = view.parent.parent as View
        longclickedPos = Integer.valueOf(parent.generic_tag.text.toString())
        longClickedChild = Integer.valueOf(view.item_tag.text.toString())
        if (!mMainActivity!!.isOnTablet)
            activity!!.toolbar_layout!!.setExpanded(true, true)
        startCab(false)
    }

    private fun scambioCanto(posizioneNew: Int) {
        if (posizioneNew != posizioneDaCanc) {

            val cantoTmp = mCantiViewModel!!.listaPersonalizzata!!.getCantoPosizione(posizioneNew)
            mCantiViewModel!!.listaPersonalizzata!!.addCanto(
                    mCantiViewModel!!.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc), posizioneNew)
            mCantiViewModel!!.listaPersonalizzata!!.addCanto(cantoTmp, posizioneDaCanc)

            runUpdate()

            actionModeOk = true
            MaterialCab.destroy()
            Snackbar.make(
                    activity!!.findViewById(R.id.main_content),
                    R.string.switch_done,
                    Snackbar.LENGTH_SHORT)
                    .show()

        } else
            Snackbar.make(rootView!!, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
    }

    private fun scambioConVuoto(posizioneNew: Int) {
        //        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
        //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        mCantiViewModel!!.listaPersonalizzata!!.addCanto(
                mCantiViewModel!!.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc), posizioneNew)
        mCantiViewModel!!.listaPersonalizzata!!.removeCanto(posizioneDaCanc)

        runUpdate()

        actionModeOk = true
        MaterialCab.destroy()
        Snackbar.make(
                activity!!.findViewById(R.id.main_content),
                R.string.switch_done,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    private fun startCab(switchMode: Boolean) {
        mSwhitchMode = switchMode
        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
            if (switchMode)
                titleRes(R.string.switch_started)
            else
                title = resources.getQuantityString(R.plurals.item_selected, 1, 1)
            popupTheme = R.style.ThemeOverlay_MaterialComponents_Dark_ActionBar
            contentInsetStartRes(R.dimen.mcab_default_content_inset)
            menuRes = R.menu.menu_actionmode_lists
            backgroundColor = themeUtils.primaryColorDark()

            onCreate { _, _ ->
                Log.d(TAG, "MaterialCab onCreate")
                mCantiViewModel!!.posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(true)
                cantoAdapter.notifyItemChanged(longclickedPos)
                actionModeOk = false
            }

            onSelection { item ->
                Log.d(TAG, "MaterialCab onSelection")
                when (item.itemId) {
                    R.id.action_remove_item -> {
                        cantoDaCanc = mCantiViewModel!!.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc)
                        mCantiViewModel!!.listaPersonalizzata!!.removeCanto(posizioneDaCanc)
                        runUpdate()
                        actionModeOk = true
                        MaterialCab.destroy()
                        Snackbar.make(
                                activity!!.findViewById(R.id.main_content),
                                R.string.song_removed,
                                Snackbar.LENGTH_LONG)
                                .setAction(
                                        getString(android.R.string.cancel).toUpperCase()
                                ) {
                                    mCantiViewModel!!.listaPersonalizzata!!.addCanto(cantoDaCanc, posizioneDaCanc)
                                    runUpdate()
                                }
                                .setActionTextColor(themeUtils.accentColor())
                                .show()
                        true
                    }
                    R.id.action_switch_item -> {
                        cantoDaCanc = mCantiViewModel!!.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc)
                        startCab(true)
                        Toast.makeText(
                                activity,
                                resources.getString(R.string.switch_tooltip),
                                Toast.LENGTH_SHORT)
                                .show()
                        true
                    }
                    else -> false
                }
            }

            onDestroy {
                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                mSwhitchMode = false
                if (!actionModeOk) {
                    try {
                        mCantiViewModel!!.posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(false)
                        cantoAdapter.notifyItemChanged(longclickedPos)
                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                    }
                }
                true
            }
        }
    }

    private fun runUpdate() {
        Thread(
                Runnable {
                    val listaNew = ListaPers()
                    listaNew.lista = mCantiViewModel!!.listaPersonalizzata
                    listaNew.id = idLista
                    listaNew.titolo = mCantiViewModel!!.listaPersonalizzataTitle
                    val mDao = RisuscitoDatabase.getInstance(this@ListaPersonalizzataFragment.mMainActivity!!).listePersDao()
                    mDao.updateLista(listaNew)
                })
                .start()
    }

    private fun populateDb() {
        mCantiViewModel!!.listaPersonalizzataId = idLista
        mCantiViewModel!!.createDb()
    }

    private fun subscribeUiChanges() {
        mCantiViewModel!!
                .listaPersonalizzataResult
                .observe(
                        this,
                        Observer { listaPersonalizzataResult ->
                            Log.d(TAG, "onChanged")
                            mCantiViewModel!!.posizioniList = listaPersonalizzataResult!!.map { it ->
                                it.withClickListener(click)
                                        .withLongClickListener(longClick)
                                        .withSelectedColor(themeUtils.primaryColorDark())
                                        .listItem!!.forEach {
                                    try {
                                        it.titolo = resources.getString(LUtils.getResId(it.titolo!!, R.string::class.java))
                                        it.pagina = resources.getString(LUtils.getResId(it.pagina!!, R.string::class.java))
                                        it.source = resources.getString(LUtils.getResId(it.source!!, R.string::class.java))
                                    } catch (e: Exception) {
                                        Log.d(TAG, "titolo ${it.titolo}")
                                    }
                                }
                                it
                            }
                            cantoAdapter.set(mCantiViewModel!!.posizioniList)
                        })
    }

    companion object {
        internal val TAG = ListaPersonalizzataFragment::class.java.canonicalName
        private const val TAG_INSERT_PERS = 555
    }
}
