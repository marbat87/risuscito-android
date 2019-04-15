package it.cammino.risuscito

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.colorInt
import com.mikepenz.iconics.paddingDp
import com.mikepenz.iconics.sizeDp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.DefaultListaViewModel
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.lista_pers_button.*

class CantiParolaFragment : Fragment() {

    private var mCantiViewModel: DefaultListaViewModel? = null
    // create boolean for fetching data
    private var isViewShown = true
    private var posizioneDaCanc: Int = 0
    private var idDaCanc: Int = 0
    private var timestampDaCanc: String? = null
    private var rootView: View? = null
    private var mSwhitchMode: Boolean = false
    private var posizioniList: ArrayList<ListaPersonalizzataItem> = ArrayList()
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private var cantoAdapter: FastItemAdapter<ListaPersonalizzataItem>? = null
    private var mMainActivity: MainActivity? = null
    private var mLastClickTime: Long = 0
    private var mLUtils: LUtils? = null

    private val defaultIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, titlesList)
            intent.type = "text/plain"
            return intent
        }

    private val titlesList: String
        get() {

            val l = ThemeableActivity.getSystemLocalWrapper(activity!!.resources.configuration)
            val result = StringBuilder()
            result
                    .append("-- ")
                    .append(getString(R.string.title_activity_canti_parola).toUpperCase(l))
                    .append(" --\n")

            result.append(resources.getString(R.string.canto_iniziale).toUpperCase(l))
            result.append("\n")

            result.append(getTitoloToSendFromPosition(0))
            result.append("\n")
            result.append(resources.getString(R.string.prima_lettura).toUpperCase(l))
            result.append("\n")

            result.append(getTitoloToSendFromPosition(1))
            result.append("\n")
            result.append(resources.getString(R.string.seconda_lettura).toUpperCase(l))
            result.append("\n")

            result.append(getTitoloToSendFromPosition(2))
            result.append("\n")
            result.append(resources.getString(R.string.terza_lettura).toUpperCase(l))
            result.append("\n")

            result.append(getTitoloToSendFromPosition(3))
            result.append("\n")
            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            if (pref.getBoolean(Utility.SHOW_PACE, false)) {
                result.append(resources.getString(R.string.canto_pace).toUpperCase(l))
                result.append("\n")

                result.append(getTitoloToSendFromPosition(4))
                result.append("\n")
                result.append(resources.getString(R.string.canto_fine).toUpperCase(l))
                result.append("\n")

                result.append(getTitoloToSendFromPosition(5))
            } else {
                result.append(resources.getString(R.string.canto_fine).toUpperCase(l))
                result.append("\n")

                result.append(getTitoloToSendFromPosition(4))
            }

            return result.toString()
        }

    private val themeUtils: ThemeUtils
        get() {
            return (activity as MainActivity).themeUtils!!
        }

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener
        mLastClickTime = SystemClock.elapsedRealtime()
        val parent = v.parent.parent as View
        if (parent.addCantoGenerico.isVisible) {
            if (mSwhitchMode) {
                MaterialCab.destroy()
                ListeUtils.scambioConVuoto(this@CantiParolaFragment, 1, posizioneDaCanc, idDaCanc, Integer.valueOf(parent.text_id_posizione.text.toString()))
            } else {
                if (!MaterialCab.isActive) {
                    val bundle = Bundle()
                    bundle.putInt("fromAdd", 1)
                    bundle.putInt("idLista", 1)
                    bundle.putInt(
                            "position",
                            Integer.valueOf(parent.text_id_posizione.text.toString()))
                    startSubActivity(bundle)
                }
            }
        } else {
            if (!mSwhitchMode)
                if (MaterialCab.isActive) {
                    posizioneDaCanc = Integer.valueOf(parent.text_id_posizione.text.toString())
                    idDaCanc = Integer.valueOf(v.text_id_canto_card.text.toString())
                    timestampDaCanc = v.text_timestamp.text.toString()
                    snackBarRimuoviCanto(v)
                } else
                    openPagina(v)
            else {
                MaterialCab.destroy()
                ListeUtils.scambioCanto(this@CantiParolaFragment,
                        1,
                        posizioneDaCanc,
                        idDaCanc,
                        Integer.valueOf(parent.text_id_posizione.text.toString()),
                        Integer.valueOf((v.text_id_canto_card).text.toString())
                )
            }
        }
    }

    private val longClick = OnLongClickListener { v ->
        val parent = v.parent.parent as View
        posizioneDaCanc = Integer.valueOf(parent.text_id_posizione.text.toString())
        idDaCanc = Integer.valueOf(v.text_id_canto_card.text.toString())
        timestampDaCanc = v.text_timestamp.text.toString()
        snackBarRimuoviCanto(v)
        true
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false)

        mCantiViewModel = ViewModelProviders.of(this).get(DefaultListaViewModel::class.java)

        mMainActivity = activity as MainActivity?

        mLUtils = LUtils.getInstance(activity!!)
        mSwhitchMode = false

        if (!isViewShown) {
            if (MaterialCab.isActive) MaterialCab.destroy()
            val fab1 = (parentFragment as CustomLists).getFab()
            fab1.show()
            (parentFragment as CustomLists).initFabOptions(false)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Creating new adapter object
        cantoAdapter = FastItemAdapter()
        cantoAdapter!!.setHasStableIds(true)
        cantoAdapter!!.set(posizioniList)
        recycler_list!!.adapter = cantoAdapter

        // Setting the layoutManager
        recycler_list!!.layoutManager = LinearLayoutManager(activity)

        populateDb()
        subscribeUiFavorites()

        button_pulisci.setOnClickListener {
            ListeUtils.cleanList(context!!, 1)
        }

        button_condividi.setOnClickListener {
            val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, defaultIntent)
            bottomSheetDialog.show(fragmentManager!!, null)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (view != null) {
                isViewShown = true
                if (MaterialCab.isActive) MaterialCab.destroy()
                (parentFragment as CustomLists).initFabOptions(false)

            } else
                isViewShown = false
        }
    }

    override fun onDestroy() {
        if (MaterialCab.isActive) MaterialCab.destroy()
        super.onDestroy()
    }

    private fun startSubActivity(bundle: Bundle) {
        val intent = Intent(activity, GeneralInsertSearch::class.java)
        intent.putExtras(bundle)
        parentFragment!!.startActivityForResult(intent, TAG_INSERT_PAROLA)
        Animatoo.animateShrink(activity)
    }

    private fun openPagina(v: View) {
        // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da
        // visualizzare
        val bundle = Bundle()
        bundle.putString("pagina", v.text_source_canto.text.toString())
        bundle.putInt("idCanto", Integer.valueOf(v.text_id_canto_card.text.toString()))
        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent)
    }

    private fun getCantofromPosition(
            posizioni: List<Posizione>?, titoloPosizione: String, position: Int, tag: Int): ListaPersonalizzataItem {
        val list = posizioni!!
                .filter { it.position == position }
                .map {
                    PosizioneItem(
                            resources.getString(LUtils.getResId(it.pagina!!, R.string::class.java)),
                            resources.getString(LUtils.getResId(it.titolo!!, R.string::class.java)),
                            it.color!!,
                            it.id,
                            resources.getString(LUtils.getResId(it.source!!, R.string::class.java)),
                            (it.timestamp!!.time).toString())
                }

        return ListaPersonalizzataItem()
                .withTitleItem(PosizioneTitleItem(
                        titoloPosizione,
                        1,
                        position,
                        tag,
                        false))
                .withListItem(list)
                .withClickListener(click)
                .withLongClickListener(longClick)
                .withSelectedColor(themeUtils.primaryColorDark())
                .withId(tag)
    }

    // recupera il titolo del canto in posizione "position" nella lista "list"
    private fun getTitoloToSendFromPosition(position: Int): String {
        val result = StringBuilder()

        val items = posizioniList[position].listItem

        if (items!!.isNotEmpty()) {
            for (tempItem in items) {
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

        return result.toString()
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

            onCreate { _, menu ->
                Log.d(TAG, "MaterialCab onCreate")
                posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(true)
                cantoAdapter!!.notifyItemChanged(longclickedPos)
                menu.findItem(R.id.action_switch_item).icon = IconicsDrawable(activity!!, CommunityMaterial.Icon2.cmd_shuffle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorInt(Color.WHITE)
                menu.findItem(R.id.action_remove_item).icon = IconicsDrawable(activity!!, CommunityMaterial.Icon.cmd_delete)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorInt(Color.WHITE)
            }

            onSelection { item ->
                Log.d(TAG, "MaterialCab onSelection")
                when (item.itemId) {
                    R.id.action_remove_item -> {
                        MaterialCab.destroy()
                        ListeUtils.removePositionWithUndo(this@CantiParolaFragment, 1, posizioneDaCanc, idDaCanc, timestampDaCanc!!)
                        true
                    }
                    R.id.action_switch_item -> {
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
                //                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                mSwhitchMode = false
                try {
                    posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(false)
                    cantoAdapter!!.notifyItemChanged(longclickedPos)
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }
                true
            }
        }
    }

    private fun populateDb() {
        mCantiViewModel!!.defaultListaId = 1
        mCantiViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mCantiViewModel!!
                .cantiResult!!
                .observe(
                        this,
                        Observer<List<Posizione>> { mCanti ->
                            Log.d(TAG, "onChanged")
                            var progressiveTag = 0
                            posizioniList.clear()
                            posizioniList.add(
                                    getCantofromPosition(mCanti, getString(R.string.canto_iniziale), 1, progressiveTag++))
                            posizioniList.add(
                                    getCantofromPosition(mCanti, getString(R.string.prima_lettura), 2, progressiveTag++))
                            posizioniList.add(
                                    getCantofromPosition(mCanti, getString(R.string.seconda_lettura), 3, progressiveTag++))
                            posizioniList.add(
                                    getCantofromPosition(mCanti, getString(R.string.terza_lettura), 4, progressiveTag++))

                            val pref = PreferenceManager.getDefaultSharedPreferences(context)
                            if (pref.getBoolean(Utility.SHOW_PACE, false))
                                posizioniList.add(
                                        getCantofromPosition(mCanti, getString(R.string.canto_pace), 6, progressiveTag++))

                            posizioniList.add(
                                    getCantofromPosition(mCanti, getString(R.string.canto_fine), 5, progressiveTag))

                            cantoAdapter!!.set(posizioniList)
                        })
    }

    companion object {
        const val TAG_INSERT_PAROLA = 333
        private val TAG = CantiParolaFragment::class.java.canonicalName
    }
}
