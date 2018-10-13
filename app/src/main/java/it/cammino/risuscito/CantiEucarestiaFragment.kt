package it.cammino.risuscito

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.crashlytics.android.Crashlytics
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.iconics.IconicsDrawable
import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.DefaultListaViewModel
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.lista_pers_button.*
import java.sql.Date

class CantiEucarestiaFragment : Fragment() {
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
    //    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

    private val defaultIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, titlesList)
            intent.type = "text/plain"
            return intent
        }

    private val titlesList: String
        get() {

            var progressivePos = 0

            val l = ThemeableActivity.getSystemLocalWrapper(activity!!.resources.configuration)
            val result = StringBuilder()
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

            return result.toString()
        }

    private val themeUtils: ThemeUtils
        get() = (activity as MainActivity).themeUtils!!

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return@OnClickListener
        mLastClickTime = SystemClock.elapsedRealtime()
        val parent = v.parent.parent as View
        if (v.id == R.id.addCantoGenerico) {
            if (mSwhitchMode) {
//                actionModeOk = true
                MaterialCab.destroy()
                Thread(
                        Runnable {
                            scambioConVuoto(
                                    Integer.valueOf(
                                            (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                                    .text
                                                    .toString()))
                        })
                        .start()
            } else {
                if (!MaterialCab.isActive) {
                    val bundle = Bundle()
                    bundle.putInt("fromAdd", 1)
                    bundle.putInt("idLista", 2)
                    bundle.putInt(
                            "position",
                            Integer.valueOf(
                                    (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                            .text
                                            .toString()))
                    startSubActivity(bundle)
                }
            }
        } else {
            if (!mSwhitchMode)
                if (MaterialCab.isActive) {
                    posizioneDaCanc = Integer.valueOf(
                            (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                    .text
                                    .toString())
                    idDaCanc = Integer.valueOf(
                            (v.findViewById<View>(R.id.text_id_canto_card) as TextView)
                                    .text
                                    .toString())
                    timestampDaCanc = (v.findViewById<View>(R.id.text_timestamp) as TextView).text.toString()
                    snackBarRimuoviCanto(v)
                } else
                    openPagina(v)
            else {
//                actionModeOk = true
                MaterialCab.destroy()
                Thread(
                        Runnable {
                            scambioCanto(
                                    v,
                                    Integer.valueOf(
                                            (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                                    .text
                                                    .toString()))
                        })
                        .start()
            }
        }
    }

    private val longClick = View.OnLongClickListener { v ->
        val parent = v.parent.parent as View
        posizioneDaCanc = Integer.valueOf(
                (parent.findViewById<View>(R.id.text_id_posizione) as TextView).text.toString())
        idDaCanc = Integer.valueOf(
                (v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString())
        timestampDaCanc = (v.findViewById<View>(R.id.text_timestamp) as TextView).text.toString()
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
            Thread(
                    Runnable {
                        val mDao = RisuscitoDatabase.getInstance(context!!).customListDao()
                        mDao.deleteListById(2)
                    })
                    .start()
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
                val fab1 = (parentFragment as CustomLists).getFab()
                fab1.show()
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
        parentFragment!!.startActivityForResult(intent, TAG_INSERT_EUCARESTIA)
        activity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on)
    }

    private fun openPagina(v: View) {
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
                            it.timestamp!!.time.toString())
                }

        return ListaPersonalizzataItem()
                .withTitleItem(PosizioneTitleItem(
                        titoloPosizione,
                        1,
                        position,
                        tag,
                        position == 4 || position == 3))
                .withListItem(list)
                .withClickListener(click)
                .withLongClickListener(longClick)
                .withSelectedColor(themeUtils.primaryColorDark())
                .withId(position)
    }

    // recupera il titolo del canto in posizione "position" nella lista 2
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

    private fun scambioCanto(v: View, position: Int) {
        val idNew = Integer.valueOf((v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString())
        val timestampNew = (v.findViewById<View>(R.id.text_timestamp) as TextView).text.toString()
        //        Log.i(getClass().toString(), "positionNew: " + position);
        //        Log.i(getClass().toString(), "idNew: " + idNew);
        //        Log.i(getClass().toString(), "timestampNew: " + timestampNew);
        //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        //        Log.i(getClass().toString(), "idDaCanc: " + idDaCanc);
        //        Log.i(getClass().toString(), "timestampDaCanc: " + timestampDaCanc);
        if (idNew != idDaCanc || posizioneDaCanc != position) {
            val roomDb = RisuscitoDatabase.getInstance(context!!)
            roomDb.beginTransaction()

            try {
                val positionToDelete = CustomList()
                positionToDelete.id = 2
                positionToDelete.position = position
                positionToDelete.idCanto = idNew
                val mDao = RisuscitoDatabase.getInstance(context!!).customListDao()
                mDao.deletePosition(positionToDelete)

                mDao.updatePositionNoTimestamp(idNew, 2, posizioneDaCanc, idDaCanc)

                val positionToInsert = CustomList()
                positionToInsert.id = 2
                positionToInsert.position = position
                positionToInsert.idCanto = idDaCanc
                positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampNew))
                mDao.insertPosition(positionToInsert)

                roomDb.setTransactionSuccessful()
                Snackbar.make(
                        activity!!.findViewById(R.id.main_content),
                        R.string.switch_done,
                        Snackbar.LENGTH_SHORT)
                        .show()
            } catch (e: SQLiteConstraintException) {
                Snackbar.make(rootView!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
            } finally {
                roomDb.endTransaction()
            }

        } else {
            Snackbar.make(rootView!!, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun scambioConVuoto(position: Int) {
        val roomDb = RisuscitoDatabase.getInstance(context!!)
        roomDb.beginTransaction()

        try {
            val positionToDelete = CustomList()
            positionToDelete.id = 2
            positionToDelete.position = posizioneDaCanc
            positionToDelete.idCanto = idDaCanc
            val mDao = RisuscitoDatabase.getInstance(context!!).customListDao()
            mDao.deletePosition(positionToDelete)

            val positionToInsert = CustomList()
            positionToInsert.id = 2
            positionToInsert.position = position
            positionToInsert.idCanto = idDaCanc
            positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc!!))
            mDao.insertPosition(positionToInsert)

            roomDb.setTransactionSuccessful()
            Snackbar.make(
                    activity!!.findViewById(R.id.main_content),
                    R.string.switch_done,
                    Snackbar.LENGTH_SHORT)
                    .show()
        } catch (e: SQLiteConstraintException) {
            Snackbar.make(rootView!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
        } finally {
            roomDb.endTransaction()
        }
    }

    private fun startCab(switchMode: Boolean) {
        mSwhitchMode = switchMode
        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
            if (switchMode)
                titleRes(R.string.switch_started)
            else
                title = ""
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
                        .colorRes(android.R.color.white)
                menu.findItem(R.id.action_remove_item).icon = IconicsDrawable(activity!!, CommunityMaterial.Icon.cmd_delete)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(android.R.color.white)
//                actionModeOk = false
            }

            onSelection { item ->
                Log.d(TAG, "MaterialCab onSelection")
                when (item.itemId) {
                    R.id.action_remove_item -> {
                        Thread(
                                Runnable {
                                    val positionToDelete = CustomList()
                                    positionToDelete.id = 2
                                    positionToDelete.position = posizioneDaCanc
                                    positionToDelete.idCanto = idDaCanc
                                    val mDao = RisuscitoDatabase.getInstance(context!!).customListDao()
                                    mDao.deletePosition(positionToDelete)
                                })
                                .start()
//                        actionModeOk = true
                        MaterialCab.destroy()
                        Snackbar.make(
                                activity!!.findViewById(R.id.main_content),
                                R.string.song_removed,
                                Snackbar.LENGTH_LONG)
                                .setAction(
                                        getString(android.R.string.cancel).toUpperCase()
                                ) {
                                    Thread(
                                            Runnable {
                                                val positionToInsert = CustomList()
                                                positionToInsert.id = 2
                                                positionToInsert.position = posizioneDaCanc
                                                positionToInsert.idCanto = idDaCanc
                                                positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc!!))
                                                val mDao = RisuscitoDatabase.getInstance(context!!).customListDao()
                                                mDao.insertPosition(positionToInsert)
                                            })
                                            .start()
                                }
                                .setActionTextColor(themeUtils.accentColor())
                                .show()
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
//                if (!actionModeOk) {
                try {
                    posizioniList[longclickedPos].listItem!![longClickedChild].setmSelected(false)
                    cantoAdapter!!.notifyItemChanged(longclickedPos)
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }
//                }
                true
            }
        }
    }

    private fun populateDb() {
        mCantiViewModel!!.defaultListaId = 2
        mCantiViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mCantiViewModel!!
                .cantiResult!!
                .observe(
                        this,
                        Observer { canti ->
                            posizioniList.clear()
                            var progressiveTag = 0
                            val pref = PreferenceManager.getDefaultSharedPreferences(context)

                            posizioniList.add(
                                    getCantofromPosition(
                                            canti, getString(R.string.canto_iniziale), 1, progressiveTag++))

                            if (pref.getBoolean(Utility.SHOW_SECONDA, false))
                                posizioniList.add(
                                        getCantofromPosition(
                                                canti, getString(R.string.seconda_lettura), 6, progressiveTag++))

                            posizioniList.add(
                                    getCantofromPosition(
                                            canti, getString(R.string.canto_pace), 2, progressiveTag++))

                            if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false))
                                posizioniList.add(
                                        getCantofromPosition(
                                                canti, getString(R.string.canto_offertorio), 8, progressiveTag++))

                            if (pref.getBoolean(Utility.SHOW_SANTO, false))
                                posizioniList.add(
                                        getCantofromPosition(canti, getString(R.string.santo), 7, progressiveTag++))

                            posizioniList.add(
                                    getCantofromPosition(
                                            canti, getString(R.string.canto_pane), 3, progressiveTag++))
                            posizioniList.add(
                                    getCantofromPosition(
                                            canti, getString(R.string.canto_vino), 4, progressiveTag++))
                            posizioniList.add(
                                    getCantofromPosition(canti, getString(R.string.canto_fine), 5, progressiveTag))

                            cantoAdapter!!.set(posizioniList)
                        })
    }

    companion object {

        private const val TAG_INSERT_EUCARESTIA = 444
        private val TAG = CantiEucarestiaFragment::class.java.canonicalName
    }
}
