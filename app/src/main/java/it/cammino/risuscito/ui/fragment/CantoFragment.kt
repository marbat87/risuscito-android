package it.cammino.risuscito.ui.fragment

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.LocalLink
import it.cammino.risuscito.playback.MusicService
import it.cammino.risuscito.ui.activity.PaginaRenderFullScreen
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.ui.composable.MediaPlayerView
import it.cammino.risuscito.ui.composable.ScrollPlayerView
import it.cammino.risuscito.ui.composable.StateNotificationView
import it.cammino.risuscito.ui.composable.WebView
import it.cammino.risuscito.ui.composable.animations.AnimatedFadeContent
import it.cammino.risuscito.ui.composable.dialogs.CantoDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.DropDownMenuItem
import it.cammino.risuscito.ui.composable.dialogs.ProgressDialog
import it.cammino.risuscito.ui.composable.dialogs.ProgressDialogTag
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.dialogs.barreDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.tontalitaDropDownMenu
import it.cammino.risuscito.ui.composable.hasFiveMenuElements
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.FabActionItem
import it.cammino.risuscito.ui.composable.main.RisuscitoFab
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.main.cantoFabActions
import it.cammino.risuscito.ui.composable.main.cantoMenu
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.CambioAccordi
import it.cammino.risuscito.utils.DownloadState
import it.cammino.risuscito.utils.Downloader
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_POLISH
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.PdfExporter
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.getExternalLink
import it.cammino.risuscito.utils.Utility.getExternalMediaIdByName
import it.cammino.risuscito.utils.Utility.mediaScan
import it.cammino.risuscito.utils.Utility.retrieveMediaFileLink
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.hasStorageAccess
import it.cammino.risuscito.utils.extension.isDefaultLocationPublic
import it.cammino.risuscito.utils.extension.isOnline
import it.cammino.risuscito.utils.extension.readTextFromResource
import it.cammino.risuscito.utils.extension.startActivityWithFadeIn
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel
import it.cammino.risuscito.viewmodels.ProgressDialogManagerViewModel
import it.cammino.risuscito.viewmodels.SharedSnackBarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


open class CantoFragment : Fragment() {

    private lateinit var cambioAccordi: CambioAccordi
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var mDownload: Boolean = false

    private val noChangesTabBarre: Boolean
        get() = mCantiViewModel.notaCambio == mCantiViewModel.mCurrentCanto?.savedTab && mCantiViewModel.barreCambio == mCantiViewModel.mCurrentCanto?.savedBarre

    private lateinit var mSharedPrefs: SharedPreferences

    private lateinit var mRisuscitoDb: RisuscitoDatabase

    private lateinit var mDownloader: Downloader

    private val mCantiViewModel: PaginaRenderViewModel by viewModels()

    private val sharedSnackBarViewModel: SharedSnackBarViewModel by viewModels()

    private val mainActivityViewModel: MainActivityViewModel by viewModels({ requireActivity() })
    protected val progressDialogViewModel: ProgressDialogManagerViewModel by viewModels()
    private val downloaderViewModel: Downloader.DownloaderViewModel by viewModels({ requireActivity() })
    private val fabExpanded = mutableStateOf(false)
    private val cantoFabActionsList = mutableStateOf(cantoFabActions)
    private val tonalitaMenuExpanded = mutableStateOf(false)
    private val barreMenuExpanded = mutableStateOf(false)

    //    private val otherMenuExpanded = mutableStateOf(false)
    private val initialScale = mutableIntStateOf(0)
    private var url: String? = null
    private var personalUrl: String? = null
    private var localUrl: String? = null
    private val htmlContent = mutableStateOf(StringUtils.EMPTY)

    private val mediaPlayerVisible = mutableStateOf(true)
    private val playButtonAnimated = mutableStateOf(false)
    private val timeText = mutableStateOf("00:00")
    private val seekBarValue = mutableFloatStateOf(0f)
    private val seekBarMaxValue = mutableFloatStateOf(0f)
    private val seekBarEnabled = mutableStateOf(false)
    private val playButtonEnabled = mutableStateOf(false)

    private val seekBarScrollValue = mutableFloatStateOf(0.02f)
    private val mUpdateProgressTask =
        Runnable { if (!mExecutorService.isShutdown) updateProgress() }

    private var mScheduleFuture: ScheduledFuture<*>? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var resolveDeleteAudioConsent: ActivityResultLauncher<IntentSenderRequest>? = null

    private val pickAudio =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                Log.d(
                    TAG,
                    "${getString(R.string.file_selected)}: $it",
                )
                showSnackBar(getString(R.string.file_selected) + ": " + it)
                stopMedia()
                lifecycleScope.launch { insertLink(it.toString()) }
            }
        }

    private var mMainActivity: ThemeableActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? ThemeableActivity
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        mRisuscitoDb = RisuscitoDatabase.getInstance(context)
        mDownloader = Downloader(requireActivity())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

            setContent {
                RisuscitoTheme {

                    val scrollBehavior =
                        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                    val snackbarHostState = remember { SnackbarHostState() }

                    val showAlertDialog by mCantiViewModel.showAlertDialog.observeAsState()

                    val showProgressDialog by progressDialogViewModel.showProgressDialog.observeAsState()

                    val progressDialogProgress by progressDialogViewModel.progress.observeAsState()

                    val viewMode by remember { mCantiViewModel.viewMode }

                    val seekBarMode by remember { mCantiViewModel.seekBarMode }

                    val playButtonMode by remember { mCantiViewModel.playButtonMode }

                    val localmediaPlayerVisible by remember { mediaPlayerVisible }

                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(stringResource(R.string.canto_title_activity))
                                },
                                navigationIcon = {
                                    TooltipBox(
                                        positionProvider =
                                            TooltipDefaults.rememberTooltipPositionProvider(
                                                TooltipAnchorPosition.Above
                                            ),
                                        tooltip = { PlainTooltip { Text(stringResource(R.string.material_drawer_close)) } },
                                        state = rememberTooltipState(),
                                    ) {
                                        IconButton(onClick = { onOptionsItemSelected(ActionModeItem.CLOSE) }) {
                                            Icon(
                                                painter = painterResource(R.drawable.arrow_back_24px),
                                                contentDescription = stringResource(R.string.material_drawer_close)
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Material guidelines state 3 items max in compact, and 5 items max elsewhere.
                                    val maxItemCount =
                                        if (hasFiveMenuElements()) 5 else 3
                                    Box {
                                        AppBarRow(
                                            maxItemCount = maxItemCount,
                                            overflowIndicator = {
                                                TooltipBox(
                                                    positionProvider =
                                                        TooltipDefaults.rememberTooltipPositionProvider(
                                                            TooltipAnchorPosition.Above
                                                        ),
                                                    tooltip = { PlainTooltip { Text(stringResource(R.string.more)) } },
                                                    state = rememberTooltipState(),
                                                ) {
                                                    IconButton(onClick = { it.show() }) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.more_vert_24px),
                                                            contentDescription = stringResource(R.string.more),
                                                        )
                                                    }
                                                }
                                            },
                                        ) {
                                            cantoMenu.forEach { item ->
                                                clickableItem(
                                                    onClick = { onOptionsItemSelected(item) },
                                                    icon = {
                                                        Icon(
                                                            painter = painterResource(item.iconRes),
                                                            contentDescription = stringResource(item.label),
                                                        )
                                                    },
                                                    label = getString(item.label),
                                                )
                                            }
                                        }
                                        CantoDropDownMenu(
                                            menu = tontalitaDropDownMenu,
                                            menuExpanded = tonalitaMenuExpanded.value,
                                            onItemClick = { onDropDownMenuItemClick(it) }
                                        ) { tonalitaMenuExpanded.value = false }
                                        CantoDropDownMenu(
                                            menu = barreDropDownMenu,
                                            menuExpanded = barreMenuExpanded.value,
                                            onItemClick = { onDropDownMenuItemClick(it) }
                                        ) { barreMenuExpanded.value = false }
                                    }
                                },
                                scrollBehavior = scrollBehavior
                            )
                        },
                        floatingActionButton = {
                            Column {
                                RisuscitoFab(
                                    actions = cantoFabActionsList.value,
                                    expanded = fabExpanded.value,
                                    onExpandedChange = { fabExpanded.value = it },
                                    onFabActionClick = {
                                        fabExpanded.value = false
                                        onFabActionItemClicked(it)
                                    },
                                    mainIconRes = R.drawable.add_24px,
                                )
                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        },
                        floatingActionButtonPosition = FabPosition.End,
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState)
                        },
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (localmediaPlayerVisible) {
                                    AnimatedFadeContent(
                                        viewMode
                                    ) {
                                        when (it) {
                                            PaginaRenderViewModel.ViewMode.PLAY -> {

                                                MediaPlayerView(
                                                    seekbarViewMode = seekBarMode,
                                                    playButtonMode = playButtonMode,
                                                    playButtonAnimated = playButtonAnimated.value,
                                                    onPlayButtonClick = {
                                                        val controller =
                                                            MediaControllerCompat.getMediaController(
                                                                requireActivity()
                                                            )
                                                        val stateObj = controller.playbackState
                                                        val state = stateObj?.state
                                                            ?: PlaybackStateCompat.STATE_NONE
                                                        Log.d(
                                                            TAG,
                                                            "playPause: Button pressed, in state $state"
                                                        )

                                                        when (state) {
                                                            PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> {
                                                                playFromId(mCantiViewModel.idCanto.toString())
                                                            }

                                                            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                                                                pauseMedia()
                                                            }

                                                            PlaybackStateCompat.STATE_PAUSED -> {
                                                                playMedia()
                                                            }
                                                        }
                                                    },
                                                    playButtonEnabled = playButtonEnabled.value,
                                                    timeText = timeText.value,
                                                    seekBarValue = seekBarValue.floatValue,
                                                    seekBarMaxValue = seekBarMaxValue.floatValue,
                                                    seekBarEnabled = seekBarEnabled.value,
                                                    onValueChange = { newValue ->
                                                        stopSeekbarUpdate()
                                                        val controller =
                                                            MediaControllerCompat.getMediaController(
                                                                requireActivity()
                                                            )
                                                        controller?.transportControls?.seekTo(
                                                            newValue.toLong()
                                                        )
                                                        scheduleSeekbarUpdate()
                                                        seekBarValue.floatValue = newValue
                                                        updateTimeText(newValue)
                                                    }
                                                )
                                            }

                                            PaginaRenderViewModel.ViewMode.NO_INTERNET -> {
                                                StateNotificationView(
                                                    iconRes = R.drawable.wifi_off_24px,
                                                    textRes = R.string.no_connection
                                                )
                                            }

                                            PaginaRenderViewModel.ViewMode.NO_LINK -> {
                                                StateNotificationView(
                                                    iconRes = R.drawable.music_off_24px,
                                                    textRes = R.string.no_record
                                                )
                                            }
                                        }
                                    }
                                }
                                WebView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.dp)
                                        .weight(1f),
                                    canto = mCantiViewModel.mCurrentCanto,
                                    content = htmlContent.value,
                                    initialScale = initialScale.intValue,
                                    autoScroll = mCantiViewModel.scrollPlaying.value,
                                    scrollSpeed = seekBarScrollValue.floatValue,
                                    onScrollChange = { scrollX, scrollY ->
                                        Log.d(TAG, "onScrollChange: $scrollX, $scrollY")
                                        mCantiViewModel.scrollXValue = scrollX
                                        mCantiViewModel.scrollYValue = scrollY
                                    },
                                    onZoomChange = {
                                        Log.d(TAG, "onZoomChange: $it")
                                        mCantiViewModel.zoomValue = it
                                    }
                                )
                                ScrollPlayerView(
                                    playButtonAnimated = mCantiViewModel.scrollPlaying.value,
                                    onPlayButtonClick = {
                                        Log.d(
                                            TAG,
                                            "playPause: Button pressed: ${mCantiViewModel.scrollPlaying.value}"
                                        )
                                        if (mCantiViewModel.scrollPlaying.value) {
                                            showScrolling(false)
                                        } else {
                                            showScrolling(true)
                                        }
                                    },
                                    seekBarValue = seekBarScrollValue.floatValue,
                                    onValueChange = { newValue ->
                                        Log.d(TAG, "onValueChange: $newValue")
                                        seekBarScrollValue.floatValue = newValue
                                        mCantiViewModel.speedValue =
                                            (newValue * 100).toInt().toString()
                                        Log.d(
                                            TAG,
                                            "mCantiViewModel.speedValue: ${mCantiViewModel.speedValue}"
                                        )
                                    }
                                )

                            }
                        }
                    }

                    LaunchedEffect(sharedSnackBarViewModel.showSnackBar.value) {
                        if (sharedSnackBarViewModel.showSnackBar.value) {
                            val result = snackbarHostState
                                .showSnackbar(
                                    message = sharedSnackBarViewModel.snackbarMessage.value,
                                    duration = SnackbarDuration.Short,
                                    withDismissAction = true
                                )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {}
                                SnackbarResult.Dismissed -> {
                                    sharedSnackBarViewModel.showSnackBar.value = false
                                }
                            }
                        }
                    }

                    if (showAlertDialog == true) {
                        SimpleAlertDialog(
                            onDismissRequest = {
                                mCantiViewModel.showAlertDialog.postValue(false)
                                when (it) {
                                    SimpleDialogTag.SAVE_TAB -> {
                                        if (mCantiViewModel.scrollPlaying.value) {
                                            showScrolling(false)
                                        }
                                        saveZoom(andSpeedAlso = true, andSaveTabAlso = false)
                                        stopMedia()
                                        closeCanto()
                                    }

                                    else -> {}
                                }
                            },
                            onConfirmation = {
                                mCantiViewModel.showAlertDialog.postValue(false)
                                when (it) {
                                    SimpleDialogTag.DELETE_LINK -> {
                                        showSnackBar(getString(R.string.delink_delete))
                                        stopMedia()
                                        lifecycleScope.launch { deleteLink() }
                                    }

                                    SimpleDialogTag.DELETE_MP3 -> {
                                        localUrl?.let { url ->
                                            stopMedia()
                                            if ((activity?.isDefaultLocationPublic == true) && OSUtils.hasQ()) {
                                                val retrievedId =
                                                    getExternalMediaIdByName(requireContext(), url)
                                                if (retrievedId > 0) {
                                                    mCantiViewModel.toDelete =
                                                        ContentUris.withAppendedId(
                                                            MediaStore.Audio.Media
                                                                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                                                            retrievedId
                                                        )
                                                    Log.d(
                                                        TAG,
                                                        "DELETE_MP3 toDelete: ${mCantiViewModel.toDelete.toString()}"
                                                    )
                                                    try {
                                                        deleteAudio(mCantiViewModel.toDelete!!)
                                                        mCantiViewModel.toDelete = null
                                                    } catch (securityException: SecurityException) {
                                                        if (OSUtils.hasQ()) {
                                                            val recoverableSecurityException =
                                                                securityException as?
                                                                        RecoverableSecurityException
                                                                    ?: throw RuntimeException(
                                                                        securityException.message,
                                                                        securityException
                                                                    )
                                                            val intentSender =
                                                                recoverableSecurityException.userAction.actionIntent.intentSender
                                                            resolveDeleteAudioConsent?.launch(
                                                                IntentSenderRequest.Builder(
                                                                    intentSender
                                                                )
                                                                    .build()
                                                            )
                                                        } else {
                                                            throw RuntimeException(
                                                                securityException.message,
                                                                securityException
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                val fileToDelete = File(url)
                                                if (fileToDelete.delete()) {
                                                    if (fileToDelete.absolutePath.contains("/Risuscit")) {
                                                        // initiate media scan and put the new things into the path array to
                                                        // make the scanner aware of the location and the files you want to see
                                                        MediaScannerConnection.scanFile(
                                                            requireActivity().applicationContext,
                                                            arrayOf(fileToDelete.absolutePath),
                                                            null,
                                                            null
                                                        )
                                                    }
                                                    showSnackBar(getString(R.string.file_delete))
                                                } else {
                                                    showSnackBar(getString(R.string.error))
                                                }

                                            }
                                        }
                                        refreshCatalog()
                                        lifecycleScope.launch { checkRecordState() }
                                    }

                                    SimpleDialogTag.DOWNLINK_CHOOSE -> {
                                        if (activity?.isDefaultLocationPublic == true) {
                                            if (activity?.hasStorageAccess == true)
                                            // Have permission, do the thing!
                                                startDownload(true)
                                            else {
                                                mSharedPrefs.edit {
                                                    putString(
                                                        Utility.SAVE_LOCATION,
                                                        "0"
                                                    )
                                                }
                                                showSnackBar(getString(R.string.forced_private))
                                                startDownload(false)
                                            }
                                        } else
                                            startDownload(false)
                                    }

                                    SimpleDialogTag.ONLY_LINK -> {
                                        pickAudio.launch(arrayOf(MP3_MIME_TYPE))
                                    }

                                    SimpleDialogTag.SAVE_TAB -> {
                                        if (mCantiViewModel.scrollPlaying.value) {
                                            showScrolling(false)
                                        }
                                        saveZoom(andSpeedAlso = true, andSaveTabAlso = true)
                                        closeCanto()
                                    }

                                    else -> {}
                                }
                            },
                            dialogTitle = mCantiViewModel.dialogTitle.value.orEmpty(),
                            dialogText = mCantiViewModel.content.value.orEmpty(),
                            iconRes = mCantiViewModel.iconRes.value ?: 0,
                            confirmButtonText = mCantiViewModel.positiveButton.value.orEmpty(),
                            dismissButtonText = mCantiViewModel.negativeButton.value.orEmpty(),
                            dialogTag = mCantiViewModel.dialogTag
                        )
                    }

                    if (showProgressDialog == true) {
                        ProgressDialog(
                            dialogTitleRes = progressDialogViewModel.dialogTitleRes,
                            messageRes = progressDialogViewModel.messageRes.value ?: 0,
                            onDismissRequest = {
                                progressDialogViewModel.showProgressDialog.value = false
                                if (it == ProgressDialogTag.DOWNLOAD_MP3)
                                    mDownloader.cancel()
                            },
                            buttonTextRes = progressDialogViewModel.buttonTextRes,
                            indeterminate = progressDialogViewModel.indeterminate,
                            progress = progressDialogProgress ?: 0.5f
                        )
                    }

                    // After drawing main content, draw status bar protection
                    StatusBarProtection()

                    BackHandler {
                        Log.d(TAG, "BackHandler")
                        if (fabExpanded.value)
                            fabExpanded.value = false
                        else
                            onBackPressedAction()
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView()")
        stopSeekbarUpdate()
        mExecutorService.shutdown()
        showScrolling(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mCantiViewModel.idCanto == 0) {
            arguments?.let {
                if (mCantiViewModel.idCanto == 0) {
                    mCantiViewModel.idCanto = it.getInt(ARG_ID_CANTO)
                    mCantiViewModel.pagina = it.getString(ARG_NUM_PAGINA)
                    mCantiViewModel.inActivity = it.getBoolean(ARG_ON_ACTIVITY)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(
            TAG,
            "onCreateOptionsMenu - INTRO_PAGINARENDER: ${
                mSharedPrefs.getBoolean(
                    Utility.INTRO_PAGINARENDER,
                    false
                )
            }"
        )
//        if (!mSharedPrefs.getBoolean(Utility.INTRO_PAGINARENDER, false)) {
//            Handler(Looper.getMainLooper()).postDelayed(1500) {
//                playIntro(mediaPlayerVisible.value)
//            }
//        }

        Log.d(TAG, "LINGUA CTX: ${requireContext().systemLocale.language}")
        Log.d(TAG, "LINGUA BASE: ${requireActivity().systemLocale.language}")
        cambioAccordi = CambioAccordi(requireContext())

        try {
            Firebase.crashlytics.setCustomKeys(
                CustomKeysAndValues.Builder().putString(
                    "pagina_canto",
                    mCantiViewModel.pagina.orEmpty()
                ).putString("lingua", requireContext().systemLocale.language).build()
            )

            mCantiViewModel.primaNota =
                mCantiViewModel.primaNota.ifEmpty {
                    CambioAccordi.recuperaPrimoAccordo(
                        resources.openRawResource(
                            Utility.getResId(
                                mCantiViewModel.pagina,
                                R.raw::class.java
                            )
                        ),
                        requireContext().systemLocale.language
                    )
                }
            mCantiViewModel.primoBarre =
                mCantiViewModel.primoBarre.ifEmpty {
                    cambioAccordi.recuperaBarre(
                        resources.openRawResource(
                            Utility.getResId(
                                mCantiViewModel.pagina,
                                R.raw::class.java
                            )
                        ),
                        requireContext().systemLocale.language
                    )
                }
        } catch (e: IOException) {
            Log.e(TAG, e.localizedMessage, e)
        }

//        showScrolling(false)

        lifecycleScope.launch { retrieveData() }

        if (savedInstanceState == null)
            mCantiViewModel.mostraAudio = mSharedPrefs.getBoolean(Utility.SHOW_AUDIO, true)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        resolveDeleteAudioConsent = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mCantiViewModel.toDelete?.let {
                    deleteAudio(it)
                    mCantiViewModel.toDelete = null
                }
            } else {
                showSnackBar(getString(R.string.external_storage_denied))
            }
        }

        subscribeUiChange()

    }

    private fun subscribeUiChange() {
        mainActivityViewModel.lastPlaybackState.value?.let {
            updatePlayBackStatus(it)
        }

        mainActivityViewModel.lastPlaybackState.observe(viewLifecycleOwner) {
            updatePlayBackStatus(it)
        }

        mainActivityViewModel.medatadaCompat.observe(viewLifecycleOwner) {
            updateSeekBarValueTo(
                it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toFloat()
            )
            enableSeekbar(true)
        }

        mainActivityViewModel.playerConnected.observe(viewLifecycleOwner) {
            if (mainActivityViewModel.lastPlaybackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
                scheduleSeekbarUpdate()
            }
            showPlaying(mainActivityViewModel.lastPlaybackState.value?.state == PlaybackStateCompat.STATE_PLAYING)
            enableSeekbar(
                mainActivityViewModel.lastPlaybackState.value?.state == PlaybackStateCompat.STATE_PLAYING
                        || mainActivityViewModel.lastPlaybackState.value?.state == PlaybackStateCompat.STATE_PAUSED
            )

            mainActivityViewModel.medatadaCompat.value?.let {
                Log.d(
                    TAG,
                    "onConnected: duration ${it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)}"
                )
                updateSeekBarValueTo(
                    it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toFloat()
                )
            }

            Log.d(
                TAG,
                "onConnected: mLastPlaybackState.getPosition() ${mainActivityViewModel.lastPlaybackState.value?.position}"
            )
            updateSeekBarValue(
                mainActivityViewModel.lastPlaybackState.value?.position?.toFloat() ?: 0F
            )
        }

        mainActivityViewModel.catalogRefreshReady.observe(viewLifecycleOwner) {
            mCantiViewModel.retrieveDone = it
            showPlaying(false)
            playButtonEnabled.value = it
            stopMedia()
        }

        downloaderViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "downloaderViewModel state $it")
            if (!downloaderViewModel.handled) {
                when (it) {
                    is DownloadState.Progress -> {
                        Log.d(TAG, "DownloadListener update: ${it.progress}")
                        Log.d(TAG, "DownloadListener update FLOAT: ${it.progress.toFloat() / 100f}")
                        progressDialogViewModel.progress.value = it.progress.toFloat() / 100f
                    }

                    is DownloadState.Completed -> {
                        Log.d(TAG, "DownloadListener onComplete")
                        progressDialogViewModel.showProgressDialog.value = false
                        // initiate media scan and put the new things into the path array to
                        // make the scanner aware of the location and the files you want to see
                        if ((activity?.isDefaultLocationPublic == true) && !OSUtils.hasQ())
                            mediaScan(requireContext(), url.orEmpty())
                        showSnackBar(getString(R.string.download_completed))
                        stopMedia()
                        refreshCatalog()
                        lifecycleScope.launch { checkRecordState() }
                    }

                    is DownloadState.Error -> {
                        Log.d(TAG, "DownloadListener onError: ${it.message}")
                        downloaderViewModel.handled = true
                        progressDialogViewModel.showProgressDialog.value = false
                        showSnackBar(" ${getString(R.string.download_error)}: $it.message")
                    }
                }
            }
        }
    }

    private fun updatePlayBackStatus(state: PlaybackStateCompat) {
        Log.d(TAG, "updatePlayBackStatus - state: ${state.state}")
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED -> {
                stopSeekbarUpdate()
                showPlaying(false)
                enableSeekbar(true)
            }

            PlaybackStateCompat.STATE_STOPPED -> {
                stopSeekbarUpdate()
                updateSeekBarValue(0F)
                enableSeekbar(false)
                showPlaying(false)
            }

            PlaybackStateCompat.STATE_ERROR -> {

                mCantiViewModel.seekBarMode.value = PaginaRenderViewModel.SeekBarMode.SEEKBAR
                stopSeekbarUpdate()
                updateSeekBarValue(0F)
                enableSeekbar(false)
                showPlaying(false)
                Log.e(TAG, "onPlaybackStateChanged: " + state.errorMessage)
                showSnackBar(state.errorMessage.toString())
            }

            PlaybackStateCompat.STATE_PLAYING -> {

                mCantiViewModel.seekBarMode.value = PaginaRenderViewModel.SeekBarMode.SEEKBAR
                scheduleSeekbarUpdate()
                showPlaying(true)
                enableSeekbar(true)
            }

            else -> {
                Log.i(TAG, "Non gestito")
            }
        }
    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        if (mCantiViewModel.notaCambio.isEmpty()
            || mCantiViewModel.mCurrentCanto?.savedTab == null
            || mCantiViewModel.barreCambio.isEmpty()
            || mCantiViewModel.mCurrentCanto?.savedBarre == null
            || noChangesTabBarre
        ) {
            if (mCantiViewModel.scrollPlaying.value) {
                showScrolling(false)
            }
            saveZoom(andSpeedAlso = true, andSaveTabAlso = false)
            stopMedia()
            closeCanto()
        } else {
            mCantiViewModel.dialogTag = SimpleDialogTag.SAVE_TAB
            mCantiViewModel.dialogTitle.value = getString(R.string.dialog_save_tab_title)
            mCantiViewModel.iconRes.value = R.drawable.save_24px
            mCantiViewModel.content.value = getString(R.string.dialog_save_tab)
            mCantiViewModel.positiveButton.value = getString(R.string.save_exit_confirm)
            mCantiViewModel.negativeButton.value = getString(R.string.discard_exit_confirm)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        enableMusicControls(mCantiViewModel.mostraAudio)
    }

    private fun closeCanto() {
        if (mCantiViewModel.inActivity) {
            mMainActivity?.finishAfterTransitionWrapper()
        } else {
            mMainActivity?.closeCanto()
        }
    }

    // recupera e setta il record per la registrazione
    private fun getRecordLink() {
        url = if (!mCantiViewModel.mCurrentCanto?.link.isNullOrEmpty())
            getString(Utility.getResId(mCantiViewModel.mCurrentCanto?.link, R.string::class.java))
        else
            StringUtils.EMPTY

        val mDao = mRisuscitoDb.localLinksDao()
        val localLink = mDao.getLocalLinkByCantoId(mCantiViewModel.idCanto)

        personalUrl = localLink?.localPath.orEmpty()
    }

    private fun saveZoom(andSpeedAlso: Boolean, andSaveTabAlso: Boolean) {
        mCantiViewModel.mCurrentCanto?.let {
            it.zoom = mCantiViewModel.zoomValue
            it.scrollX = mCantiViewModel.scrollXValue
            it.scrollY = mCantiViewModel.scrollYValue

            if (andSpeedAlso) it.savedSpeed = mCantiViewModel.speedValue?.ifEmpty { "2" }

            if (andSaveTabAlso) {
                it.savedBarre = mCantiViewModel.barreCambio
                it.savedTab = mCantiViewModel.notaCambio
            }

            lifecycleScope.launch { updateCanto(0) }
        }
    }

    private fun cambiaAccordi(
        conversione: HashMap<String, String>?,
        barre: String?,
        conversioneMin: HashMap<String, String>?
    ): String {
        val cantoTrasportato = StringBuffer()

        var barreScritto = false

        try {
            val br = BufferedReader(
                InputStreamReader(
                    resources.openRawResource(
                        Utility.getResId(
                            mCantiViewModel.pagina,
                            R.raw::class.java
                        )
                    ), ECONDING_UTF8
                )
            )

            var line: String? = br.readLine()

            val language = requireContext().systemLocale.language

            val pattern: Pattern
            var patternMinore: Pattern? = null

            when (language) {
//                LANGUAGE_ITALIAN, LANGUAGE_TURKISH -> pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si")
                LANGUAGE_UKRAINIAN -> {
                    pattern = Pattern.compile("Cis|C|D|Eb|E|Fis|F|Gis|G|A|B|H")
                    // inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
                    patternMinore = Pattern.compile("cis|c|d|eb|e|fis|f|gis|g|a| b|h")
                }

                LANGUAGE_POLISH -> {
                    pattern = Pattern.compile("Cis|C|D|Dis|E|Fis|F|Gis|G|A|B|H")
                    // inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
                    patternMinore = Pattern.compile("cis|c|d|dis|e|fis|f|gis|g|a| b|h")
                }

                LANGUAGE_ENGLISH -> pattern = Pattern.compile("C#|C|D|Eb|E|F#|F|G#|G|A|Bb|B")
                else -> pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si")
            }

            // serve per segnarsi se si è già evidenziato il primo accordo del testo
            var notaHighlighed = false
            var insidePre = false

            while (line != null) {
                Log.v(TAG, "RIGA DA ELAB: $line")
                if ((line.startsWith("</FONT><FONT COLOR=\"#A13F3C\">"))
                    && !line.contains("<H2>")
                    && !line.contains("<H4>")
                    && insidePre
                ) {
                    if (language.equals(LANGUAGE_UKRAINIAN, ignoreCase = true) || language.equals(
                            LANGUAGE_ENGLISH,
                            ignoreCase = true
                        ) || language.equals(LANGUAGE_POLISH, ignoreCase = true)
                    ) {
                        line = line.replace("</FONT><FONT COLOR=\"#A13F3C\">".toRegex(), "<K>")
                        line = line.replace("</FONT><FONT COLOR=\"#000000\">".toRegex(), "<K2>")
                    }
                    val matcher = pattern.matcher(line)
                    val sb = StringBuffer()
                    val sb2 = StringBuffer()
                    while (matcher.find()) matcher.appendReplacement(
                        sb, conversione?.get(
                            matcher.group(0).orEmpty()
                        ).orEmpty()
                    )
                    matcher.appendTail(sb)
                    if (language.equals(LANGUAGE_UKRAINIAN, ignoreCase = true) || language.equals(
                            LANGUAGE_POLISH,
                            ignoreCase = true
                        )
                    ) {
                        val matcherMin = patternMinore?.matcher(sb.toString())
                        while (matcherMin?.find() == true)
                            matcherMin.appendReplacement(
                                sb2, conversioneMin?.get(
                                    matcherMin.group(0).orEmpty()
                                ).orEmpty()
                            )
                        matcherMin?.appendTail(sb2)
                        line = sb2.toString()
                        //                        Log.d(TAG, "RIGA ELAB 1: " + line);
                        //                        Log.d(TAG, "notaHighlighed: " + notaHighlighed);
                        //                        Log.d(TAG, "notaCambio: " + notaCambio);
                        //                        Log.d(TAG, "primaNota: " + primaNota);
                        if (!notaHighlighed) {
                            if (!mCantiViewModel.primaNota.equals(
                                    mCantiViewModel.notaCambio,
                                    ignoreCase = true
                                )
                            ) {
                                if (Utility.isLowerCase(mCantiViewModel.primaNota[0])) {
                                    var notaCambioMin = mCantiViewModel.notaCambio
                                    notaCambioMin = if (notaCambioMin.length == 1)
                                        notaCambioMin.lowercase(systemLocale)
                                    else
                                        notaCambioMin.take(1)
                                            .lowercase(systemLocale) + notaCambioMin.substring(
                                            1
                                        )
                                    line = line.replaceFirst(
                                        notaCambioMin.toRegex(),
                                        "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">$notaCambioMin</SPAN>"
                                    )
                                } else
                                    line = line.replaceFirst(
                                        mCantiViewModel.notaCambio.toRegex(),
                                        "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">"
                                                + mCantiViewModel.notaCambio
                                                + "</SPAN>"
                                    )
                                notaHighlighed = true
                            }
                        }
                        //                        Log.d(TAG, "RIGA ELAB 2: " + line);
                        line = line.replace("<K>".toRegex(), "</FONT><FONT COLOR='#A13F3C'>")
                        line = line.replace("<K2>".toRegex(), "</FONT><FONT COLOR='#000000'>")
                        //                        Log.d(TAG, "RIGA ELAB 3: " + line);
                    } else {
                        line = sb.toString()
                        if (!notaHighlighed) {
                            if (!mCantiViewModel.primaNota.equals(
                                    mCantiViewModel.notaCambio,
                                    ignoreCase = true
                                )
                            ) {
                                line = line.replaceFirst(
                                    mCantiViewModel.notaCambio.toRegex(),
                                    "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">"
                                            + mCantiViewModel.notaCambio
                                            + "</SPAN>"
                                )
                                notaHighlighed = true
                            }
                        }

                        if (language.equals(LANGUAGE_ENGLISH, ignoreCase = true)) {
                            line = line.replace("<K>".toRegex(), "</FONT><FONT COLOR='#A13F3C'>")
                            line = line.replace("<K2>".toRegex(), "</FONT><FONT COLOR='#000000'>")
                        }
                    }
                    cantoTrasportato.append(line)
                    cantoTrasportato.append("\n")
                } else {
                    if (line.contains(PRE_START)) {
                        if (barre != null && barre != "0") {
                            if (!barreScritto) {
                                val oldLine: String = if (!barre.equals(
                                        mCantiViewModel.primoBarre,
                                        ignoreCase = true
                                    )
                                ) {
                                    ("<H4><SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\"><FONT COLOR=\"#000000\"><I>"
                                            + getString(R.string.barre_al_tasto, barre)
                                            + "</I></FONT></SPAN></H4>")
                                } else {
                                    ("<H4><FONT COLOR=\"#000000\"><I>"
                                            + getString(R.string.barre_al_tasto, barre)
                                            + "</I></FONT></H4>")
                                }
                                cantoTrasportato.append(oldLine)
                                cantoTrasportato.append("\n")
                                barreScritto = true
                            }
                        }
                        cantoTrasportato.append(line)
                        cantoTrasportato.append("\n")
                    } else {
                        if (!line.contains(getString(R.string.barre_search_string)) || !line.contains(
                                "<H4>"
                            )
                        ) {
                            cantoTrasportato.append(line)
                            cantoTrasportato.append("\n")
                        }
                    }
                }
                if (line.contains(PRE_START)) insidePre = true
                if (line.contains(PRE_END)) insidePre = false
                line = br.readLine()
            }
            br.close()
            Log.i(TAG, "cambiaAccordi cantoTrasportato -> $cantoTrasportato")
            return cantoTrasportato.toString()
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage, e)
            return StringUtils.EMPTY
        }

    }

    private fun startDownload(isExternal: Boolean) {
        Log.d(TAG, "startDownload - isExternal: $isExternal")
        Log.d(
            TAG,
            "startDownload - isExternalStorageWritable: ${Utility.isExternalStorageWritable}"
        )
        if ((isExternal && Utility.isExternalStorageWritable) || !isExternal) {
            val localFilePath =
                mMainActivity?.filesDir?.toString().orEmpty() + "/" + Utility.filterMediaLink(url)
            progressDialogViewModel.dialogTag = ProgressDialogTag.DOWNLOAD_MP3
            progressDialogViewModel.indeterminate = false
            progressDialogViewModel.progress.value = 0f
            progressDialogViewModel.dialogTitleRes = R.string.save_file
            progressDialogViewModel.dialogIconRes = R.drawable.file_download_24px
            progressDialogViewModel.messageRes.value = R.string.download_running
            progressDialogViewModel.buttonTextRes = R.string.cancel
            progressDialogViewModel.showProgressDialog.value = true
            lifecycleScope.launch(Dispatchers.IO) {
                mDownloader.startSaving(
                    url, if (isExternal) getExternalLink(
                        url.orEmpty()
                    ) else localFilePath, isExternal
                )
            }
        } else {
            showSnackBar(getString(R.string.no_memory_writable))
        }
    }

    private fun showPlaying(started: Boolean) {
        Log.d(TAG, "showPlaying: $started")
        mCantiViewModel.playButtonMode.value =
            if (mCantiViewModel.retrieveDone) PaginaRenderViewModel.PlayButtonMode.PLAY else PaginaRenderViewModel.PlayButtonMode.LOADING
        playButtonAnimated.value = started
    }

    private fun showScrolling(scrolling: Boolean) {
        mCantiViewModel.scrollPlaying.value = scrolling
    }

//    private fun playIntro(isFull: Boolean) {
//        binding.musicControls.isVisible = true
//        enableMusicControls(true)
//        val colorOnPrimary = MaterialColors.getColor(
//            requireContext(),
//            com.google.android.material.R.attr.colorOnPrimary,
//            TAG
//        )
//        var id = 1
//        TapTargetSequence(requireActivity()).apply {
//            continueOnCancel(true)
//            target(
//                TapTarget.forToolbarMenuItem(
//                    binding.risuscitoToolbarCanto,
//                    R.id.tonalita,
//                    getString(R.string.action_tonalita),
//                    getString(R.string.sc_tonalita_desc)
//                )
//                    // All options below are optional
//                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                    .titleTypeface(mMediumFont) // Specify a typeface for the text
//                    .titleTypeface(mMediumFont) // Specify a typeface for the text
//                    .titleTextColorInt(colorOnPrimary)
//                    .textColorInt(colorOnPrimary)
//                    .setForceCenteredTarget(true)
//                    .id(id++)
//            )
//            target(
//                TapTarget.forToolbarMenuItem(
//                    binding.risuscitoToolbarCanto,
//                    R.id.barre,
//                    getString(R.string.action_barre),
//                    getString(R.string.sc_barre_desc)
//                )
//                    // All options below are optional
//                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                    .titleTypeface(mMediumFont) // Specify a typeface for the text
//                    .titleTextColorInt(colorOnPrimary)
//                    .textColorInt(colorOnPrimary)
//                    .setForceCenteredTarget(true)
//                    .id(id++)
//            )
//            if (isFull) {
//                target(
//                    TapTarget.forView(
//                        binding.playSong,
//                        getString(R.string.sc_audio_title),
//                        getString(R.string.sc_audio_desc)
//                    )
//                        // All options below are optional
//                        .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                        .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                        .titleTypeface(mMediumFont) // Specify a typeface for the text
//                        .titleTextColorInt(colorOnPrimary)
//                        .textColorInt(colorOnPrimary)
//                        .setForceCenteredTarget(true)
//                        .id(id++)
//                )
//            }
//            target(
//                TapTarget.forView(
//                    binding.playScroll,
//                    getString(R.string.sc_scroll_title),
//                    getString(R.string.sc_scroll_desc)
//                )
//                    // All options below are optional
//                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                    .titleTypeface(mMediumFont) // Specify a typeface for the text
//                    .titleTextColorInt(colorOnPrimary)
//                    .textColorInt(colorOnPrimary)
//                    .setForceCenteredTarget(true)
//                    .id(id++)
//            )
//            target(
//                TapTarget.forToolbarOverflow(
//                    binding.risuscitoToolbarCanto,
//                    getString(R.string.showcase_end_title),
//                    getString(R.string.showcase_help_general)
//                )
//                    // All options below are optional
//                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                    .titleTypeface(mMediumFont) // Specify a typeface for the text
//                    .titleTextColorInt(colorOnPrimary)
//                    .textColorInt(colorOnPrimary)
//                    .setForceCenteredTarget(true)
//                    .id(id)
//            )
//            listener(
//                object :
//                    TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
//                    override fun onSequenceFinish() {
//                        mSharedPrefs.edit { putBoolean(Utility.INTRO_PAGINARENDER, true) }
//                        enableMusicControls(mCantiViewModel.mostraAudio)
////                        binding.musicControls.isVisible = mCantiViewModel.mostraAudio
//                    }
//
//                    override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
//                        // no-op
//                    }
//
//                    override fun onSequenceCanceled(tapTarget: TapTarget) {
//                        mSharedPrefs.edit { putBoolean(Utility.INTRO_PAGINARENDER, true) }
////                        binding.musicControls.isVisible = mCantiViewModel.mostraAudio
//                        enableMusicControls(mCantiViewModel.mostraAudio)
//                    }
//                })
//        }.start()
//    }

    private fun playMedia() {
        Log.d(TAG, "playMedia: ")
        val controller = MediaControllerCompat.getMediaController(requireActivity())
        controller?.transportControls?.play()
    }

    private fun pauseMedia() {
        Log.d(TAG, "pauseMedia: ")
        val controller = MediaControllerCompat.getMediaController(requireActivity())
        controller?.transportControls?.pause()
    }

    private fun stopMedia() {
        Log.d(TAG, "stopMedia: ")
        if (mainActivityViewModel.lastPlaybackState.value?.state != PlaybackStateCompat.STATE_STOPPED) {
            val controller = MediaControllerCompat.getMediaController(requireActivity())
            controller?.transportControls?.stop()
        }
    }

    private fun playFromId(id: String) {
        mCantiViewModel.seekBarMode.value = PaginaRenderViewModel.SeekBarMode.LOADINGBAR
        val controller = MediaControllerCompat.getMediaController(requireActivity())
        controller?.transportControls?.playFromMediaId(id, null)
    }

    private fun refreshCatalog() {
        Log.d(TAG, "refreshCatalog")
        playButtonEnabled.value = false
        val controller = MediaControllerCompat.getMediaController(requireActivity())
        controller?.transportControls?.sendCustomAction(MusicService.ACTION_REFRESH, null)
    }

    private fun scheduleSeekbarUpdate() {
        stopSeekbarUpdate()
        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleWithFixedDelay(
                { mHandler.post(mUpdateProgressTask) },
                PROGRESS_UPDATE_INITIAL_INTERVAL,
                PROGRESS_UPDATE_INTERNAL,
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun updateTimeText(value: Float) {
        val time = String.format(
            systemLocale,
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(value.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(value.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    value.toLong()
                )
            )
        )
        timeText.value = time
    }

    private fun stopSeekbarUpdate() {
        mScheduleFuture?.cancel(false)
    }

    private fun updateProgress() {
        if (mainActivityViewModel.lastPlaybackState.value == null) {
            return
        }
        var currentPosition = mainActivityViewModel.lastPlaybackState.value?.position ?: 0
        if (mainActivityViewModel.lastPlaybackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            val timeDelta =
                SystemClock.elapsedRealtime() - (mainActivityViewModel.lastPlaybackState.value?.lastPositionUpdateTime
                    ?: 0L)
            currentPosition += (timeDelta.toInt() * (mainActivityViewModel.lastPlaybackState.value?.playbackSpeed
                ?: 0F)).toLong()
        }
        enableSeekbar(true)
        updateSeekBarValue(currentPosition.toFloat())
    }

    private fun enableSeekbar(enabled: Boolean) {
        Log.d(TAG, "enableSeekbar: $enabled")
        seekBarEnabled.value = enabled
    }

    private fun updateSeekBarValue(value: Float) {
        Log.d(TAG, "updateSeekBarValue: $value")
        if (value < seekBarMaxValue.floatValue) {
            seekBarValue.floatValue = value
            updateTimeText(value)
        }
    }

    private fun updateSeekBarValueTo(valueTo: Float) {
        Log.d(TAG, "updateSeekBarValueTo: $valueTo")
        if (valueTo > 0F) {
            seekBarMaxValue.floatValue = valueTo
        }
    }

    private fun enableMusicControls(enabled: Boolean) {
        Log.d(TAG, "enableMusicControls: $enabled")
        mediaPlayerVisible.value = enabled
    }

    private fun checkRecordsState() {
        // c'è la registrazione online
        if (!url.isNullOrEmpty()) {
            // controllo se ho scaricato un file in locale
            localUrl = if (mMainActivity?.isDefaultLocationPublic == true) {
                if (mMainActivity?.hasStorageAccess == true) {
                    // Have permission, do the thing!
                    retrieveMediaFileLink(requireActivity(), url, true)
                } else {
                    mSharedPrefs.edit { putString(Utility.SAVE_LOCATION, "0") }
                    showSnackBar(getString(R.string.external_storage_denied))
                    retrieveMediaFileLink(requireActivity(), url, false)
                }
            } else
                retrieveMediaFileLink(requireActivity(), url, false)

            mDownload = !(localUrl.isNullOrEmpty() && personalUrl.isNullOrEmpty())

            // almeno una registrazione c'è, quindi nascondo il messaggio di binding.noRecords
            // mostra i pulsanti per il lettore musicale se ho una registrazione locale oppure se sono
            // online, altrimenti mostra il messaggio di mancata connessione
            Log.d(TAG, "isOnline ${context?.isOnline == true}")

            mCantiViewModel.viewMode.value =
                if ((context?.isOnline == true) || mDownload) PaginaRenderViewModel.ViewMode.PLAY else PaginaRenderViewModel.ViewMode.NO_INTERNET
        } else {
            mDownload = !personalUrl.isNullOrEmpty()
            // Se c'è una registrazione locale mostro i pulsanti
            mCantiViewModel.viewMode.value =
                if (mDownload) PaginaRenderViewModel.ViewMode.PLAY else PaginaRenderViewModel.ViewMode.NO_LINK
        }// NON c'è la registrazione online
        initFabOptions()
    }

    private suspend fun retrieveData() {
        val mDao = mRisuscitoDb.cantoDao()
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mCantiViewModel.mCurrentCanto = mDao.getCantoById(mCantiViewModel.idCanto)
            getRecordLink()
        }
        if (mCantiViewModel.mCurrentCanto?.savedTab.isNullOrEmpty()) {
            if (mCantiViewModel.notaCambio.isEmpty()) {
                mCantiViewModel.notaCambio = mCantiViewModel.primaNota
                mCantiViewModel.mCurrentCanto?.savedTab = mCantiViewModel.notaCambio
            } else
                mCantiViewModel.mCurrentCanto?.savedTab = mCantiViewModel.primaNota
        } else if (mCantiViewModel.notaCambio.isEmpty())
            mCantiViewModel.notaCambio = mCantiViewModel.mCurrentCanto?.savedTab.orEmpty()

        if (mCantiViewModel.mCurrentCanto?.savedBarre == null) {
            if (mCantiViewModel.barreCambio.isEmpty()) {
                mCantiViewModel.barreCambio = mCantiViewModel.primoBarre
                mCantiViewModel.mCurrentCanto?.savedBarre = mCantiViewModel.barreCambio
            } else
                mCantiViewModel.mCurrentCanto?.savedBarre = mCantiViewModel.primoBarre
        } else {
            //	    	Log.i("BARRESALVATO", barreSalvato);
            if (mCantiViewModel.barreCambio.isEmpty())
                mCantiViewModel.barreCambio = mCantiViewModel.mCurrentCanto?.savedBarre.orEmpty()
        }

        val convMap =
            cambioAccordi.diffSemiToni(mCantiViewModel.primaNota, mCantiViewModel.notaCambio)
        var convMin: HashMap<String, String>? = null
        if (requireContext().systemLocale.language.equals(
                LANGUAGE_UKRAINIAN,
                ignoreCase = true
            ) || requireContext().systemLocale.language.equals(LANGUAGE_POLISH, ignoreCase = true)
        )
            convMin =
                cambioAccordi.diffSemiToniMin(mCantiViewModel.primaNota, mCantiViewModel.notaCambio)
        if (convMap != null) {
            htmlContent.value = cambiaAccordi(convMap, mCantiViewModel.barreCambio, convMin)
        } else
            htmlContent.value =
                resources.readTextFromResource(
                    mCantiViewModel.pagina
                        ?: NO_CANTO
                )

        mCantiViewModel.mCurrentCanto?.let {
            if (it.zoom > 0)
                initialScale.intValue = it.zoom
        }

        if (mCantiViewModel.speedValue.orEmpty().isEmpty()) {
            try {
                mCantiViewModel.speedValue =
                    (mCantiViewModel.mCurrentCanto?.savedSpeed.orEmpty().ifEmpty { "2" })
            } catch (e: NumberFormatException) {
                Log.e(TAG, "savedSpeed ${mCantiViewModel.mCurrentCanto?.savedSpeed}", e)
                mCantiViewModel.speedValue = "2"
            }
        }
        seekBarScrollValue.floatValue =
            mCantiViewModel.speedValue.orEmpty().ifEmpty { "2" }.toFloat().div(100)

        //	    Log.i(this.getClass().toString(), "scrollPlaying? " + scrollPlaying);
        if (mCantiViewModel.scrollPlaying.value) {
            showScrolling(true)
        }
        checkRecordsState()
    }

    private suspend fun insertLink(path: String) {
        val mDao = mRisuscitoDb.localLinksDao()
        val linkToInsert = LocalLink()
        linkToInsert.idCanto = mCantiViewModel.idCanto
        linkToInsert.localPath = path
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.insertLocalLink(linkToInsert)
            getRecordLink()
        }
        refreshCatalog()
        checkRecordsState()
    }

    private suspend fun checkRecordState() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            getRecordLink()
        }
        checkRecordsState()
    }

    private suspend fun deleteLink() {
        val mDao = mRisuscitoDb.localLinksDao()
        val linkToDelete = LocalLink()
        linkToDelete.idCanto = mCantiViewModel.idCanto
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.deleteLocalLink(linkToDelete)
            getRecordLink()
        }
        refreshCatalog()
        checkRecordsState()
    }

    private suspend fun updateFavorite() {
        val mDao = mRisuscitoDb.cantoDao()
        mCantiViewModel.mCurrentCanto?.let {
            it.favorite = if (mCantiViewModel.mCurrentCanto?.favorite == 1) 0 else 1
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.updateCanto(it)
            }
            showSnackBar(getString(if (it.favorite == 1) R.string.favorite_added else R.string.favorite_removed))
            initFabOptions()
        }
    }

    private suspend fun updateCanto(option: Int) {
        val mDao = mRisuscitoDb.cantoDao()
        mCantiViewModel.mCurrentCanto?.let {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.updateCanto(it)
            }
            if (option != 0) {
                showSnackBar(getString(if (option == 1) R.string.tab_saved else R.string.barre_saved))
            }
        }
    }


    private fun initFabOptions() {
        val newList = ArrayList(cantoFabActions)

        newList.apply {
            var index = indexOfFirst { it.id == FabActionItem.SOUND.id }
            add(index, removeAt(index).copy(mCantiViewModel.mostraAudio))

            if (mDownload) {
                index = indexOfFirst { it.id == FabActionItem.DELETEFILE.id }
                add(index, removeAt(index).copy(!personalUrl.isNullOrEmpty()))

                index = indexOfFirst { it.id == FabActionItem.SAVEFILE.id }
                removeAt(index)

                index = indexOfFirst { it.id == FabActionItem.LINKFILE.id }
                removeAt(index)


            } else {
                index = indexOfFirst { it.id == FabActionItem.DELETEFILE.id }
                removeAt(index)

                if (url.isNullOrEmpty()) {
                    index = indexOfFirst { it.id == FabActionItem.SAVEFILE.id }
                    removeAt(index)
                }
            }

            index = indexOfFirst { it.id == FabActionItem.FAVORITE.id }
            add(index, removeAt(index).copy(mCantiViewModel.mCurrentCanto?.favorite == 1))

        }

        cantoFabActionsList.value = newList

    }

    private suspend fun exportPdf() {
        progressDialogViewModel.dialogTag = ProgressDialogTag.EXPORT_PDF
        progressDialogViewModel.indeterminate = true
        progressDialogViewModel.dialogTitleRes = R.string.action_exp_pdf
        progressDialogViewModel.dialogIconRes = R.drawable.picture_as_pdf_24px
        progressDialogViewModel.messageRes.value = R.string.export_running
        progressDialogViewModel.buttonTextRes = 0
        progressDialogViewModel.showProgressDialog.value = true
        val pdfOutput = PdfExporter(requireContext()).exportPdf(htmlContent.value)
        delay(1000)
        progressDialogViewModel.showProgressDialog.value = false
        if (pdfOutput.isError) {
            showSnackBar("${getString(R.string.error)}: ${pdfOutput.errorMessage}")
        } else {
            val file = File(pdfOutput.pdfPath)
            val target = Intent(Intent.ACTION_VIEW)
            val pdfUri = FileProvider.getUriForFile(
                requireContext(), "it.cammino.risuscito.fileprovider", file
            )
            Log.d(TAG, "pdfUri: $pdfUri")
            target.setDataAndType(pdfUri, "application/pdf")
            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
            val intent2 = Intent.createChooser(target, getString(R.string.open_pdf))
            try {
                startActivity(intent2)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Error:", e)
                showSnackBar(getString(R.string.no_pdf_reader))
            }
        }
    }

    private fun deleteAudio(toDelete: Uri) {
        if (requireContext().contentResolver.delete(toDelete, null, null) > 0) {
            showSnackBar(getString(R.string.file_delete))
            refreshCatalog()
            lifecycleScope.launch { checkRecordState() }
        } else {
            showSnackBar(getString(R.string.error))
        }
    }

    private fun showSnackBar(message: String) {
        sharedSnackBarViewModel.snackbarMessage.value = message
        sharedSnackBarViewModel.showSnackBar.value = true
    }

    private fun onOptionsItemSelected(item: ActionModeItem) {
        when (item) {
            ActionModeItem.CLOSE -> onBackPressedAction()
            ActionModeItem.TONALITA -> tonalitaMenuExpanded.value = true
            ActionModeItem.BARRE -> barreMenuExpanded.value = true
            ActionModeItem.EXPORT_PDF -> lifecycleScope.launch { exportPdf() }
            else -> {}
        }
    }

    private fun onDropDownMenuItemClick(item: DropDownMenuItem) {
        when (item) {
            DropDownMenuItem.TONALITA_SALVA -> {
                if (!mCantiViewModel.mCurrentCanto?.savedTab.equals(
                        mCantiViewModel.notaCambio,
                        ignoreCase = true
                    )
                ) {
                    mCantiViewModel.mCurrentCanto?.savedTab = mCantiViewModel.notaCambio
                    lifecycleScope.launch { updateCanto(1) }
                } else {
                    showSnackBar(getString(R.string.tab_not_saved))
                }
            }

            DropDownMenuItem.TONALITA_RESET -> {
                mCantiViewModel.notaCambio = mCantiViewModel.primaNota
                val convMap = cambioAccordi.diffSemiToni(
                    mCantiViewModel.primaNota,
                    mCantiViewModel.notaCambio
                )
                var convMin: HashMap<String, String>? = null
                if (requireContext().systemLocale.language.equals(
                        LANGUAGE_UKRAINIAN,
                        ignoreCase = true
                    ) || requireContext().systemLocale.language.equals(
                        LANGUAGE_POLISH,
                        ignoreCase = true
                    )
                )
                    convMin = cambioAccordi.diffSemiToniMin(
                        mCantiViewModel.primaNota,
                        mCantiViewModel.notaCambio
                    )
                saveZoom(andSpeedAlso = false, andSaveTabAlso = false)
                if (convMap != null) {
                    htmlContent.value =
                        cambiaAccordi(
                            convMap,
                            mCantiViewModel.barreCambio,
                            convMin
                        )
                } else
                    htmlContent.value =
                        resources.readTextFromResource(
                            mCantiViewModel.pagina
                                ?: NO_CANTO
                        )
                mCantiViewModel.mCurrentCanto?.let {
                    if (it.zoom > 0)
                        initialScale.intValue = it.zoom
                }
            }

            DropDownMenuItem.BARRE_SALVA -> {
                if (!mCantiViewModel.mCurrentCanto?.savedBarre.equals(
                        mCantiViewModel.barreCambio,
                        ignoreCase = true
                    )
                ) {
                    mCantiViewModel.mCurrentCanto?.savedBarre =
                        mCantiViewModel.barreCambio
                    lifecycleScope.launch { updateCanto(2) }
                } else {
                    showSnackBar(getString(R.string.barre_not_saved))
                }
            }

            DropDownMenuItem.BARRE_RESET -> {
                mCantiViewModel.barreCambio = mCantiViewModel.primoBarre
                val convMap1 = cambioAccordi.diffSemiToni(
                    mCantiViewModel.primaNota,
                    mCantiViewModel.notaCambio
                )
                var convMin1: HashMap<String, String>? = null
                if (requireContext().systemLocale.language.equals(
                        LANGUAGE_UKRAINIAN,
                        ignoreCase = true
                    ) || requireContext().systemLocale.language.equals(
                        LANGUAGE_POLISH,
                        ignoreCase = true
                    )
                )
                    convMin1 = cambioAccordi.diffSemiToniMin(
                        mCantiViewModel.primaNota,
                        mCantiViewModel.notaCambio
                    )
                saveZoom(andSpeedAlso = false, andSaveTabAlso = false)
                if (convMap1 != null) {
                    htmlContent.value =
                        cambiaAccordi(
                            convMap1,
                            mCantiViewModel.barreCambio,
                            convMin1
                        )
                } else
                    htmlContent.value =
                        resources.readTextFromResource(
                            mCantiViewModel.pagina
                                ?: NO_CANTO
                        )
                mCantiViewModel.mCurrentCanto?.let {
                    if (it.zoom > 0)
                        initialScale.intValue = it.zoom
                }
            }

            DropDownMenuItem.TONO_DO, DropDownMenuItem.TONO_DO_D,
            DropDownMenuItem.TONO_RE, DropDownMenuItem.TONO_MI_B,
            DropDownMenuItem.TONO_MI, DropDownMenuItem.TONO_FA,
            DropDownMenuItem.TONO_FA_D, DropDownMenuItem.TONO_SOL,
            DropDownMenuItem.TONO_SOL_D, DropDownMenuItem.TONO_LA,
            DropDownMenuItem.TONO_SI_B, DropDownMenuItem.TONO_SI -> {
                mCantiViewModel.notaCambio = getString(item.value)
                val convMap2 = cambioAccordi.diffSemiToni(
                    mCantiViewModel.primaNota,
                    mCantiViewModel.notaCambio
                )
                var convMin2: HashMap<String, String>? = null
                if (requireContext().systemLocale.language.equals(
                        LANGUAGE_UKRAINIAN,
                        ignoreCase = true
                    ) || requireContext().systemLocale.language.equals(
                        LANGUAGE_POLISH,
                        ignoreCase = true
                    )
                )
                    convMin2 = cambioAccordi.diffSemiToniMin(
                        mCantiViewModel.primaNota,
                        mCantiViewModel.notaCambio
                    )
                saveZoom(andSpeedAlso = false, andSaveTabAlso = false)
                if (convMap2 != null) {
                    htmlContent.value =
                        cambiaAccordi(
                            convMap2,
                            mCantiViewModel.barreCambio,
                            convMin2
                        )
                } else
                    htmlContent.value =
                        resources.readTextFromResource(
                            mCantiViewModel.pagina
                                ?: NO_CANTO
                        )
                mCantiViewModel.mCurrentCanto?.let {
                    if (it.zoom > 0)
                        initialScale.intValue = it.zoom
                }
            }

            DropDownMenuItem.BARRE_NO, DropDownMenuItem.BARRE_I,
            DropDownMenuItem.BARRE_II, DropDownMenuItem.BARRE_III,
            DropDownMenuItem.BARRE_IV, DropDownMenuItem.BARRE_V,
            DropDownMenuItem.BARRE_VI, DropDownMenuItem.BARRE_VII -> {
                mCantiViewModel.barreCambio = if (item.value > 0) getString(item.value) else "0"
                Log.d(TAG, "barreCambio: ${mCantiViewModel.barreCambio}")
                val convMap3 = cambioAccordi.diffSemiToni(
                    mCantiViewModel.primaNota,
                    mCantiViewModel.notaCambio
                )
                var convMin3: HashMap<String, String>? = null
                if (requireContext().systemLocale.language.equals(
                        LANGUAGE_UKRAINIAN,
                        ignoreCase = true
                    ) || requireContext().systemLocale.language.equals(
                        LANGUAGE_POLISH,
                        ignoreCase = true
                    )
                )
                    convMin3 = cambioAccordi.diffSemiToniMin(
                        mCantiViewModel.primaNota,
                        mCantiViewModel.notaCambio
                    )
                saveZoom(andSpeedAlso = false, andSaveTabAlso = false)
                if (convMap3 != null) {
                    htmlContent.value =
                        cambiaAccordi(
                            convMap3,
                            mCantiViewModel.barreCambio,
                            convMin3
                        )
                } else
                    htmlContent.value =
                        resources.readTextFromResource(
                            mCantiViewModel.pagina
                                ?: NO_CANTO
                        )
                mCantiViewModel.mCurrentCanto?.let {
                    if (it.zoom > 0)
                        initialScale.intValue = it.zoom
                }
            }

            else -> {}
        }
    }

    private fun onFabActionItemClicked(item: String) {
        when (item) {
            FabActionItem.FULLSCREEN.id -> {
                saveZoom(andSpeedAlso = false, andSaveTabAlso = false)
                val bundle = Bundle().apply {
                    putString(Utility.HTML_CONTENT, htmlContent.value)
                    putFloat(
                        Utility.SPEED_VALUE,
                        seekBarScrollValue.floatValue
                    )
                    putInt(Utility.ZOOM_VALUE, mCantiViewModel.zoomValue)
                    putInt(Utility.SCROLL_X_VALUE, mCantiViewModel.scrollXValue)
                    putInt(Utility.SCROLL_Y_VALUE, mCantiViewModel.scrollYValue)
                    putBoolean(Utility.SCROLL_PLAYING, mCantiViewModel.scrollPlaying.value)
                    putInt(ARG_ID_CANTO, mCantiViewModel.idCanto)
                }
                showScrolling(false)
                val intent = Intent(requireContext(), PaginaRenderFullScreen::class.java)
                intent.putExtras(bundle)
                activity?.startActivityWithFadeIn(intent)
            }

            FabActionItem.SOUND.id -> {
                mCantiViewModel.mostraAudio = !mCantiViewModel.mostraAudio
                enableMusicControls(mCantiViewModel.mostraAudio)
                initFabOptions()
            }

            FabActionItem.DELETEFILE.id -> {
                if (!url.isNullOrEmpty() && personalUrl.isNullOrEmpty()) {
                    mCantiViewModel.dialogTag = SimpleDialogTag.DELETE_MP3
                    mCantiViewModel.dialogTitle.value = getString(R.string.dialog_delete_mp3_title)
                    mCantiViewModel.iconRes.value = R.drawable.delete_24px
                    mCantiViewModel.content.value = getString(R.string.dialog_delete_mp3)
                    mCantiViewModel.positiveButton.value = getString(R.string.delete_confirm)
                    mCantiViewModel.negativeButton.value = getString(R.string.cancel)
                } else {
                    mCantiViewModel.dialogTag = SimpleDialogTag.DELETE_LINK
                    mCantiViewModel.dialogTitle.value = getString(R.string.dialog_delete_link_title)
                    mCantiViewModel.iconRes.value = R.drawable.link_off_24px
                    mCantiViewModel.content.value = getString(R.string.dialog_delete_link)
                    mCantiViewModel.positiveButton.value = getString(R.string.unlink_confirm)
                    mCantiViewModel.negativeButton.value = getString(R.string.cancel)
                }
                mCantiViewModel.showAlertDialog.value = true
            }

            FabActionItem.SAVEFILE.id -> {
                mCantiViewModel.dialogTag = SimpleDialogTag.DOWNLINK_CHOOSE
                mCantiViewModel.dialogTitle.value = getString(R.string.save_file)
                mCantiViewModel.iconRes.value = R.drawable.file_download_24px
                mCantiViewModel.content.value = getString(R.string.download_message)
                mCantiViewModel.positiveButton.value = getString(R.string.download_confirm)
                mCantiViewModel.negativeButton.value = getString(R.string.cancel)
                mCantiViewModel.showAlertDialog.value = true
            }

            FabActionItem.LINKFILE.id -> {
                mCantiViewModel.dialogTag = SimpleDialogTag.ONLY_LINK
                mCantiViewModel.dialogTitle.value = getString(R.string.only_link_title)
                mCantiViewModel.iconRes.value = R.drawable.add_link_24px
                mCantiViewModel.content.value = getString(R.string.only_link)
                mCantiViewModel.positiveButton.value = getString(R.string.associate_confirm)
                mCantiViewModel.negativeButton.value = getString(R.string.cancel)
                mCantiViewModel.showAlertDialog.value = true
            }

            FabActionItem.FAVORITE.id -> {
                lifecycleScope.launch { updateFavorite() }
            }
        }
    }

    companion object {
        internal val TAG = CantoFragment::class.java.canonicalName
        const val ARG_ID_CANTO = "ARG_ID_CANTO"
        const val ARG_NUM_PAGINA = "ARG_NUM_PAGINA"
        const val ARG_ON_ACTIVITY = "ARG_ON_ACTIVITY"
        private const val PROGRESS_UPDATE_INTERNAL: Long = 1000
        private const val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
        private const val ECONDING_UTF8 = "utf-8"
        private const val NO_CANTO = "no_canto"
        private const val MP3_MIME_TYPE = "audio/mpeg"
        private const val PRE_START = "<H3><PRE>"
        private const val PRE_END = "</PRE></H3>"

    }
}
