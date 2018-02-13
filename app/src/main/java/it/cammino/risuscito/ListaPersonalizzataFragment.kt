package it.cammino.risuscito

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialcab.MaterialCab
import com.crashlytics.android.Crashlytics
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.lista_pers_button.*
import java.lang.ref.WeakReference

class ListaPersonalizzataFragment : Fragment(), MaterialCab.Callback {

    private lateinit var cantoDaCanc: String

    // create boolean for fetching data
    private var isViewShown = true
    private var posizioneDaCanc: Int = 0
    private var rootView: View? = null
    private var idLista: Int = 0
    private var listaPersonalizzata: ListaPersonalizzata? = null
    private var listaPersonalizzataTitle: String? = null
    private var mSwhitchMode: Boolean = false
    private var posizioniList: MutableList<Pair<PosizioneTitleItem, List<PosizioneItem>>>? = null
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private var cantoAdapter: PosizioneRecyclerAdapter? = null
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
            result.append("-- ").append(listaPersonalizzata!!.name.toUpperCase(l)).append(" --\n")
            for (i in 0 until listaPersonalizzata!!.numPosizioni) {
                result.append(listaPersonalizzata!!.getNomePosizione(i).toUpperCase(l)).append("\n")
                if (!listaPersonalizzata!!.getCantoPosizione(i).equals("", ignoreCase = true)) {
                    for (tempItem in posizioniList!![i].second!!) {
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
                if (i < listaPersonalizzata!!.numPosizioni - 1) result.append("\n")
            }

            return result.toString()
        }

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false)

        mMainActivity = activity as MainActivity?

        mLUtils = LUtils.getInstance(activity)
        mSwhitchMode = false

        idLista = arguments!!.getInt("idLista")

        if (!isViewShown) {
            if (mMainActivity!!.materialCab!!.isActive) mMainActivity!!.materialCab!!.finish()
            val fab1 = (parentFragment as CustomLists).fab
            fab1.show()
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val click = OnClickListener { v ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            val parent = v.parent.parent as View
            if (parent.findViewById<View>(R.id.addCantoGenerico).visibility == View.VISIBLE) {
                if (mSwhitchMode) {
                    scambioConVuoto(
                            Integer.valueOf(
                                    (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                            .text
                                            .toString())!!)
                } else {
                    if (!mMainActivity!!.materialCab!!.isActive) {
                        val bundle = Bundle()
                        bundle.putInt("fromAdd", 0)
                        bundle.putInt("idLista", idLista)
                        bundle.putInt(
                                "position",
                                Integer.valueOf(
                                        (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                                .text
                                                .toString())!!)
                        val intent = Intent(activity, GeneralInsertSearch::class.java)
                        intent.putExtras(bundle)
                        parentFragment!!.startActivityForResult(intent, TAG_INSERT_PERS + idLista)
                        activity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on)
                    }
                }
            } else {
                if (!mSwhitchMode)
                    if (mMainActivity!!.materialCab!!.isActive) {
                        posizioneDaCanc = Integer.valueOf(
                                (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                        .text
                                        .toString())!!
                        snackBarRimuoviCanto(v)
                    } else
                        openPagina(v)
                else {
                    scambioCanto(
                            Integer.valueOf(
                                    (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                            .text
                                            .toString())!!)
                }
            }
        }

        val longClick = OnLongClickListener { v ->
            val parent = v.parent.parent as View
            posizioneDaCanc = Integer.valueOf(
                    (parent.findViewById<View>(R.id.text_id_posizione) as TextView).text.toString())!!
            snackBarRimuoviCanto(v)
            true
        }

        // Creating new adapter object
        posizioniList = ArrayList()
        cantoAdapter = PosizioneRecyclerAdapter(
                themeUtils.primaryColorDark(), posizioniList, click, longClick)
        recycler_list!!.adapter = cantoAdapter

        // Setting the layoutManager
        recycler_list!!.layoutManager = LinearLayoutManager(activity)

        UpdateListTask(this@ListaPersonalizzataFragment).execute()

        button_pulisci.setOnClickListener {
            for (i in 0 until listaPersonalizzata!!.numPosizioni)
                listaPersonalizzata!!.removeCanto(i)
            runUpdate()
        }

        button_condividi.setOnClickListener {
            val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, shareIntent)
            bottomSheetDialog.show(fragmentManager!!, null)
        }

        button_invia_file.setOnClickListener {
            val exportUri = mLUtils!!.listToXML(listaPersonalizzata!!)
            Log.d(TAG, "onClick: exportUri = " + exportUri!!)
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
                if (mMainActivity!!.materialCab!!.isActive) mMainActivity!!.materialCab!!.finish()
                val fab1 = (parentFragment as CustomLists).fab
                fab1.show()
            } else
                isViewShown = false
        }
    }

    override fun onDestroy() {
        if (mMainActivity!!.materialCab!!.isActive) mMainActivity!!.materialCab!!.finish()
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
                Integer.valueOf((v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString())!!)

        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER)
    }

    private fun snackBarRimuoviCanto(view: View) {
        if (mMainActivity!!.materialCab!!.isActive) mMainActivity!!.materialCab!!.finish()
        val parent = view.parent.parent as View
        longclickedPos = Integer.valueOf((parent.findViewById<View>(R.id.tag) as TextView).text.toString())!!
        longClickedChild = Integer.valueOf((view.findViewById<View>(R.id.item_tag) as TextView).text.toString())!!
        if (!mMainActivity!!.isOnTablet)
            activity!!.toolbar_layout!!.setExpanded(true, true)
        mMainActivity!!.materialCab!!.start(this@ListaPersonalizzataFragment)
    }

    private fun scambioCanto(posizioneNew: Int) {
        if (posizioneNew != posizioneDaCanc) {

            val cantoTmp = listaPersonalizzata!!.getCantoPosizione(posizioneNew)
            listaPersonalizzata!!.addCanto(
                    listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc), posizioneNew)
            listaPersonalizzata!!.addCanto(cantoTmp, posizioneDaCanc)

            runUpdate()

            actionModeOk = true
            mMainActivity!!.materialCab!!.finish()
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
        listaPersonalizzata!!.addCanto(
                listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc), posizioneNew)
        listaPersonalizzata!!.removeCanto(posizioneDaCanc)

        runUpdate()

        actionModeOk = true
        mMainActivity!!.materialCab!!.finish()
        Snackbar.make(
                activity!!.findViewById(R.id.main_content),
                R.string.switch_done,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    override fun onCabCreated(cab: MaterialCab, menu: Menu): Boolean {
        Log.d(TAG, "onCabCreated: ")
        cab.setMenu(R.menu.menu_actionmode_lists)
        cab.setTitle("")
        posizioniList!![longclickedPos].second!![longClickedChild].setmSelected(true)
        cantoAdapter!!.notifyItemChanged(longclickedPos)
        menu.findItem(R.id.action_switch_item).icon = IconicsDrawable(activity!!, CommunityMaterial.Icon.cmd_shuffle)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(android.R.color.white)
        menu.findItem(R.id.action_remove_item).icon = IconicsDrawable(activity!!, CommunityMaterial.Icon.cmd_delete)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(android.R.color.white)
        actionModeOk = false
        return true
    }

    override fun onCabItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_remove_item -> {
                cantoDaCanc = listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc)
                listaPersonalizzata!!.removeCanto(posizioneDaCanc)
                runUpdate()
                actionModeOk = true
                mMainActivity!!.materialCab!!.finish()
                Snackbar.make(
                        activity!!.findViewById(R.id.main_content),
                        R.string.song_removed,
                        Snackbar.LENGTH_LONG)
                        .setAction(
                                getString(android.R.string.cancel).toUpperCase()
                        ) {
                            listaPersonalizzata!!.addCanto(cantoDaCanc, posizioneDaCanc)
                            runUpdate()
                        }
                        .setActionTextColor(themeUtils.accentColor())
                        .show()
                mSwhitchMode = false
            }
            R.id.action_switch_item -> {
                mSwhitchMode = true
                cantoDaCanc = listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc)
                mMainActivity!!.materialCab!!.setTitleRes(R.string.switch_started)
                Toast.makeText(
                        activity,
                        resources.getString(R.string.switch_tooltip),
                        Toast.LENGTH_SHORT)
                        .show()
            }
        }
        return true
    }

    override fun onCabFinished(cab: MaterialCab): Boolean {
        mSwhitchMode = false
        if (!actionModeOk) {
            try {
                posizioniList!![longclickedPos].second!![longClickedChild].setmSelected(false)
                cantoAdapter!!.notifyItemChanged(longclickedPos)
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }

        }
        return true
    }

    private fun runUpdate() {
        Thread(
                Runnable {
                    val listaNew = ListaPers()
                    listaNew.lista = listaPersonalizzata
                    listaNew.id = idLista
                    listaNew.titolo = listaPersonalizzataTitle
                    val mDao = RisuscitoDatabase.getInstance(context).listePersDao()
                    mDao.updateLista(listaNew)
                    UpdateListTask(this@ListaPersonalizzataFragment).execute()
                })
                .start()
    }

    private class UpdateListTask internal constructor(fragment: ListaPersonalizzataFragment) : AsyncTask<Void, Void, Int>() {

        private val fragmentReference: WeakReference<ListaPersonalizzataFragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Void): Int? {

            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.activity).listePersDao()
            val listaPers = mDao.getListById(fragmentReference.get()!!.idLista)

            fragmentReference.get()!!.listaPersonalizzata = listaPers.lista
            fragmentReference.get()!!.listaPersonalizzataTitle = listaPers.titolo

            for (cantoIndex in 0 until fragmentReference.get()!!.listaPersonalizzata!!.numPosizioni) {
                val list = ArrayList<PosizioneItem>()
                if (fragmentReference.get()!!.listaPersonalizzata!!.getCantoPosizione(cantoIndex).isNotEmpty()) {

                    val mCantoDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.activity).cantoDao()
                    val cantoTemp = mCantoDao.getCantoById(
                            Integer.parseInt(
                                    fragmentReference.get()!!.listaPersonalizzata!!.getCantoPosizione(cantoIndex)))

                    list.add(
                            PosizioneItem(
                                    cantoTemp.pagina,
                                    cantoTemp.titolo,
                                    cantoTemp.color,
                                    cantoTemp.id,
                                    cantoTemp.source,
                                    ""))
                }


                val result = Pair(
                        PosizioneTitleItem(
                                fragmentReference.get()!!.listaPersonalizzata!!.getNomePosizione(cantoIndex),
                                fragmentReference.get()!!.idLista,
                                cantoIndex,
                                cantoIndex,
                                false),
                        list as List<PosizioneItem>)

                fragmentReference.get()!!.posizioniList!!.add(result)
            }

            return 0
        }

        override fun onPreExecute() {
            super.onPreExecute()
            fragmentReference.get()!!.posizioniList!!.clear()
        }

        override fun onPostExecute(result: Int?) {
            fragmentReference.get()!!.cantoAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        internal val TAG = ListaPersonalizzataFragment::class.java.canonicalName
        private val TAG_INSERT_PERS = 555
    }
}
