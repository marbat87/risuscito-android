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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialcab.MaterialCab.Companion.destroy
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
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.lista_pers_button.*

class ListaPredefinitaFragment : Fragment() {

    private lateinit var mCantiViewModel: DefaultListaViewModel
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
            var progressivePos = 0

            when (mCantiViewModel.defaultListaId) {
                1 -> {
                    result
                            .append("-- ")
                            .append(getString(R.string.title_activity_canti_parola).toUpperCase(l))
                            .append(" --\n")

                    result.append(resources.getString(R.string.canto_iniziale).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.prima_lettura).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.seconda_lettura).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.terza_lettura).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    val pref = PreferenceManager.getDefaultSharedPreferences(context)

                    if (pref.getBoolean(Utility.SHOW_PACE, false)) {
                        result.append(resources.getString(R.string.canto_pace).toUpperCase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_fine).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos))
                }
                2 -> {
                    result
                            .append("-- ")
                            .append(getString(R.string.title_activity_canti_eucarestia).toUpperCase(l))
                            .append(" --")
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_iniziale).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    val pref = PreferenceManager.getDefaultSharedPreferences(context)
                    if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
                        result.append(resources.getString(R.string.seconda_lettura).toUpperCase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_pace).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
                        result.append(resources.getString(R.string.canto_offertorio).toUpperCase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
                        result.append(resources.getString(R.string.santo).toUpperCase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_pane).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_vino).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_fine).toUpperCase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos))
                }
            }

            return result.toString()
        }

    private val themeUtils: ThemeUtils
        get() {
            return (activity as MainActivity).themeUtils
        }

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener
        mLastClickTime = SystemClock.elapsedRealtime()
        val parent = v.parent.parent as View
        if (v.id == R.id.addCantoGenerico) {
            if (mSwhitchMode) {
                destroy()
                ListeUtils.scambioConVuoto(this, mCantiViewModel.defaultListaId, posizioneDaCanc, idDaCanc, Integer.valueOf(parent.text_id_posizione.text.toString()))
            } else {
                if (!MaterialCab.isActive) {
                    val intent = Intent(activity, InsertActivity::class.java)
                    intent.putExtras(bundleOf("fromAdd" to 1,
                            "idLista" to mCantiViewModel.defaultListaId,
                            "position" to Integer.valueOf(parent.text_id_posizione.text.toString())))
                    parentFragment!!.startActivityForResult(intent, when (mCantiViewModel.defaultListaId) {
                        1 -> TAG_INSERT_PAROLA
                        2 -> TAG_INSERT_EUCARESTIA
                        else -> TAG_INSERT_PAROLA
                    })
                    Animatoo.animateShrink(activity)
                }
            }
        } else {
            if (!mSwhitchMode)
                if (MaterialCab.isActive) {
                    posizioneDaCanc = Integer.valueOf(parent.text_id_posizione.text.toString())
                    idDaCanc = Integer.valueOf(v.text_id_canto_card.text.toString())
                    timestampDaCanc = v.text_timestamp.text.toString()
                    snackBarRimuoviCanto(v)
                } else {
                    //apri canto
                    val intent = Intent(activity, PaginaRenderActivity::class.java)
                    intent.putExtras(bundleOf("pagina" to v.text_source_canto.text.toString(),
                            "idCanto" to Integer.valueOf(v.text_id_canto_card.text.toString())))
                    mLUtils!!.startActivityWithTransition(intent)
                }
            else {
                destroy()
                ListeUtils.scambioCanto(this,
                        mCantiViewModel.defaultListaId,
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

        val args = Bundle().apply { putInt("tipoLista", arguments!!.getInt("indiceLista", 0)) }
        mCantiViewModel = ViewModelProviders.of(this, ViewModelWithArgumentsFactory(activity!!.application, args)).get(DefaultListaViewModel::class.java)

        mMainActivity = activity as MainActivity?

        mLUtils = LUtils.getInstance(activity!!)
        mSwhitchMode = false

        if (!isViewShown) {
            destroy()
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

        subscribeUiFavorites()

        button_pulisci.setOnClickListener {
            ListeUtils.cleanList(context!!, mCantiViewModel.defaultListaId)
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
                destroy()
                (parentFragment as CustomLists).initFabOptions(false)

            } else
                isViewShown = false
        }
    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    private fun getCantofromPosition(
            posizioni: List<Posizione>?, titoloPosizione: String, position: Int, tag: Int): ListaPersonalizzataItem {
        val list = posizioni!!
                .filter { it.position == position }
                .map {
                    PosizioneItem()
                            .withTitle(LUtils.getResId(it.titolo, R.string::class.java))
                            .withPage(LUtils.getResId(it.pagina, R.string::class.java))
                            .withSource(LUtils.getResId(it.source, R.string::class.java))
                            .withColor(it.color!!)
                            .withId(it.id)
                            .withTimestamp(it.timestamp!!.time.toString())
                }

        return ListaPersonalizzataItem()
                .withTitleItem(PosizioneTitleItem(
                        titoloPosizione,
                        position,
                        tag,
                        when (mCantiViewModel.defaultListaId) {
                            2 -> (position == 4 || position == 3)
                            else -> false
                        }))
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

        return result.toString()
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
                        destroy()
                        ListeUtils.removePositionWithUndo(this@ListaPredefinitaFragment, mCantiViewModel.defaultListaId, posizioneDaCanc, idDaCanc, timestampDaCanc!!)
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

    private fun subscribeUiFavorites() {
        mCantiViewModel
                .cantiResult!!
                .observe(
                        this,
                        Observer<List<Posizione>> { mCanti ->
                            var progressiveTag = 0
                            val pref = PreferenceManager.getDefaultSharedPreferences(context)
                            posizioniList.clear()

                            when (mCantiViewModel.defaultListaId) {
                                1 -> {
                                    posizioniList.add(
                                            getCantofromPosition(mCanti, getString(R.string.canto_iniziale), 1, progressiveTag++))
                                    posizioniList.add(
                                            getCantofromPosition(mCanti, getString(R.string.prima_lettura), 2, progressiveTag++))
                                    posizioniList.add(
                                            getCantofromPosition(mCanti, getString(R.string.seconda_lettura), 3, progressiveTag++))
                                    posizioniList.add(
                                            getCantofromPosition(mCanti, getString(R.string.terza_lettura), 4, progressiveTag++))

                                    if (pref.getBoolean(Utility.SHOW_PACE, false))
                                        posizioniList.add(
                                                getCantofromPosition(mCanti, getString(R.string.canto_pace), 6, progressiveTag++))

                                    posizioniList.add(
                                            getCantofromPosition(mCanti, getString(R.string.canto_fine), 5, progressiveTag))
                                }
                                2 -> {
                                    posizioniList.add(
                                            getCantofromPosition(
                                                    mCanti, getString(R.string.canto_iniziale), 1, progressiveTag++))

                                    if (pref.getBoolean(Utility.SHOW_SECONDA, false))
                                        posizioniList.add(
                                                getCantofromPosition(
                                                        mCanti, getString(R.string.seconda_lettura), 6, progressiveTag++))

                                    posizioniList.add(
                                            getCantofromPosition(
                                                    mCanti, getString(R.string.canto_pace), 2, progressiveTag++))

                                    if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false))
                                        posizioniList.add(
                                                getCantofromPosition(
                                                        mCanti, getString(R.string.canto_offertorio), 8, progressiveTag++))

                                    if (pref.getBoolean(Utility.SHOW_SANTO, false))
                                        posizioniList.add(
                                                getCantofromPosition(mCanti, getString(R.string.santo), 7, progressiveTag++))

                                    posizioniList.add(
                                            getCantofromPosition(
                                                    mCanti, getString(R.string.canto_pane), 3, progressiveTag++))
                                    posizioniList.add(
                                            getCantofromPosition(
                                                    mCanti, getString(R.string.canto_vino), 4, progressiveTag++))
                                    posizioniList.add(
                                            getCantofromPosition(mCanti, getString(R.string.canto_fine), 5, progressiveTag))
                                }
                            }
                            cantoAdapter!!.set(posizioniList)
                        })
    }

    companion object {
        const val TAG_INSERT_PAROLA = 333
        const val TAG_INSERT_EUCARESTIA = 444
        private val TAG = ListaPredefinitaFragment::class.java.canonicalName

        fun newInstance(indiceLista: Int): ListaPredefinitaFragment {
            val f = ListaPredefinitaFragment()
            f.arguments = bundleOf("indiceLista" to indiceLista)
            return f
        }
    }
}
