package it.cammino.risuscito.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityOptionsCompat
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
import it.cammino.risuscito.ui.activity.PaginaRenderActivity
import it.cammino.risuscito.ui.dialog.BottomSheetFragment
import it.cammino.risuscito.utils.*
import it.cammino.risuscito.utils.extension.slideInRight
import it.cammino.risuscito.utils.extension.startActivityWithTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.DefaultListaViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        posizioni: List<Posizione>, title: String, position: Int, tag: Int
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
                    withPage(Utility.getResId(it.pagina, R.string::class.java))
                    withSource(Utility.getResId(it.source, R.string::class.java))
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
        mMainActivity?.actionMode?.finish()
        val parent = view.parent.parent as? View
        longclickedPos =
            Integer.valueOf(parent?.findViewById<TextView>(R.id.generic_tag)?.text.toString())
        longClickedChild =
            Integer.valueOf(view.findViewById<TextView>(R.id.item_tag).text.toString())
        startCab()
    }

    private fun startCab() {
        mSwhitchMode = false

        val callback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
                activity?.menuInflater?.inflate(R.menu.menu_actionmode_lists, menu)
                posizioniList[longclickedPos].listItem?.get(longClickedChild)?.setmSelected(true)
                cantoAdapter.notifyItemChanged(longclickedPos)
                actionModeOk = false
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                Log.d(TAG, "MaterialCab onActionItemClicked")
                return when (item?.itemId) {
                    R.id.action_remove_item -> {
                        actionModeOk = true
                        mMainActivity?.actionMode?.finish()
                        ListeUtils.removePositionWithUndo(
                            this@ListaPredefinitaFragment,
                            mCantiViewModel.defaultListaId,
                            posizioneDaCanc,
                            idDaCanc,
                            timestampDaCanc.orEmpty()
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

            override fun onDestroyActionMode(mode: ActionMode?) {
                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                Log.d(
                    TAG,
                    "MaterialCab onDestroy - longclickedPos: $longclickedPos / defaultListaId: ${mCantiViewModel.defaultListaId}"
                )
                mSwhitchMode = false
                if (!actionModeOk) {
                    try {
                        posizioniList[longclickedPos].listItem?.get(longClickedChild)
                            ?.setmSelected(false)
                        cantoAdapter.notifyItemChanged(longclickedPos)
                    } catch (e: Exception) {
                        Firebase.crashlytics.recordException(e)
                    }
                }
                mMainActivity?.destroyActionMode()
            }

        }

        mMainActivity?.createActionMode(callback)
        updateActionModeTitle(false)
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
                            progressiveTag++
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.prima_lettura),
                            2,
                            progressiveTag++
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.seconda_lettura),
                            3,
                            progressiveTag++
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.terza_lettura),
                            4,
                            progressiveTag++
                        )
                    )

                    if (pref.getBoolean(Utility.SHOW_PACE, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.canto_pace),
                                6,
                                progressiveTag++
                            )
                        )

                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.canto_fine),
                            5,
                            progressiveTag
                        )
                    )
                }
                2 -> {
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti, getString(R.string.canto_iniziale), 1, progressiveTag++
                        )
                    )

                    if (pref.getBoolean(Utility.SHOW_SECONDA, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti, getString(R.string.seconda_lettura), 6, progressiveTag++
                            )
                        )

                    posizioniList.add(
                        getCantofromPosition(
                            mCanti, getString(R.string.canto_pace), 2, progressiveTag++
                        )
                    )

                    if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti, getString(R.string.canto_offertorio), 8, progressiveTag++
                            )
                        )

                    if (pref.getBoolean(Utility.SHOW_SANTO, false))
                        posizioniList.add(
                            getCantofromPosition(
                                mCanti,
                                getString(R.string.santo),
                                7,
                                progressiveTag++
                            )
                        )

                    posizioniList.add(
                        getCantofromPosition(
                            mCanti, getString(R.string.canto_pane), 3, progressiveTag++
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti, getString(R.string.canto_vino), 4, progressiveTag++
                        )
                    )
                    posizioniList.add(
                        getCantofromPosition(
                            mCanti,
                            getString(R.string.canto_fine),
                            5,
                            progressiveTag
                        )
                    )
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
                    result.append(resources.getString(R.string.canto_pace).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
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
                    actionModeOk = true
                    mMainActivity?.actionMode?.finish()
                    ListeUtils.scambioConVuoto(
                        this,
                        mCantiViewModel.defaultListaId,
                        posizioneDaCanc,
                        idDaCanc,
                        Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
                    )
                } else {
                    if (mMainActivity?.actionMode == null) {
                        val intent = Intent(activity, InsertActivity::class.java)
                        intent.putExtras(
                            bundleOf(
                                InsertActivity.FROM_ADD to 1,
                                InsertActivity.ID_LISTA to mCantiViewModel.defaultListaId,
                                InsertActivity.POSITION to Integer.valueOf(
                                    parent?.findViewById<TextView>(
                                        R.id.text_id_posizione
                                    )?.text.toString()
                                )
                            )
                        )

                        mMainActivity?.let {
                            if (OSUtils.isObySamsung()) {
                                startListInsertForResult.launch(intent)
                                it.slideInRight()
                            } else {
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    it,
                                    v,
                                    "shared_insert_container" // The transition name to be matched in Activity B.
                                )
                                startListInsertForResult.launch(intent, options)
                            }
                        }
                    }
                }
            } else {
                if (!mSwhitchMode)
                    if (mMainActivity?.actionMode != null) {
                        posizioneDaCanc =
                            Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
                        idDaCanc =
                            Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
                        timestampDaCanc =
                            v.findViewById<TextView>(R.id.text_timestamp).text.toString()
                        snackBarRimuoviCanto(v)
                    } else {
                        //apri canto
                        val intent = Intent(activity, PaginaRenderActivity::class.java)
                        intent.putExtras(
                            bundleOf(
                                Utility.PAGINA to v.findViewById<TextView>(R.id.text_source_canto).text.toString(),
                                Utility.ID_CANTO to Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
                            )
                        )
                        mMainActivity?.startActivityWithTransition(intent, v)
                    }
                else {
                    actionModeOk = true
                    mMainActivity?.actionMode?.finish()
                    ListeUtils.scambioCanto(
                        this,
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
        posizioneDaCanc =
            Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
        idDaCanc =
            Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())
        timestampDaCanc = v.findViewById<TextView>(R.id.text_timestamp).text.toString()
        snackBarRimuoviCanto(v)
        true
    }

    companion object {
        private const val INDICE_LISTA = "indiceLista"
        private val TAG = ListaPredefinitaFragment::class.java.canonicalName

        fun newInstance(indiceLista: Int): ListaPredefinitaFragment {
            val f = ListaPredefinitaFragment()
            f.arguments = bundleOf(INDICE_LISTA to indiceLista)
            return f
        }
    }
}
