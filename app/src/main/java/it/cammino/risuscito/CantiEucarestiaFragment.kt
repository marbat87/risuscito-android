package it.cammino.risuscito

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialcab.MaterialCab
import com.crashlytics.android.Crashlytics
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter
import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.CantiEucarestiaViewModel
import kotlinx.android.synthetic.main.activity_lista_personalizzata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.lista_pers_button.*
import java.sql.Date

class CantiEucarestiaFragment : Fragment(), MaterialCab.Callback {
    private var mCantiViewModel: CantiEucarestiaViewModel? = null
    // create boolean for fetching data
    private var isViewShown = true
    private var posizioneDaCanc: Int = 0
    private var idDaCanc: Int = 0
    private var timestampDaCanc: String? = null
    private var rootView: View? = null
    private var mSwhitchMode: Boolean = false
    private lateinit var posizioniList: MutableList<Pair<PosizioneTitleItem, List<PosizioneItem>>>
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private var cantoAdapter: PosizioneRecyclerAdapter? = null
    private var actionModeOk: Boolean = false
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
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
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
        get() = (activity as MainActivity).themeUtils

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false)

        mMainActivity = activity as MainActivity?

        mLUtils = LUtils.getInstance(activity)
        mSwhitchMode = false

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
            if (v.id == R.id.addCantoGenerico) {
                if (mSwhitchMode) {
                    mSwhitchMode = false
                    actionModeOk = true
                    mMainActivity!!.materialCab!!.finish()
                    Thread(
                            Runnable {
                                scambioConVuoto(
                                        Integer.valueOf(
                                                (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                                        .text
                                                        .toString())!!)
                            })
                            .start()
                } else {
                    if (!mMainActivity!!.materialCab!!.isActive) {
                        val bundle = Bundle()
                        bundle.putInt("fromAdd", 1)
                        bundle.putInt("idLista", 2)
                        bundle.putInt(
                                "position",
                                Integer.valueOf(
                                        (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                                .text
                                                .toString())!!)
                        startSubActivity(bundle)
                    }
                }
            } else {
                if (!mSwhitchMode)
                    if (mMainActivity!!.materialCab!!.isActive) {
                        posizioneDaCanc = Integer.valueOf(
                                (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                        .text
                                        .toString())!!
                        idDaCanc = Integer.valueOf(
                                (v.findViewById<View>(R.id.text_id_canto_card) as TextView)
                                        .text
                                        .toString())!!
                        timestampDaCanc = (v.findViewById<View>(R.id.text_timestamp) as TextView).text.toString()
                        snackBarRimuoviCanto(v)
                    } else
                        openPagina(v)
                else {
                    mSwhitchMode = false
                    actionModeOk = true
                    mMainActivity!!.materialCab!!.finish()
                    Thread(
                            Runnable {
                                scambioCanto(
                                        v,
                                        Integer.valueOf(
                                                (parent.findViewById<View>(R.id.text_id_posizione) as TextView)
                                                        .text
                                                        .toString())!!)
                            })
                            .start()
                }
            }
        }

        val longClick = View.OnLongClickListener { v ->
            val parent = v.parent.parent as View
            posizioneDaCanc = Integer.valueOf(
                    (parent.findViewById<View>(R.id.text_id_posizione) as TextView).text.toString())!!
            idDaCanc = Integer.valueOf(
                    (v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString())!!
            timestampDaCanc = (v.findViewById<View>(R.id.text_timestamp) as TextView).text.toString()
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

        mCantiViewModel = ViewModelProviders.of(this).get(CantiEucarestiaViewModel::class.java)
        populateDb()
        subscribeUiFavorites()

        button_pulisci.setOnClickListener {
            Thread(
                    Runnable {
                        val mDao = RisuscitoDatabase.getInstance(context).customListDao()
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
                Integer.valueOf((v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString())!!)

        val intent = Intent(activity, PaginaRenderActivity::class.java)
        intent.putExtras(bundle)
        mLUtils!!.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER)
    }

    private fun getCantofromPosition(
            posizioni: List<Posizione>?, titoloPosizione: String, position: Int, tag: Int): Pair<PosizioneTitleItem, List<PosizioneItem>> {
        val list = posizioni!!
                .filter { it.position == position }
                .map {
                    PosizioneItem(
                            it.pagina,
                            it.titolo,
                            it.color,
                            it.id,
                            it.source,
                            it.timestamp.time.toString())
                }

        return Pair(
                PosizioneTitleItem(titoloPosizione, 2, position, tag, position == 4 || position == 3),
                list as ArrayList<PosizioneItem>)
    }

    // recupera il titolo del canto in posizione "position" nella lista 2
    private fun getTitoloToSendFromPosition(position: Int): String {

        val result = StringBuilder()

        val items = posizioniList[position].second

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
        if (mMainActivity!!.materialCab!!.isActive) mMainActivity!!.materialCab!!.finish()
        val parent = view.parent.parent as View
        longclickedPos = Integer.valueOf((parent.findViewById<View>(R.id.tag) as TextView).text.toString())!!
        longClickedChild = Integer.valueOf((view.findViewById<View>(R.id.item_tag) as TextView).text.toString())!!
        if (!mMainActivity!!.isOnTablet)
            toolbar_layout!!.setExpanded(true, true)
        mMainActivity!!.materialCab!!.start(this@CantiEucarestiaFragment)
    }

    private fun scambioCanto(v: View, position: Int) {
        //        db = listaCanti.getReadableDatabase();
        val idNew = Integer.valueOf((v.findViewById<View>(R.id.text_id_canto_card) as TextView).text.toString())!!
        val timestampNew = (v.findViewById<View>(R.id.text_timestamp) as TextView).text.toString()
        //        Log.i(getClass().toString(), "positionNew: " + position);
        //        Log.i(getClass().toString(), "idNew: " + idNew);
        //        Log.i(getClass().toString(), "timestampNew: " + timestampNew);
        //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        //        Log.i(getClass().toString(), "idDaCanc: " + idDaCanc);
        //        Log.i(getClass().toString(), "timestampDaCanc: " + timestampDaCanc);
        if (idNew != idDaCanc || posizioneDaCanc != position) {

            val positionToDelete = CustomList()
            positionToDelete.id = 2
            positionToDelete.position = position
            positionToDelete.idCanto = idNew
            val mDao = RisuscitoDatabase.getInstance(context).customListDao()
            mDao.deletePosition(positionToDelete)

            mDao.updatePositionNoTimestamp(idNew, 2, posizioneDaCanc, idDaCanc)

            val positionToInsert = CustomList()
            positionToInsert.id = 2
            positionToInsert.position = position
            positionToInsert.idCanto = idDaCanc
            positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampNew))
            mDao.insertPosition(positionToInsert)

            Snackbar.make(
                    activity!!.findViewById(R.id.main_content),
                    R.string.switch_done,
                    Snackbar.LENGTH_SHORT)
                    .show()
        } else {
            Snackbar.make(rootView!!, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun scambioConVuoto(position: Int) {
        val positionToDelete = CustomList()
        positionToDelete.id = 2
        positionToDelete.position = posizioneDaCanc
        positionToDelete.idCanto = idDaCanc
        val mDao = RisuscitoDatabase.getInstance(context).customListDao()
        mDao.deletePosition(positionToDelete)

        val positionToInsert = CustomList()
        positionToInsert.id = 2
        positionToInsert.position = position
        positionToInsert.idCanto = idDaCanc
        positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc))
        mDao.insertPosition(positionToInsert)

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
        posizioniList[longclickedPos].second!![longClickedChild].setmSelected(true)
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
        Log.d(TAG, "onCabItemClicked: ")
        when (item.itemId) {
            R.id.action_remove_item -> {
                Thread(
                        Runnable {
                            val positionToDelete = CustomList()
                            positionToDelete.id = 2
                            positionToDelete.position = posizioneDaCanc
                            positionToDelete.idCanto = idDaCanc
                            val mDao = RisuscitoDatabase.getInstance(context).customListDao()
                            mDao.deletePosition(positionToDelete)
                        })
                        .start()
                actionModeOk = true
                mMainActivity!!.materialCab!!.finish()
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
                                        positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc))
                                        val mDao = RisuscitoDatabase.getInstance(context).customListDao()
                                        mDao.insertPosition(positionToInsert)
                                    })
                                    .start()
                        }
                        .setActionTextColor(themeUtils.accentColor())
                        .show()
                mSwhitchMode = false
                return true
            }
            R.id.action_switch_item -> {
                mSwhitchMode = true
                mMainActivity!!.materialCab!!.setTitleRes(R.string.switch_started)
                Toast.makeText(
                        activity,
                        resources.getString(R.string.switch_tooltip),
                        Toast.LENGTH_SHORT)
                        .show()
                return true
            }
        }
        return false
    }

    override fun onCabFinished(cab: MaterialCab): Boolean {
        Log.d(TAG, "onCabFinished: ")
        mSwhitchMode = false
        if (!actionModeOk) {
            try {
                posizioniList[longclickedPos].second!![longClickedChild].setmSelected(false)
                cantoAdapter!!.notifyItemChanged(longclickedPos)
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }

        }
        return true
    }

    private fun populateDb() {
        mCantiViewModel!!.createDb()
    }

    private fun subscribeUiFavorites() {
        mCantiViewModel!!
                .cantiEucarestiaResult
                .observe(
                        this,
                        Observer { canti ->
                            posizioniList.clear()
                            var progressiveTag = 0
                            val pref = PreferenceManager.getDefaultSharedPreferences(activity)

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

                            cantoAdapter!!.notifyDataSetChanged()
                        })
    }

    companion object {

        private val TAG_INSERT_EUCARESTIA = 444
        private val TAG = CantiEucarestiaFragment::class.java.canonicalName
    }
}
