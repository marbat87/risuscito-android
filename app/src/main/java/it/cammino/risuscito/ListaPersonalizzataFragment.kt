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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialcab.MaterialCab.Companion.destroy
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.ActivityListaPersonalizzataBinding
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.ui.BottomSheetFragment
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ThemeUtils.Companion.isDarkMode
import it.cammino.risuscito.utils.ioThread
import it.cammino.risuscito.utils.themeColor
import it.cammino.risuscito.viewmodels.ListaPersonalizzataViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory

class ListaPersonalizzataFragment : Fragment() {

    private val mCantiViewModel: ListaPersonalizzataViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA) ?: 0)
        })
    }

    private lateinit var cantoDaCanc: String
    private var posizioneDaCanc: Int = 0
    private var mSwhitchMode: Boolean = false
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private val cantoAdapter: FastItemAdapter<ListaPersonalizzataItem> = FastItemAdapter()
    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLUtils: LUtils? = null
    private var mLastClickTime: Long = 0

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
        cantoAdapter.set(mCantiViewModel.posizioniList)
        binding.recyclerList.adapter = cantoAdapter

        // Setting the layoutManager
        binding.recyclerList.layoutManager = LinearLayoutManager(activity)

        subscribeUiChanges()

        binding.buttonPulisci.setOnClickListener {
            for (i in 0 until (mCantiViewModel.listaPersonalizzata?.numPosizioni ?: 0))
                mCantiViewModel.listaPersonalizzata?.removeCanto(i)
            runUpdate()
        }

        binding.buttonCondividi.setOnClickListener {
            val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, shareIntent)
            bottomSheetDialog.show(parentFragmentManager, null)
        }

        binding.buttonInviaFile.setOnClickListener {
            val exportUri = mLUtils?.listToXML(mCantiViewModel.listaPersonalizzata)
            Log.d(TAG, "onClick: exportUri = $exportUri")
            @Suppress("SENSELESS_COMPARISON")
            if (exportUri != null) {
                val bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, getSendIntent(exportUri))
                bottomSheetDialog.show(parentFragmentManager, null)
            } else
                mMainActivity?.let {
                    Snackbar.make(it.activityMainContent, R.string.xml_error, Snackbar.LENGTH_LONG).show()
                }
        }

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
        intent.putExtras(bundleOf(Utility.PAGINA to v.findViewById<TextView>(R.id.text_source_canto).text.toString(),
                Utility.ID_CANTO to Integer.valueOf(v.findViewById<TextView>(R.id.text_id_canto_card).text.toString())))
        mLUtils?.startActivityWithTransition(intent)
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

    private fun scambioCanto(posizioneNew: Int) {
        if (posizioneNew != posizioneDaCanc) {

            val cantoTmp = mCantiViewModel.listaPersonalizzata?.getCantoPosizione(posizioneNew)
            mCantiViewModel.listaPersonalizzata?.addCanto(
                    mCantiViewModel.listaPersonalizzata?.getCantoPosizione(posizioneDaCanc), posizioneNew)
            mCantiViewModel.listaPersonalizzata?.addCanto(cantoTmp, posizioneDaCanc)

            runUpdate()

            actionModeOk = true
            destroy()
            mMainActivity?.let {
                Snackbar.make(it.activityMainContent, R.string.switch_done, Snackbar.LENGTH_SHORT).show()
            }

        } else
            mMainActivity?.let {
                Snackbar.make(it.activityMainContent, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun scambioConVuoto(posizioneNew: Int) {
        //        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
        //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        mCantiViewModel.listaPersonalizzata?.addCanto(
                mCantiViewModel.listaPersonalizzata?.getCantoPosizione(posizioneDaCanc), posizioneNew)
        mCantiViewModel.listaPersonalizzata?.removeCanto(posizioneDaCanc)

        runUpdate()

        actionModeOk = true
        destroy()
        mMainActivity?.let {
            Snackbar.make(it.activityMainContent, R.string.switch_done, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startCab(switchMode: Boolean) {
        mSwhitchMode = switchMode
        mMainActivity?.let { mainActivity ->
            MaterialCab.attach(mainActivity, R.id.cab_stub) {
                if (switchMode)
                    titleRes(R.string.switch_started)
                else
                    title = resources.getQuantityString(R.plurals.item_selected, 1, 1)
                popupTheme = R.style.ThemeOverlay_MaterialComponents_Dark_ActionBar
                contentInsetStartRes(R.dimen.mcab_default_content_inset)
                if (isDarkMode(requireContext()))
                    backgroundColor = requireContext().themeColor(R.attr.colorSurface)
                menuRes = R.menu.menu_actionmode_lists

                onCreate { _, _ ->
                    Log.d(TAG, "MaterialCab onCreate")
                    mCantiViewModel.posizioniList[longclickedPos].listItem?.get(longClickedChild)?.setmSelected(true)
                    cantoAdapter.notifyItemChanged(longclickedPos)
                    actionModeOk = false
                }

                onSelection { item ->
                    Log.d(TAG, "MaterialCab onSelection")
                    when (item.itemId) {
                        R.id.action_remove_item -> {
                            cantoDaCanc = mCantiViewModel.listaPersonalizzata?.getCantoPosizione(posizioneDaCanc)
                                    ?: ""
                            mCantiViewModel.listaPersonalizzata?.removeCanto(posizioneDaCanc)
                            runUpdate()
                            actionModeOk = true
                            destroy()
                            mMainActivity?.let {
                                Snackbar.make(it.activityMainContent, R.string.song_removed, Snackbar.LENGTH_LONG)
                                        .setAction(getString(R.string.cancel).toUpperCase(getSystemLocale(resources))) {
                                            mCantiViewModel.listaPersonalizzata?.addCanto(cantoDaCanc, posizioneDaCanc)
                                            runUpdate()
                                        }
                                        .show()
                            }
                            true
                        }
                        R.id.action_switch_item -> {
                            cantoDaCanc = mCantiViewModel.listaPersonalizzata?.getCantoPosizione(posizioneDaCanc)
                                    ?: ""
                            startCab(true)
                            Toast.makeText(activity, resources.getString(R.string.switch_tooltip), Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }

                onDestroy {
                    Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                    Log.d(TAG, "MaterialCab onDestroy - longclickedPos: $longclickedPos / listaPersonalizzataId: ${mCantiViewModel.listaPersonalizzataId}")
                    mSwhitchMode = false
                    if (!actionModeOk) {
                        try {
                            mCantiViewModel.posizioniList[longclickedPos].listItem?.get(longClickedChild)?.setmSelected(false)
                            cantoAdapter.notifyItemChanged(longclickedPos)
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }
                    true
                }
            }
        }
    }

    private fun runUpdate() {
        ioThread {
            mMainActivity?.let {
                val listaNew = ListaPers()
                listaNew.lista = mCantiViewModel.listaPersonalizzata
                listaNew.id = mCantiViewModel.listaPersonalizzataId
                listaNew.titolo = mCantiViewModel.listaPersonalizzataTitle
                val mDao = RisuscitoDatabase.getInstance(it).listePersDao()
                mDao.updateLista(listaNew)
            }
        }
    }

    private fun subscribeUiChanges() {
        mCantiViewModel.listaPersonalizzataResult?.observe(viewLifecycleOwner) { listaPersonalizzataResult ->
            mCantiViewModel.posizioniList = listaPersonalizzataResult.map {
                it.apply {
                    createClickListener = click
                    createLongClickListener = longClick
                }
            }
            cantoAdapter.set(mCantiViewModel.posizioniList)
        }
    }

    private val shareIntent: Intent
        get() = Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, titlesList)
                .setType("text/plain")

    private val titlesList: String
        get() {
            val l = getSystemLocale(resources)
            val result = StringBuilder()
            result.append("-- ").append(mCantiViewModel.listaPersonalizzata?.name?.toUpperCase(l)).append(" --\n")
            for (i in 0 until (mCantiViewModel.listaPersonalizzata?.numPosizioni ?: 0)) {
                result.append(mCantiViewModel.listaPersonalizzata?.getNomePosizione(i)?.toUpperCase(l)).append("\n")
                if (!mCantiViewModel.listaPersonalizzata?.getCantoPosizione(i).isNullOrEmpty()) {
                    mCantiViewModel.posizioniList[i].listItem?.let {
                        for (tempItem in it) {
                            result
                                    .append(tempItem.title?.getText(requireContext()))
                                    .append(" - ")
                                    .append(getString(R.string.page_contracted))
                                    .append(tempItem.page?.getText(requireContext()))
                            result.append("\n")
                        }
                    }
                } else {
                    result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<")
                    result.append("\n")
                }
                if (i < (mCantiViewModel.listaPersonalizzata?.numPosizioni
                                ?: 0) - 1) result.append("\n")
            }

            return result.toString()
        }

    private val startListInsertForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == CustomLists.RESULT_OK || result.resultCode == CustomLists.RESULT_KO)
            mMainActivity?.activityMainContent?.let {
                Snackbar.make(it, if (result.resultCode == CustomLists.RESULT_OK) R.string.list_added else R.string.present_yet, Snackbar.LENGTH_SHORT).show()
            }
    }

    private val click = OnClickListener { v ->
        if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
            mLastClickTime = SystemClock.elapsedRealtime()
            val parent = v.parent.parent as? View
            if (parent?.findViewById<View>(R.id.add_canto_generico)?.isVisible == true) {
                if (mSwhitchMode) {
                    scambioConVuoto(
                            Integer.valueOf(parent.findViewById<TextView>(R.id.text_id_posizione).text.toString()))
                } else {
                    if (!MaterialCab.isActive) {
                        val intent = Intent(activity, InsertActivity::class.java)
                        intent.putExtras(bundleOf(InsertActivity.FROM_ADD to 0,
                                InsertActivity.ID_LISTA to mCantiViewModel.listaPersonalizzataId,
                                InsertActivity.POSITION to Integer.valueOf(parent.findViewById<TextView>(R.id.text_id_posizione).text.toString())))
                        startListInsertForResult.launch(intent)
                        Animatoo.animateShrink(activity)
                    }
                }
            } else {
                if (!mSwhitchMode)
                    if (MaterialCab.isActive) {
                        posizioneDaCanc = Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
                        snackBarRimuoviCanto(v)
                    } else
                        openPagina(v)
                else {
                    scambioCanto(
                            Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString()))
                }
            }
        }
    }

    private val longClick = OnLongClickListener { v ->
        val parent = v.parent.parent as? View
        posizioneDaCanc = Integer.valueOf(parent?.findViewById<TextView>(R.id.text_id_posizione)?.text.toString())
        snackBarRimuoviCanto(v)
        true
    }

    companion object {
        internal val TAG = ListaPersonalizzataFragment::class.java.canonicalName
        private const val INDICE_LISTA = "indiceLista"

        fun newInstance(indiceLista: Int): ListaPersonalizzataFragment {
            val f = ListaPersonalizzataFragment()
            f.arguments = bundleOf(INDICE_LISTA to indiceLista)
            return f
        }
    }
}
