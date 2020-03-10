package it.cammino.risuscito

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialcab.MaterialCab.Companion.destroy
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.database.pojo.Posizione
import it.cammino.risuscito.databinding.ActivityListaPersonalizzataBinding
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.items.listaPersonalizzataItem
import it.cammino.risuscito.items.posizioneTitleItem
import it.cammino.risuscito.objects.posizioneItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.ThemeUtils.Companion.isDarkMode
import it.cammino.risuscito.utils.themeColor
import it.cammino.risuscito.viewmodels.DefaultListaViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory

class ListaPredefinitaFragment : Fragment() {

    private val mCantiViewModel: DefaultListaViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }

    private var posizioneDaCanc: Int = 0
    private var idDaCanc: Int = 0
    private var timestampDaCanc: String? = null
    private var mSwhitchMode: Boolean = false
    private val posizioniList: ArrayList<ListaPersonalizzataItem> = ArrayList()
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private val cantoAdapter: FastItemAdapter<ListaPersonalizzataItem> = FastItemAdapter()
    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLastClickTime: Long = 0
    private var mLUtils: LUtils? = null

    private var _binding: ActivityListaPersonalizzataBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityListaPersonalizzataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity = activity as? MainActivity

        mLUtils = LUtils.getInstance(requireActivity())
        mSwhitchMode = false

        // Creating new adapter object
        cantoAdapter.setHasStableIds(true)
        cantoAdapter.set(posizioniList)
        binding.recyclerList.adapter = cantoAdapter

        // Setting the layoutManager
        binding.recyclerList.layoutManager = LinearLayoutManager(activity)

        subscribeUiFavorites()

        binding.buttonPulisci.setOnClickListener {
            ListeUtils.cleanList(requireContext(), mCantiViewModel.defaultListaId)
        }

        binding.buttonCondividi.setOnClickListener {
            val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, defaultIntent)
            bottomSheetDialog.show(parentFragmentManager, null)
        }
    }

    private fun getCantofromPosition(
            posizioni: List<Posizione>, title: String, position: Int, tag: Int): ListaPersonalizzataItem {
        return listaPersonalizzataItem {
            posizioneTitleItem {
                titoloPosizione = title
                idPosizione = position
                tagPosizione = tag
                isMultiple = if (mCantiViewModel.defaultListaId == 2)
                    (position == 4 || position == 3)
                else
                    false
            }
            listItem = posizioni.filter { it.position == position }.map {
                posizioneItem {
                    withTitle(LUtils.getResId(it.titolo, R.string::class.java))
                    withPage(LUtils.getResId(it.pagina, R.string::class.java))
                    withSource(LUtils.getResId(it.source, R.string::class.java))
                    withColor(it.color ?: Canto.BIANCO)
                    withId(it.id)
                    withTimestamp(it.timestamp?.time.toString())
                }
            }
            createClickListener = click
            createLongClickListener = longClick
            id = tag
        }
    }

    // recupera il titolo del canto in posizione "position" nella lista "list"
    private fun getTitoloToSendFromPosition(position: Int): String {
        val result = StringBuilder()

        val items = posizioniList[position].listItem

        if (!items.isNullOrEmpty()) {
            for (tempItem in items) {
                result
                        .append(tempItem.title?.getText(requireContext()))
                        .append(" - ")
                        .append(getString(R.string.page_contracted))
                        .append(tempItem.page?.getText(requireContext()))
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
        val parent = view.parent.parent as? View
        longclickedPos = Integer.valueOf(parent?.findViewById<TextView>(R.id.generic_tag)?.text.toString())
        longClickedChild = Integer.valueOf(view.findViewById<TextView>(R.id.item_tag).text.toString())
        if (mMainActivity?.isOnTablet != true)
            mMainActivity?.expandToolbar()
        startCab(false)
    }

    private fun startCab(switchMode: Boolean) {
        mMainActivity?.let {
            mSwhitchMode = switchMode
            MaterialCab.attach(it, R.id.cab_stub) {
                if (switchMode)
                    titleRes(R.string.switch_started)
                else
                    title = resources.getQuantityString(R.plurals.item_selected, 1, 1)
                popupTheme = R.style.ThemeOverlay_MaterialComponents_Dark_ActionBar
                contentInsetStartRes(R.dimen.mcab_default_content_inset)
                if (isDarkMode(requireContext()))
                    backgroundColor = requireContext().themeColor(R.attr.colorSurface)
                menuRes = R.menu.menu_actionmode_lists

                onCreate { _, menu ->
                    Log.d(TAG, "MaterialCab onCreate")
                    posizioniList[longclickedPos].listItem?.get(longClickedChild)?.setmSelected(true)
                    cantoAdapter.notifyItemChanged(longclickedPos)
                    menu.findItem(R.id.action_switch_item).icon = IconicsDrawable(requireContext(), CommunityMaterial.Icon2.cmd_shuffle).apply {
                        colorInt = Color.WHITE
                        sizeDp = 24
                        paddingDp = 2
                    }
                    menu.findItem(R.id.action_remove_item).icon = IconicsDrawable(requireContext(), CommunityMaterial.Icon.cmd_delete).apply {
                        colorInt = Color.WHITE
                        sizeDp = 24
                        paddingDp = 2
                    }
                    actionModeOk = false
                }

                onSelection { item ->
                    Log.d(TAG, "MaterialCab onSelection")
                    when (item.itemId) {
                        R.id.action_remove_item -> {
                            actionModeOk = true
                            destroy()
                            ListeUtils.removePositionWithUndo(this@ListaPredefinitaFragment, mCantiViewModel.defaultListaId, posizioneDaCanc, idDaCanc, timestampDaCanc
                                    ?: "")
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
                    Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                    Log.d(TAG, "MaterialCab onDestroy - longclickedPos: $longclickedPos / defaultListaId: ${mCantiViewModel.defaultListaId}")
                    mSwhitchMode = false
                    if (!actionModeOk) {
                        try {
                            posizioniList[longclickedPos].listItem?.get(longClickedChild)?.setmSelected(false)
                            cantoAdapter.notifyItemChanged(longclickedPos)
                        } catch (e: Exception) {
                            Crashlytics.logException(e)
                        }
                    }
                    true
                }
            }
        }
    }

    private fun subscribeUiFavorites() {
        mCantiViewModel.cantiResult?.observe(viewLifecycleOwner) { mCanti ->
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
            cantoAdapter.set(posizioniList)
        }
    }

    private val defaultIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, titlesList)
            intent.type = "text/plain"
            return intent
        }

    private val titlesList: String
        get() {
            val l = getSystemLocale(resources)
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

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
            mLastClickTime = SystemClock.elapsedRealtime()
            val parent = v.parent.parent as? View
            if (v.id == R.id.add_canto_generico) {
                if (mSwhitchMode) {
                    actionModeOk = true
                    destroy()
                    ListeUtils.scambioConVuoto(this, mCantiViewModel.defaultListaId, posizioneDaCanc, idDaCanc, Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString()))
                } else {
                    if (!MaterialCab.isActive) {
                        val intent = Intent(activity, InsertActivity::class.java)
                        intent.putExtras(bundleOf(InsertActivity.FROM_ADD to 1,
                                InsertActivity.ID_LISTA to mCantiViewModel.defaultListaId,
                                InsertActivity.POSITION to Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())))
                        parentFragment?.startActivityForResult(intent, when (mCantiViewModel.defaultListaId) {
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
                        posizioneDaCanc = Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
                        idDaCanc = Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
                        timestampDaCanc = v.findViewById<TextView>(R.id.text_timestamp).text.toString()
                        snackBarRimuoviCanto(v)
                    } else {
                        //apri canto
                        val intent = Intent(activity, PaginaRenderActivity::class.java)
                        intent.putExtras(bundleOf(Utility.PAGINA to v.findViewById<TextView>(R.id.text_source_canto).text.toString(),
                                Utility.ID_CANTO to Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())))
                        mLUtils?.startActivityWithTransition(intent)
                    }
                else {
                    actionModeOk = true
                    destroy()
                    ListeUtils.scambioCanto(this,
                            mCantiViewModel.defaultListaId,
                            posizioneDaCanc,
                            idDaCanc,
                            Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString()),
                            Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
                    )
                }
            }
        }
    }

    private val longClick = OnLongClickListener { v ->
        val parent = v.parent.parent as? View
        posizioneDaCanc = Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
        idDaCanc = Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
        timestampDaCanc = v.findViewById<TextView>(R.id.text_timestamp).text.toString()
        snackBarRimuoviCanto(v)
        true
    }

    companion object {
        const val TAG_INSERT_PAROLA = 333
        const val TAG_INSERT_EUCARESTIA = 444
        private const val INDICE_LISTA = "indiceLista"
        private val TAG = ListaPredefinitaFragment::class.java.canonicalName

        fun newInstance(indiceLista: Int): ListaPredefinitaFragment {
            val f = ListaPredefinitaFragment()
            f.arguments = bundleOf(INDICE_LISTA to indiceLista)
            return f
        }
    }
}
