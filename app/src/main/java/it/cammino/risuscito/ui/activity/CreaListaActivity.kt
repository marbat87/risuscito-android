package it.cammino.risuscito.ui.activity

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.SwipeableRisuscitoListItem
import it.cammino.risuscito.ui.composable.DraggableDismissableListItem
import it.cammino.risuscito.ui.composable.EmptyListView
import it.cammino.risuscito.ui.composable.Hint
import it.cammino.risuscito.ui.composable.dialogs.InputDialog
import it.cammino.risuscito.ui.composable.dialogs.InputDialogTag
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.main.creaListaMenu
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.getTypedValueResId
import it.cammino.risuscito.utils.extension.setEnterTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.CreaListaViewModel
import it.cammino.risuscito.viewmodels.InputDialogManagerViewModel
import it.cammino.risuscito.viewmodels.SnackBarTag
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class CreaListaActivity : ThemeableActivity() {

    private val mCreaListaViewModel: CreaListaViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply {
            putInt(ID_DA_MODIF, intent.extras?.getInt(ID_DA_MODIF, 0) ?: 0)
        })
    }

    private val inputdialogViewModel: InputDialogManagerViewModel by viewModels()

    private var modifica: Boolean = false

    private var elementoRimosso: SwipeableRisuscitoListItem? = null
    private var indiceRimosso: Int = 0

    private val hintVisible = mutableStateOf(false)

    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val mSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(this)

        setContent {
            RisuscitoTheme {
                modifica = intent.extras?.getBoolean(EDIT_EXISTING_LIST) == true

                val scope = rememberCoroutineScope()

                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                val inputState = rememberTextFieldState()

                val keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences
                )

                val snackbarHostState = remember { SnackbarHostState() }

                val showInputDialog by inputdialogViewModel.showAlertDialog.observeAsState()

                val showAlertDialog by mCreaListaViewModel.showAlertDialog.observeAsState()

                LaunchedEffect(inputState) {
                    snapshotFlow { inputState.text }
                        .distinctUntilChanged()
                        .collect {
                            Log.d(TAG, "inputState.text: $it")
                            mCreaListaViewModel.tempTitle.value = it.toString()
                        }
                }

                LaunchedEffect(mCreaListaViewModel.tempTitle.value) {
                    snapshotFlow { mCreaListaViewModel.tempTitle.value }
                        .distinctUntilChanged()
                        .collect {
                            Log.d(TAG, "mCreaListaViewModel.tempTitle.value: $it")
                            inputState.edit { replace(0, length, it) }
                        }
                }

                val hapticFeedback = LocalHapticFeedback.current

                val localItems by mCreaListaViewModel.elementi.observeAsState()
                val lazyListState = rememberLazyListState()
                val reorderableLazyListState =
                    rememberReorderableLazyListState(lazyListState) { from, to ->
                        mCreaListaViewModel.elementi.value =
                            mCreaListaViewModel.elementi.value.orEmpty().toMutableList().apply {
                                add(to.index - 1, removeAt(from.index - 1))
                            }
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }

                val rememberItemLongClick = remember<(Int, SwipeableRisuscitoListItem) -> Unit> {
                    { index, item ->
                        mCreaListaViewModel.positionToRename = index
                        inputdialogViewModel.dialogTag = InputDialogTag.RENAME
                        inputdialogViewModel.dialogTitleRes = R.string.posizione_rename
                        inputdialogViewModel.confirmationLabelRes = R.string.aggiungi_rename
                        inputdialogViewModel.dialogPrefill = item.title
                        inputdialogViewModel.showAlertDialog.value = true
                    }
                }

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(mCreaListaViewModel.tempTitle.value)
                            },
                            navigationIcon = {
                                IconButton(onClick = { onOptionsItemSelected(ActionModeItem.CLOSE) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back_24px),
                                        contentDescription = stringResource(R.string.material_drawer_close)
                                    )
                                }
                            },
                            actions = {
                                AppBarRow(overflowIndicator = {}) {
                                    creaListaMenu.forEach {

                                        clickableItem(
                                            onClick = { onOptionsItemSelected(it) },
                                            icon = {
                                                Icon(
                                                    painter = painterResource(it.iconRes),
                                                    contentDescription = stringResource(it.label),
                                                )
                                            },
                                            label = "",
                                        )
                                    }
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    floatingActionButton = {
                        Column(horizontalAlignment = Alignment.End) {
                            FloatingActionButton(
                                onClick = { scope.launch { saveList() } },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.save_24px),
                                    contentDescription = "Floating action button."
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            ExtendedFloatingActionButton(
                                onClick = {
                                    inputdialogViewModel.dialogTag = InputDialogTag.ADD
                                    inputdialogViewModel.dialogTitleRes =
                                        R.string.posizione_add_desc
                                    inputdialogViewModel.confirmationLabelRes =
                                        R.string.aggiungi_confirm
                                    inputdialogViewModel.showAlertDialog.value = true
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.add_24px),
                                        contentDescription = stringResource(
                                            R.string.add_position
                                        )
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(
                                            R.string.add_position
                                        )
                                    )
                                })
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { innerPadding ->

                    val listModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .then(
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        )

                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyColumn(
                            modifier = listModifier,
                            state = lazyListState,
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                Column {
                                    OutlinedTextField(
                                        label = { Text(stringResource(R.string.list_title)) },
                                        modifier = Modifier.padding(
                                            bottom = 12.dp
                                        ),
                                        state = inputState,
                                        lineLimits = TextFieldLineLimits.SingleLine,
                                        keyboardOptions = keyboardOptions
                                    )
                                    Text(
                                        text = stringResource(R.string.list_elements),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(vertical = 16.dp)
                                    )
                                    if (hintVisible.value) {
                                        Hint(
                                            hintText = stringResource(id = R.string.showcase_rename_desc) + System.lineSeparator() + stringResource(
                                                R.string.showcase_delete_desc
                                            ),
                                            onDismiss = {
                                                hintVisible.value = false
                                                PreferenceManager.getDefaultSharedPreferences(
                                                    this@CreaListaActivity
                                                )
                                                    .edit {
                                                        putBoolean(
                                                            Utility.INTRO_CREALISTA_2,
                                                            true
                                                        )
                                                    }
                                            }
                                        )
                                    }
                                }
                            }

                            itemsIndexed(
                                localItems.orEmpty(),
                                key = { _, item -> item.identifier }) { index, item ->
                                val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.StartToEnd) removeItem(
                                            index, item
                                        )
                                        else if (it == SwipeToDismissBoxValue.EndToStart) removeItem(
                                            index, item
                                        )
                                        // Reset item when toggling done status
                                        it != SwipeToDismissBoxValue.StartToEnd && it != SwipeToDismissBoxValue.EndToStart
                                    }
                                )

                                ReorderableItem(
                                    state = reorderableLazyListState,
                                    key = item.identifier
                                ) { isDragging ->
                                    val interactionSource =
                                        remember { MutableInteractionSource() }

                                    DraggableDismissableListItem(
                                        modifier = Modifier.animateItem(),
                                        dragModifier = Modifier.draggableHandle(
                                            onDragStarted = {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.GestureThresholdActivate
                                                )
                                            },
                                            onDragStopped = {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.GestureEnd
                                                )
                                            },
                                            interactionSource = interactionSource
                                        ),
                                        interactionSource = interactionSource,
                                        swipeToDismissBoxState = swipeToDismissBoxState,
                                        index = index,
                                        item = item,
                                        onItemLongClick = rememberItemLongClick
                                    )
                                }
                            }

                            item {
                                Spacer(Modifier.height(144.dp))
                            }
                        }

                        AnimatedVisibility(
                            visible = localItems.orEmpty().isEmpty(),
                            enter = fadeIn(animationSpec = tween(1000)),
                            exit = fadeOut(animationSpec = tween(100))
                        ) {
                            EmptyListView(
                                iconRes = R.drawable.format_list_bulleted_add_24px,
                                textRes = R.string.no_elements_added
                            )
                        }

                    }
                }

                LaunchedEffect(sharedSnackBarViewModel.showSnackBar.value) {
                    if (sharedSnackBarViewModel.showSnackBar.value) {
                        val result = snackbarHostState
                            .showSnackbar(
                                message = sharedSnackBarViewModel.snackbarMessage,
                                actionLabel = sharedSnackBarViewModel.actionLabel.ifBlank { null },
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                when (sharedSnackBarViewModel.snackBarTag) {
                                    SnackBarTag.ELEMENT_REMOVED -> readdItem()
                                    else -> {}
                                }
                                sharedSnackBarViewModel.showSnackBar.value = false
                            }

                            SnackbarResult.Dismissed -> {
                                sharedSnackBarViewModel.showSnackBar.value = false
                            }
                        }
                    }
                }

                if (showInputDialog == true) {
                    InputDialog(
                        dialogTag = inputdialogViewModel.dialogTag,
                        dialogTitleRes = inputdialogViewModel.dialogTitleRes,
                        onDismissRequest = {
                            inputdialogViewModel.showAlertDialog.value = false
                        },
                        prefill = inputdialogViewModel.dialogPrefill,
                        onConfirmation = { tag, text ->
                            inputdialogViewModel.showAlertDialog.value = false
                            when (tag) {
                                InputDialogTag.RENAME -> {
                                    renameItem(text)
                                }

                                InputDialogTag.ADD -> {
                                    addNewItem(text)
                                    Log.d(
                                        TAG,
                                        "onPositive - elementi.size(): ${mCreaListaViewModel.elementi.value.orEmpty().size}"
                                    )
                                    Log.d(
                                        TAG,
                                        "onPositive - INTRO_CREALISTA_2: " + mSharedPrefs.getBoolean(
                                            Utility.INTRO_CREALISTA_2, false
                                        )
                                    )
                                    hintVisible.value =
                                        !mSharedPrefs.getBoolean(
                                            Utility.INTRO_CREALISTA_2,
                                            false
                                        )
                                }

                                else -> {}
                            }
                        },
                        confirmationTextRes = inputdialogViewModel.confirmationLabelRes,
                        multiline = true,
                        required = true
                    )
                }

                if (showAlertDialog == true) {
                    SimpleAlertDialog(
                        onDismissRequest = {
                            mCreaListaViewModel.showAlertDialog.postValue(false)
                            setResult(RESULT_CANCELED)
                            finishAfterTransitionWrapper()
                        },
                        onConfirmation = {
                            mCreaListaViewModel.showAlertDialog.postValue(false)
                            lifecycleScope.launch { saveList() }
                        },
                        dialogTitle = stringResource(R.string.save_list_title),
                        dialogText = stringResource(R.string.save_list_question),
                        iconRes = R.drawable.save_24px,
                        confirmButtonText = stringResource(R.string.save_exit_confirm),
                        dismissButtonText = stringResource(R.string.discard_exit_confirm)
                    )
                }

                // After drawing main content, draw status bar protection
                StatusBarProtection()

                BackHandler {
                    onBackPressedAction()
                }
            }

            if (modifica)
                mCreaListaViewModel.listaResult?.observe(this) { listaPers ->

                    val celebrazione = listaPers.lista

                    val tempList = ArrayList<SwipeableRisuscitoListItem>()

                    celebrazione?.let {
                        for (i in 0 until it.numPosizioni) {
                            tempList.add(
                                SwipeableRisuscitoListItem(
                                    identifier = Utility.random(0, 5000).toLong(),
                                    title = it.getNomePosizione(i),
                                    idCanto = it.getCantoPosizione(i),
                                    nota = it.getNotaPosizione(i),

                                    )
                            )
                        }
                    }
                    mCreaListaViewModel.elementi.value = tempList

                    if (mCreaListaViewModel.tempTitle.value.isEmpty())
                        mCreaListaViewModel.tempTitle.value = listaPers.titolo ?: DEFAULT_TITLE

                    if (!mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false)) {
                        Handler(Looper.getMainLooper()).postDelayed(1500) {
                            playIntro()
                        }
                    }

                    hintVisible.value =
                        mCreaListaViewModel.elementi.value.orEmpty()
                            .isNotEmpty() && !mSharedPrefs.getBoolean(
                            Utility.INTRO_CREALISTA_2, false
                        )
                }
            else {
                if (mCreaListaViewModel.tempTitle.value.isEmpty())
                    mCreaListaViewModel.tempTitle.value = intent.extras?.getString(LIST_TITLE).orEmpty()
                mCreaListaViewModel.elementi.value = emptyList()
            }

        }

        mRegularFont =
            ResourcesCompat.getFont(this, getTypedValueResId(R.attr.risuscito_regular_font))
        mMediumFont =
            ResourcesCompat.getFont(this, getTypedValueResId(R.attr.risuscito_medium_font))

    }

    private fun removeItem(index: Int, item: SwipeableRisuscitoListItem) {
        elementoRimosso = item
        indiceRimosso = index
        mCreaListaViewModel.elementi.value =
            mCreaListaViewModel.elementi.value?.filter { it.identifier != item.identifier }

        sharedSnackBarViewModel.snackBarTag = SnackBarTag.ELEMENT_REMOVED
        sharedSnackBarViewModel.snackbarMessage = getString(R.string.generic_removed, item.title)
        sharedSnackBarViewModel.actionLabel = getString(R.string.cancel).uppercase(systemLocale)
        sharedSnackBarViewModel.showSnackBar.value = true

    }

    private fun readdItem() {
        val newList = ArrayList(mCreaListaViewModel.elementi.value.orEmpty())
        newList.add(indiceRimosso, elementoRimosso)
        mCreaListaViewModel.elementi.value = newList
    }

    private fun addNewItem(title: String) {
        Log.d(TAG, "addNewItem: $title")
        val newList = ArrayList(mCreaListaViewModel.elementi.value.orEmpty())
        newList.add(
            SwipeableRisuscitoListItem(
                identifier = Utility.random(0, 5000).toLong(),
                title = title,
            )
        )
        mCreaListaViewModel.elementi.value = newList
    }

    private fun renameItem(title: String) {
        val newElement =
            mCreaListaViewModel.elementi.value.orEmpty()[mCreaListaViewModel.positionToRename].copy(
                title = title
            )
        val newList =
            ArrayList(mCreaListaViewModel.elementi.value.orEmpty())
        newList[mCreaListaViewModel.positionToRename] = newElement
        mCreaListaViewModel.elementi.value = newList
    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        if (mCreaListaViewModel.elementi.value.orEmpty().isNotEmpty()) {
            mCreaListaViewModel.showAlertDialog.value = true
        } else {
            setResult(RESULT_CANCELED)
            finishAfterTransitionWrapper()
        }
    }

    fun onOptionsItemSelected(item: ActionModeItem) {
        when (item) {
            ActionModeItem.HELP -> {
                playIntro()
                hintVisible.value =
                    mCreaListaViewModel.elementi.value.orEmpty().isNotEmpty()
            }

            ActionModeItem.CLOSE -> {
                onBackPressedAction()
            }

            else -> {}
        }
    }

    private suspend fun saveList() {
        val mDao = RisuscitoDatabase.getInstance(this).listePersDao()

        var result = 0
        val celebrazione = ListaPersonalizzata()

        if (mCreaListaViewModel.tempTitle.value.isNotBlank()) {
            celebrazione.name = mCreaListaViewModel.tempTitle.value
        } else {
            result += 100
            celebrazione.name =
                if (modifica) withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.getListById(mCreaListaViewModel.idModifica)?.titolo
                } ?: DEFAULT_TITLE else intent.extras?.getString(LIST_TITLE)
                    ?: DEFAULT_TITLE
        }

        Log.d(
            TAG,
            "saveList - elementi.size(): ${mCreaListaViewModel.elementi.value.orEmpty().size}"
        )
        mCreaListaViewModel.elementi.value.orEmpty().forEachIndexed { index, item ->
            if (celebrazione.addPosizione(item.title) == -2) {
                sharedSnackBarViewModel.snackBarTag = SnackBarTag.DEFAULT
                sharedSnackBarViewModel.snackbarMessage = getString(R.string.lista_pers_piena)
                sharedSnackBarViewModel.actionLabel = StringUtils.EMPTY
                sharedSnackBarViewModel.showSnackBar.value = true
            }
            celebrazione.addCanto(item.idCanto, index)
            celebrazione.addNota(item.nota, index)
        }

        if (celebrazione.getNomePosizione(0).isEmpty()) {
            sharedSnackBarViewModel.snackBarTag = SnackBarTag.DEFAULT
            sharedSnackBarViewModel.snackbarMessage = getString(R.string.lista_pers_vuota)
            sharedSnackBarViewModel.actionLabel = StringUtils.EMPTY
            sharedSnackBarViewModel.showSnackBar.value = true
            return
        }

        Log.d(TAG, "saveList - $celebrazione")

        val listaToUpdate = ListaPers()
        listaToUpdate.lista = celebrazione
        listaToUpdate.titolo = celebrazione.name
        if (modifica) {
            listaToUpdate.id = mCreaListaViewModel.idModifica
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.updateLista(
                    listaToUpdate
                )
            }
        } else withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.insertLista(
                listaToUpdate
            )
        }

        if (result == 100) Toast.makeText(
            this,
            getString(R.string.no_title_edited),
            Toast.LENGTH_SHORT
        ).show()
        setResult(RESULT_OK)
        finishAfterTransitionWrapper()
    }

    //TODO
    private fun playIntro() {
//        binding.fabCreaLista.show()
//        val colorOnPrimary =
//            MaterialColors.getColor(
//                this,
//                com.google.android.material.R.attr.colorOnPrimary,
//                TAG
//            )
//        TapTargetSequence(this).continueOnCancel(true).targets(
//            TapTarget.forView(
//                binding.fabCreaLista,
//                getString(R.string.add_position),
//                getString(R.string.showcase_add_pos_desc)
//            )
//                // All options below are optional
//                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                .titleTypeface(mMediumFont) // Specify a typeface for the text
//                .titleTextColorInt(colorOnPrimary).textColorInt(colorOnPrimary)
//                .tintTarget(false).setForceCenteredTarget(true).id(1),
//            TapTarget.forToolbarMenuItem(
//                binding.risuscitoToolbar,
//                R.id.action_save_list,
//                getString(R.string.list_save_exit),
//                getString(R.string.showcase_saveexit_desc)
//            )
//                // All options below are optional
//                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                .titleTypeface(mMediumFont) // Specify a typeface for the text
//                .titleTextColorInt(colorOnPrimary).textColorInt(colorOnPrimary)
//                .setForceCenteredTarget(true).id(2),
//            TapTarget.forToolbarMenuItem(
//                binding.risuscitoToolbar,
//                R.id.action_help,
//                getString(R.string.showcase_end_title),
//                getString(R.string.showcase_help_general)
//            )
//                // All options below are optional
//                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                .titleTypeface(mMediumFont) // Specify a typeface for the text
//                .titleTextColorInt(colorOnPrimary).textColorInt(colorOnPrimary)
//                .setForceCenteredTarget(true).id(3)
//        ).listener(object :
//            TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
//            override fun onSequenceFinish() {
//                Log.d(TAG, "onSequenceFinish: ")
//                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity)
//                    .edit { putBoolean(Utility.INTRO_CREALISTA, true) }
//            }
//
//            override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
//                // no-op
//            }
//
//            override fun onSequenceCanceled(tapTarget: TapTarget) {
//                Log.d(TAG, "onSequenceCanceled: ")
//                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity)
//                    .edit { putBoolean(Utility.INTRO_CREALISTA, true) }
//            }
//        }).start()
    }

    companion object {
        private val TAG = CreaListaActivity::class.java.canonicalName
        const val ID_DA_MODIF = "idDaModif"
        const val LIST_TITLE = "titoloLista"
        const val EDIT_EXISTING_LIST = "modifica"
        const val DEFAULT_TITLE = "NEW LIST"
    }
}

