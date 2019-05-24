package it.cammino.risuscito

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialcab.MaterialCab.Companion.destroy
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.viewmodels.ListaPersonalizzataViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.lista_pers_button.*

class ListaPersonalizzataFragment : Fragment() {

    private lateinit var cantoDaCanc: String

    private lateinit var mCantiViewModel: ListaPersonalizzataViewModel
    private var isViewShown = true
    private var posizioneDaCanc: Int = 0
    private var rootView: View? = null
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
            result.append("-- ").append(mCantiViewModel.listaPersonalizzata!!.name!!.toUpperCase(l)).append(" --\n")
            for (i in 0 until mCantiViewModel.listaPersonalizzata!!.numPosizioni) {
                result.append(mCantiViewModel.listaPersonalizzata!!.getNomePosizione(i).toUpperCase(l)).append("\n")
                if (!mCantiViewModel.listaPersonalizzata!!.getCantoPosizione(i).equals("", ignoreCase = true)) {
                    for (tempItem in mCantiViewModel.posizioniList[i].listItem!!) {
                        result
                                .append(tempItem.title!!.getText(context))
                                .append(" - ")
                                .append(getString(R.string.page_contracted))
                                .append(tempItem.page!!.getText(context))
                        result.append("\n")
                    }
                } else {
                    result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<")
                    result.append("\n")
                }
                if (i < mCantiViewModel.listaPersonalizzata!!.numPosizioni - 1) result.append("\n")
            }

            return result.toString()
        }

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener
        mLastClickTime = SystemClock.elapsedRealtime()
        val parent = v.parent.parent as View
        if (parent.addCantoGenerico.isVisible) {
            if (mSwhitchMode) {
                scambioConVuoto(
                        Integer.valueOf(parent.text_id_posizione.text.toString()))
            } else {
                if (!MaterialCab.isActive) {
                    val intent = Intent(activity, InsertActivity::class.java)
                    intent.putExtras(bundleOf("fromAdd" to 0,
                            "idLista" to mCantiViewModel.listaPersonalizzataId,
                            "position" to Integer.valueOf(parent.text_id_posizione.text.toString())))
                    parentFragment!!.startActivityForResult(intent, TAG_INSERT_PERS)
                    Animatoo.animateShrink(activity)
                }
            }
        } else {
            if (!mSwhitchMode)
                if (MaterialCab.isActive) {
                    posizioneDaCanc = Integer.valueOf(parent.text_id_posizione.text.toString())
                    snackBarRimuoviCanto(v)
                } else
                    openPagina(v)
            else {
                scambioCanto(
                        Integer.valueOf(parent.text_id_posizione.text.toString()))
            }
        }
    }

    private val longClick = OnLongClickListener { v ->
        val parent = v.parent.parent as View
        posizioneDaCanc = Integer.valueOf(parent.text_id_posizione.text.toString())
        snackBarRimuoviCanto(v)
        true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribeUiChanges()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false)

        val args = Bundle().apply { putInt("tipoLista", arguments!!.getInt("idLista")) }
        mCantiViewModel = ViewModelProviders.of(this, ViewModelWithArgumentsFactory(activity!!.application, args)).get(ListaPersonalizzataViewModel::class.java)

        mMainActivity = activity as MainActivity?

        mLUtils = LUtils.getInstance(activity!!)
        mSwhitchMode = false

        if (!isViewShown) {
            destroy()
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
        cantoAdapter.set(mCantiViewModel.posizioniList)
        recycler_list!!.adapter = cantoAdapter

        // Setting the layoutManager
        recycler_list!!.layoutManager = LinearLayoutManager(activity)

        button_pulisci.setOnClickListener {
            for (i in 0 until mCantiViewModel.listaPersonalizzata!!.numPosizioni)
                mCantiViewModel.listaPersonalizzata!!.removeCanto(i)
            runUpdate()
        }

        button_condividi.setOnClickListener {
            val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, shareIntent)
            bottomSheetDialog.show(fragmentManager!!, null)
        }

        button_invia_file.setOnClickListener {
            val exportUri = mLUtils!!.listToXML(mCantiViewModel.listaPersonalizzata!!)
            Log.d(TAG, "onClick: exportUri = " + exportUri!!)
            @Suppress("SENSELESS_COMPARISON")
            if (exportUri != null) {
                val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, getSendIntent(exportUri))
                bottomSheetDialog.show(fragmentManager!!, null)
            } else
                Snackbar.make(
                        activity!!.main_content,
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
                destroy()
                val fab1 = (parentFragment as CustomLists).getFab()
                fab1.show()
                (parentFragment as CustomLists).initFabOptions(true)
            } else
                isViewShown = false
        }
    }

    override fun onDestroy() {
        destroy()
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
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundleOf("pagina" to v.text_source_canto.text.toString(),
                "idCanto" to Integer.valueOf(v.text_id_canto_card.text.toString())))
        mLUtils!!.startActivityWithTransition(intent)
    }

    private fun snackBarRimuoviCanto(view: View) {
        destroy()
        val parent = view.parent.parent as View
        longclickedPos = Integer.valueOf(parent.generic_tag.text.toString())
        longClickedChild = Integer.valueOf(view.item_tag.text.toString())
        if (!mMainActivity!!.isOnTablet)
            activity!!.toolbar_layout!!.setExpanded(true, true)
        startCab(false)
    }

    private fun scambioCanto(posizioneNew: Int) {
        if (posizioneNew != posizioneDaCanc) {

            val cantoTmp = mCantiViewModel.listaPersonalizzata!!.getCantoPosizione(posizioneNew)
            mCantiViewModel.listaPersonalizzata!!.addCanto(
                    mCantiViewModel.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc), posizioneNew)
            mCantiViewModel.listaPersonalizzata!!.addCanto(cantoTmp, posizioneDaCanc)

            runUpdate()

            actionModeOk = true
            destroy()
            Snackbar.make(
                    activity!!.main_content,
                    R.string.switch_done,
                    Snackbar.LENGTH_SHORT)
                    .show()

        } else
            Snackbar.make(rootView!!, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
    }

    private fun scambioConVuoto(posizioneNew: Int) {
        //        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
        //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        mCantiViewModel.listaPersonalizzata!!.addCanto(
                mCantiViewModel.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc), posizioneNew)
        mCantiViewModel.listaPersonalizzata!!.removeCanto(posizioneDaCanc)

        runUpdate()

        actionModeOk = true
        destroy()
        Snackbar.make(
                activity!!.main_content,
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
                mCantiViewModel.posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(true)
                cantoAdapter.notifyItemChanged(longclickedPos)
                actionModeOk = false
            }

            onSelection { item ->
                Log.d(TAG, "MaterialCab onSelection")
                when (item.itemId) {
                    R.id.action_remove_item -> {
                        cantoDaCanc = mCantiViewModel.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc)
                        mCantiViewModel.listaPersonalizzata!!.removeCanto(posizioneDaCanc)
                        runUpdate()
                        actionModeOk = true
                        destroy()
                        Snackbar.make(
                                activity!!.main_content,
                                R.string.song_removed,
                                Snackbar.LENGTH_LONG)
                                .setAction(
                                        getString(R.string.cancel).toUpperCase()
                                ) {
                                    mCantiViewModel.listaPersonalizzata!!.addCanto(cantoDaCanc, posizioneDaCanc)
                                    runUpdate()
                                }
                                .show()
                        true
                    }
                    R.id.action_switch_item -> {
                        cantoDaCanc = mCantiViewModel.listaPersonalizzata!!.getCantoPosizione(posizioneDaCanc)
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
                        mCantiViewModel.posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(false)
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
        ioThread {
            val listaNew = ListaPers()
            listaNew.lista = mCantiViewModel.listaPersonalizzata
            listaNew.id = mCantiViewModel.listaPersonalizzataId
            listaNew.titolo = mCantiViewModel.listaPersonalizzataTitle
            val mDao = RisuscitoDatabase.getInstance(this@ListaPersonalizzataFragment.mMainActivity!!).listePersDao()
            mDao.updateLista(listaNew)
        }
    }

    private fun subscribeUiChanges() {
        mCantiViewModel
                .listaPersonalizzataResult!!
                .observe(
                        this,
                        Observer { listaPersonalizzataResult ->
                            Log.d(TAG, "onChanged")
                            mCantiViewModel.posizioniList = listaPersonalizzataResult!!.map {
                                it.withClickListener(click)
                                        .withLongClickListener(longClick)
                                        .withSelectedColor(themeUtils.primaryColorDark())
                                it
                            }
                            cantoAdapter.set(mCantiViewModel.posizioniList)
                        })
    }


    companion object {
        internal val TAG = ListaPersonalizzataFragment::class.java.canonicalName
        const val TAG_INSERT_PERS = 555
    }
}
