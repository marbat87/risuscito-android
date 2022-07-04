package it.cammino.risuscito.ui.fragment

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat.START
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.SignInButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.michaelflisar.changelog.ChangelogBuilder
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.SearchLayoutBinding
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.activity.PaginaRenderActivity
import it.cammino.risuscito.utils.*
import it.cammino.risuscito.utils.extension.*
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.Collator

class HomeFragment : AccountMenuFragment() {

    private val mViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(
            requireActivity().application,
            Bundle().apply { putInt(Utility.TIPO_LISTA, 0) })
    }
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val activityViewModel: MainActivityViewModel by viewModels({ requireActivity() })

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mMainActivity?.let {
                Snackbar.make(
                    it.activityMainContent,
                    getString(R.string.permission_ok),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putString(Utility.SAVE_LOCATION, "0") }
            mMainActivity?.let {
                Snackbar.make(
                    it.activityMainContent,
                    getString(R.string.external_storage_denied),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private var job: Job = Job()

    private val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()

    private var listePersonalizzate: List<ListaPers>? = null

    private var mLastClickTime: Long = 0
    private lateinit var mPopupMenu: PopupMenu

    private var _binding: SearchLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SearchLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setupToolbarTitle(R.string.activity_homepage)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        binding.coverLayout.coverImage.isVisible = true
        binding.coverLayout.coverImage.setOnClickListener {
            if (context?.isOnTablet == false)
                mMainActivity?.activityDrawer?.openDrawer(START)
        }

        binding.signInButton.setSize(SignInButton.SIZE_WIDE)
        binding.signInButton.isInvisible =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(Utility.SIGNED_IN, false)
        binding.signInButton.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putBoolean(Utility.SIGN_IN_REQUESTED, true) }
            activityViewModel.showSnackbar = true
            mMainActivity?.signIn()
        }

        activityViewModel.signedIn.observe(viewLifecycleOwner) {
            binding.signInButton.isVisible = !it
        }

        Log.d(TAG, "getVersionCodeWrapper(): ${getVersionCodeWrapper()}")

        ChangelogBuilder()
            .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
            .withMinVersionToShow(getVersionCodeWrapper())     // provide a number and the log will only show changelog rows for versions equal or higher than this number
            .withManagedShowOnStart(
                requireContext().getSharedPreferences(
                    "com.michaelflisar.changelog",
                    0
                ).getInt("changelogVersion", -1) != -1
            )  // library will take care to show activity/dialog only if the changelog has new infos and will only show this new infos
            .withTitle(getString(R.string.dialog_change_title)) // provide a custom title if desired, default one is "Changelog <VERSION>"
            .withOkButtonLabel(getString(R.string.ok)) // provide a custom ok button text if desired, default one is "OK"
            .buildAndShowDialog(
                mMainActivity,
                requireContext().isDarkMode
            ) // second parameter defines, if the dialog has a dark or light theme

        if (!OSUtils.hasQ())
            checkPermission()

        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0") ?: "0")
            mViewModel.advancedSearch = currentItem != 0
        }

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.fileout)
            mViewModel.aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            listePersonalizzate = RisuscitoDatabase.getInstance(requireContext()).listePersDao().all
        }

        subscribeUiCanti()

        binding.textBoxRicerca.hint =
            if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(
                R.string.fast_search_subtitle
            )

        cantoAdapter.onClickListener =
            { mView: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    val intent = Intent(
                        requireActivity().applicationContext,
                        PaginaRenderActivity::class.java
                    )
                    intent.putExtras(
                        bundleOf(
                            Utility.PAGINA to item.source?.getText(requireContext()),
                            Utility.ID_CANTO to item.id
                        )
                    )
                    mMainActivity?.startActivityWithTransition(intent, mView)
                    consume = true
                }
                consume
            }

        cantoAdapter.onLongClickListener =
            { v: View, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                mViewModel.idDaAgg = item.id
                mViewModel.popupMenu(
                    this, v,
                    SEARCH_REPLACE,
                    SEARCH_REPLACE_2, listePersonalizzate
                )
                true
            }

        cantoAdapter.setHasStableIds(true)

        binding.matchedList.adapter = cantoAdapter
        val llm = if (context?.isGridLayout == true)
            GridLayoutManager(context, if (context?.hasThreeColumns == true) 3 else 2)
        else
            LinearLayoutManager(context)
        binding.matchedList.layoutManager = llm

        binding.textFieldRicerca.setOnKeyListener { _, keyCode, _ ->
            var returnValue = false
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // to hide soft keyboard
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(binding.textFieldRicerca.windowToken, 0)
                returnValue = true
            }
            returnValue
        }

        binding.textFieldRicerca.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
            job.cancel()
            ricercaStringa(s.toString())
        }

        mPopupMenu = PopupMenu(requireContext(), binding.moreOptions, Gravity.END)
        mPopupMenu.inflate(R.menu.search_option_menu)
        mPopupMenu.menu.findItem(R.id.consegnaty_only).isVisible = false
        mPopupMenu.setOnMenuItemClickListener {
            it.isChecked = !it.isChecked
            mViewModel.advancedSearch = it.isChecked
            binding.textBoxRicerca.hint =
                if (mViewModel.advancedSearch) getString(R.string.advanced_search_subtitle) else getString(
                    R.string.fast_search_subtitle
                )
            job.cancel()
            ricercaStringa(binding.textFieldRicerca.text.toString())
            true
        }

        binding.moreOptions.setOnClickListener {
            mPopupMenu.menu.findItem(R.id.advanced_search).isChecked = mViewModel.advancedSearch
            mPopupMenu.show()
        }

        // to hide soft keyboard
        Handler(Looper.getMainLooper()).postDelayed(500) {
            context?.let {
                ContextCompat.getSystemService(it, InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(binding.textFieldRicerca.windowToken, 0)
            }
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "permission granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.external_storage_pref_rationale)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        run {
                            dialog.cancel()
                            requestPermissionLauncher.launch(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun getVersionCodeP(): Int {
        return requireActivity()
            .packageManager
            .getPackageInfo(requireActivity().packageName, 0)
            .longVersionCode.toInt()
    }

    @Suppress("DEPRECATION")
    private fun getVersionCodeLegacy(): Int {
        return requireActivity()
            .packageManager
            .getPackageInfo(requireActivity().packageName, 0)
            .versionCode
    }

    private fun getVersionCodeWrapper(): Int {
        return if (OSUtils.hasP())
            getVersionCodeP()
        else
            getVersionCodeLegacy()
    }

    private fun ricercaStringa(s: String) {
        job = lifecycleScope.launch {
            // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
            binding.coverLayout.coverImage.isVisible = s.isEmpty()
            binding.signInButton.isVisible =
                s.isEmpty() && !(activityViewModel.signedIn.value ?: false)
            if (s.trim { it <= ' ' }.length >= 3) {
                binding.searchNoResults.isVisible = false
                binding.searchProgress.isVisible = true
                val titoliResult = ArrayList<SimpleItem>()

                Log.d(TAG, "performSearch STRINGA: $s")
                Log.d(TAG, "performSearch ADVANCED: ${mViewModel.advancedSearch}")
                if (mViewModel.advancedSearch) {
                    val words =
                        s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (aText in mViewModel.aTexts) {
                        if (!isActive) return@launch

                        if (aText[0] == null || aText[0].isNullOrEmpty()) break

                        var found = true
                        for (word in words) {
                            if (!isActive) return@launch

                            if (word.trim { it <= ' ' }.length > 1) {
                                var text = word.trim { it <= ' ' }
                                text = text.lowercase(resources.systemLocale)
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(tag, "aText[0]: ${aText[0]}")
                            mViewModel.titoli
                                .filter { (aText[0].orEmpty()) == it.undecodedSource }
                                .forEach {
                                    if (!isActive) return@launch
                                    titoliResult.add(it.apply { filter = StringUtils.EMPTY })
                                }
                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).lowercase(resources.systemLocale)
                    Log.d(tag, "performSearch onTextChanged: stringa $stringa")
                    mViewModel.titoli
                        .filter {
                            Utility.removeAccents(
                                it.title?.getText(requireContext()).orEmpty()
                            ).lowercase(resources.systemLocale).contains(stringa)
                        }
                        .forEach {
                            if (!isActive) return@launch
                            titoliResult.add(it.apply { filter = stringa })
                        }
                }
                if (isActive) {
                    cantoAdapter.set(
                        titoliResult.sortedWith(
                            compareBy(
                                Collator.getInstance(resources.systemLocale)
                            ) { it.title?.getText(requireContext()) })
                    )
                    binding.searchProgress.isVisible = false
                    binding.searchNoResults.isVisible = cantoAdapter.adapterItemCount == 0
                    binding.matchedList.isGone = cantoAdapter.adapterItemCount == 0
                }
            } else {
                if (s.isEmpty()) {
                    binding.searchNoResults.isVisible = false
                    binding.matchedList.isVisible = false
                    cantoAdapter.clear()
                    binding.searchProgress.isVisible = false
                    mMainActivity?.expandToolbar()
                }
            }
        }
    }

    private fun subscribeUiCanti() {
        mViewModel.itemsResult?.observe(viewLifecycleOwner) { canti ->
            mViewModel.titoli =
                canti.sortedWith(compareBy(
                    Collator.getInstance(resources.systemLocale)
                ) {
                    it.title?.getText(requireContext())
                })
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            SEARCH_REPLACE -> {
                                simpleDialogViewModel.handled = true
                                listePersonalizzate?.let { lista ->
                                    lista[mViewModel.idListaClick]
                                        .lista?.addCanto(
                                            mViewModel.idDaAgg.toString(),
                                            mViewModel.idPosizioneClick
                                        )
                                    ListeUtils.updateListaPersonalizzata(
                                        this,
                                        lista[mViewModel.idListaClick]
                                    )
                                }
                            }
                            SEARCH_REPLACE_2 -> {
                                simpleDialogViewModel.handled = true
                                ListeUtils.updatePosizione(
                                    this,
                                    mViewModel.idDaAgg,
                                    mViewModel.idListaDaAgg,
                                    mViewModel.posizioneDaAgg
                                )
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

    }

    companion object {
        private val TAG = HomeFragment::class.java.canonicalName
        private const val SEARCH_REPLACE = "SEARCH_REPLACE"
        private const val SEARCH_REPLACE_2 = "SEARCH_REPLACE_2"
    }

}