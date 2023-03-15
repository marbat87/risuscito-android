package it.cammino.risuscito.ui.fragment

import android.content.Intent
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.database.pojo.Posizione
import it.cammino.risuscito.databinding.ActivityListaPersonalizzataBinding
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.items.listaPersonalizzataItem
import it.cammino.risuscito.items.posizioneTitleItem
import it.cammino.risuscito.objects.posizioneItem
import it.cammino.risuscito.ui.activity.InsertActivity
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.dialog.BottomSheetFragment
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.InputTextDialogFragment
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.launchForResultWithAnimation
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.utils.extension.useOldIndex
import it.cammino.risuscito.viewmodels.DefaultListaViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaPredefinitaFragment : Fragment(), ActionModeFragment {

    private val mCantiViewModel: DefaultListaViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }
    private val inputdialogViewModel: InputTextDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var posizioneDaCanc: Int = 0
    private var notaDaCanc: String = StringUtils.EMPTY
    private var idDaCanc: Int = 0
    private var timestampDaCanc: String? = null
    private var mSwhitchMode: Boolean = false
    private val posizioniList: ArrayList<ListaPersonalizzataItem> = ArrayList()
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private val cantoAdapter: FastItemAdapter<ListaPersonalizzataItem> = FastItemAdapter()
    private var mMainActivity: MainActivity? = null
    private var mLastClickTime: Long = 0

    private var _binding: ActivityListaPersonalizzataBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityListaPersonalizzataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMainActivity?.destroyActionMode()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity = activity as? MainActivity

        mSwhitchMode = false

        // Creating new adapter object
        cantoAdapter.setHasStableIds(true)
        cantoAdapter.set(posizioniList)
        binding.recyclerList.adapter = cantoAdapter

        // Setting the layoutManager
        binding.recyclerList.layoutManager = LinearLayoutManager(activity)

        subscribeUiUpdate()

        binding.buttonPulisci.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                RisuscitoDatabase.getInstance(requireContext()).customListDao()
                    .deleteListById(mCantiViewModel.defaultListaId)
            }
        }

        binding.buttonCondividi.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetFragment.newInstance(R.string.share_by, defaultIntent)
            bottomSheetDialog.show(parentFragmentManager, null)
        }
    }

    private fun getCantofromPosition(
        posizioni: List<Posizione>, title: String, position: Int, tag: Int, useOldIndex: Boolean
    ): ListaPersonalizzataItem {
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
                    withTitle(Utility.getResId(it.titolo, R.string::class.java))
                    withPage(
                        Utility.getResId(
                            if (useOldIndex) it.pagina + Utility.OLD_PAGE_SUFFIX else it.pagina,
                            R.string::class.java
                        )
                    )
                    withSource(Utility.getResId(it.source, R.string::class.java))
                    withNota(it.notaPosizione)
                    withColor(it.color ?: Canto.BIANCO)
                    withId(it.id)
                    withTimestamp(it.timestamp?.time.toString())
                }
            }
            createClickListener = click
            createLongClickListener = longClick
            editNoteClickListener = noteClick
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
                if (tempItem.nota.isNotEmpty())
                    result.append(" (")
                        .append(tempItem.nota)
                        .append(")")
                result.append("\n")
            }
        } else {
            result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<")
            result.append("\n")
        }

        return result.toString()
    }

    private fun snackBarRimuoviCanto(view: View) {
        mMainActivity?.destroyActionMode()
        val parent = view.parent.parent as? View
        longclickedPos =
            Integer.valueOf(parent?.findViewById<TextView>(R.id.generic_tag)?.text.toString())
        longClickedChild =
            Integer.valueOf(view.findViewById<TextView>(R.id.item_tag).text.toString())
        Log.d(
            TAG,
            "snackBarRimuoviCanto - longclickedPos: $longclickedPos / defaultListaId: ${mCantiViewModel.defaultListaId} / longCLickedChild: $longClickedChild"
        )
        startCab()
    }

    private fun startCab() {
        mSwhitchMode = false
        posizioniList[longclickedPos].listItem?.get(longClickedChild)?.setmSelected(true)
        cantoAdapter.notifyItemChanged(longclickedPos)
        mMainActivity?.createActionMode(R.menu.menu_actionmode_lists, this) { item ->
            Log.d(TAG, "MaterialCab onActionItemClicked")
            when (item?.itemId) {
                R.id.action_remove_item -> {
                    mMainActivity?.destroyActionMode()
                    ListeUtils.removePositionWithUndo(
                        this@ListaPredefinitaFragment,
                        mCantiViewModel.defaultListaId,
                        posizioneDaCanc,
                        idDaCanc,
                        timestampDaCanc.orEmpty(),
                        notaDaCanc
                    )
                    true
                }
                R.id.action_switch_item -> {
                    mSwhitchMode = true
                    updateActionModeTitle(true)
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.switch_tooltip),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    true
                }
                else -> false
            }
        }
        updateActionModeTitle(false)
    }

    override fun destroyActionMode() {
        Log.d(
            TAG,
            "MaterialCab onDestroy - longclickedPos: $longclickedPos / defaultListaId: ${mCantiViewModel.defaultListaId} "
        )
        mSwhitchMode = false
        try {
            posizioniList[longclickedPos].listItem?.get(longClickedChild)
                ?.setmSelected(false)
            cantoAdapter.notifyItemChanged(longclickedPos)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun updateActionModeTitle(switchMode: Boolean) {
        mMainActivity?.updateActionModeTitle(
            if (switchMode)
                resources.getString(R.string.switch_started)
            else
                resources.getQuantityString(R.plurals.item_selected, 1, 1)
        )
    }

    private fun subscribeUiUpdate() {
        val useOldIndex = requireContext().useOldIndex()
        mCantiViewModel.cantiResult?.observe(viewLifecycleOwner) { mCanti ->
            var progressiveTag = 0
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            posizioniList.clear()

            when (mCantiViewModel.defaultListaId) {
                1 -> {
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.canto_iniziale),
                            1,
                            progressiveTag++,
                            useOldIndex
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.prima_lettura),
                            2,
                            progressiveTag++,
                            useOldIndex
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.seconda_lettura),
                            3,
                            progressiveTag++,
                            useOldIndex
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.terza_lettura),
                            4,
                            progressiveTag++,
                            useOldIndex
                        )
                    )

                    if (pref.getBoolean(Utility.SHOW_PACE, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.canto_pace),
                                6,
                                progressiveTag++,
                                useOldIndex
                            )
                        )

                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.canto_fine),
                            5,
                            progressiveTag,
                            useOldIndex
                        )
                    )
                }
                2 -> {
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.canto_iniziale),
                            1,
                            progressiveTag++,
                            useOldIndex
                        )
                    )

                    if (pref.getBoolean(Utility.SHOW_SECONDA, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.seconda_lettura),
                                6,
                                progressiveTag++,
                                useOldIndex
                            )
                        )

                    if (pref.getBoolean(Utility.SHOW_EUCARESTIA_PACE, true))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.canto_pace),
                                2,
                                progressiveTag++,
                                useOldIndex
                            )
                        )

                    if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.canto_offertorio),
                                8,
                                progressiveTag++,
                                useOldIndex
                            )
                        )

                    if (pref.getBoolean(Utility.SHOW_SANTO, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.santo),
                                7,
                                progressiveTag++,
                                useOldIndex
                            )
                        )

                    posizioniList.add(
                        getCantofromPosition(
                            mCanti, getString(R.string.canto_pane), 3, progressiveTag++, useOldIndex
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti, getString(R.string.canto_vino), 4, progressiveTag++, useOldIndex
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.canto_fine),
                            5,
                            progressiveTag, useOldIndex
                        )
                    )
                }
            }
            cantoAdapter.set(posizioniList)
        }

        inputdialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(ListaPersonalizzataFragment.TAG, "inputdialogViewModel state $it")
            if (!inputdialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (inputdialogViewModel.mTag) {
                            EDIT_NOTE_PREDEFINITA + mCantiViewModel.defaultListaId -> {
                                inputdialogViewModel.handled = true
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val mDao =
                                        RisuscitoDatabase.getInstance(requireContext())
                                            .customListDao()
                                    mDao.updateNotaPosition(
                                        inputdialogViewModel.outputText,
                                        mCantiViewModel.defaultListaId,
                                        inputdialogViewModel.outputItemId,
                                        inputdialogViewModel.outputCantoId
                                    )
                                }
                                mMainActivity?.activityMainContent?.let { v ->
                                    Snackbar.make(
                                        v,
                                        R.string.edit_note_confirm_message,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        inputdialogViewModel.handled = true
                    }
                }
            }
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
            val l = resources.systemLocale
            val result = StringBuilder()
            var progressivePos = 0

            when (mCantiViewModel.defaultListaId) {
                1 -> {
                    result
                        .append("-- ")
                        .append(getString(R.string.title_activity_canti_parola).uppercase(l))
                        .append(" --\n")

                    result.append(resources.getString(R.string.canto_iniziale).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.prima_lettura).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.seconda_lettura).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.terza_lettura).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

                    if (pref.getBoolean(Utility.SHOW_PACE, false)) {
                        result.append(resources.getString(R.string.canto_pace).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_fine).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos))
                }
                2 -> {
                    result
                        .append("-- ")
                        .append(getString(R.string.title_activity_canti_eucarestia).uppercase(l))
                        .append(" --")
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_iniziale).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
                        result.append(resources.getString(R.string.seconda_lettura).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }

                    if (pref.getBoolean(Utility.SHOW_EUCARESTIA_PACE, true)) {
                        result.append(resources.getString(R.string.canto_pace).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }

                    if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
                        result.append(resources.getString(R.string.canto_offertorio).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
                        result.append(resources.getString(R.string.santo).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_pane).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_vino).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_fine).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos))
                }
            }
            return result.toString()
        }

    private val startListInsertForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.i(TAG, "startListInsertForResult result.resultCode ${result.resultCode}")
            if (result.resultCode == CustomListsFragment.RESULT_OK || result.resultCode == CustomListsFragment.RESULT_KO)
                mMainActivity?.activityMainContent?.let {
                    Snackbar.make(
                        it,
                        if (result.resultCode == CustomListsFragment.RESULT_OK) R.string.list_added else R.string.present_yet,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        }

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
            mLastClickTime = SystemClock.elapsedRealtime()
            val parent = v.parent.parent as? View
            if (v.id == R.id.add_canto_generico) {
                if (mSwhitchMode) {
                    mMainActivity?.destroyActionMode()
                    ListeUtils.scambioConVuoto(
                        this,
                        mCantiViewModel.defaultListaId,
                        posizioneDaCanc,
                        idDaCanc,
                        notaDaCanc,
                        Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
                    )
                } else {
                    if (mMainActivity?.isActionMode != true) {
                        mMainActivity?.let {
                            it.launchForResultWithAnimation(
                                startListInsertForResult,
                                Intent(it, InsertActivity::class.java).putExtras(
                                    bundleOf(
                                        InsertActivity.FROM_ADD to 1,
                                        InsertActivity.ID_LISTA to mCantiViewModel.defaultListaId,
                                        InsertActivity.POSITION to Integer.valueOf(
                                            parent?.findViewById<TextView>(
                                                R.id.text_id_posizione
                                            )?.text.toString()
                                        )
                                    )
                                ),
                                com.google.android.material.transition.platform.MaterialSharedAxis.Y
                            )
                        }
                    }
                }
            } else {
                if (!mSwhitchMode)
                    if (mMainActivity?.isActionMode == true) {
                        posizioneDaCanc =
                            Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
                        idDaCanc =
                            Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
                        timestampDaCanc =
                            v.findViewById<TextView>(R.id.text_timestamp).text.toString()
                        notaDaCanc =
                            v.findViewById<TextView>(R.id.text_nota_canto).text.toString()
                        snackBarRimuoviCanto(v)
                    } else {
                        //apri canto
                        mMainActivity?.openCanto(
                            TAG,
                            Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString()),
                            v.findViewById<TextView>(R.id.text_source_canto).text.toString(),
                            false
                        )
                    }
                else {
                    mMainActivity?.destroyActionMode()
                    ListeUtils.scambioCanto(
                        this,
                        mCantiViewModel.defaultListaId,
                        posizioneDaCanc,
                        idDaCanc,
                        notaDaCanc,
                        Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString()),
                        Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString()),
                        v.findViewById<TextView>(R.id.text_nota_canto).text.toString()
                    )
                }
            }
        }
    }

    private val longClick = OnLongClickListener { v ->
        val parent = v.parent.parent as? View
        posizioneDaCanc =
            Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
        idDaCanc =
            Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
        timestampDaCanc = v.findViewById<TextView>(R.id.text_timestamp).text.toString()
        notaDaCanc = v.findViewById<TextView>(R.id.text_nota_canto).text.toString()
        snackBarRimuoviCanto(v)
        true
    }

    private val noteClick = object : ListaPersonalizzataItem.NoteClickListener {
        override fun onclick(idPosizione: Int, nota: String, idCanto: Int) {
            mMainActivity?.let { mActivity ->
                InputTextDialogFragment.show(
                    InputTextDialogFragment.Builder(
                        EDIT_NOTE_PREDEFINITA + mCantiViewModel.defaultListaId
                    ).apply {
                        title = R.string.edit_note_title
                        positiveButton = R.string.action_salva
                        negativeButton = R.string.cancel
                        prefill = nota
                        itemId = idPosizione
                        cantoId = idCanto
                        multiLine = true
                    }, mActivity.supportFragmentManager
                )
            }
        }
    }

    companion object {
        private const val INDICE_LISTA = "indiceLista"
        private const val EDIT_NOTE_PREDEFINITA = "EDIT_NOTE_PREDEFINITA"
        private val TAG = ListaPredefinitaFragment::class.java.canonicalName

        fun newInstance(indiceLista: Int): ListaPredefinitaFragment {
            val f = ListaPredefinitaFragment()
            f.arguments = bundleOf(INDICE_LISTA to indiceLista)
            return f
        }
    }
}
