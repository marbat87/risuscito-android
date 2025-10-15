package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.compose.AndroidFragment
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.ui.fragment.SimpleIndexFragment
import it.cammino.risuscito.utils.CantiXmlParser
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.setEnterTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.Collator

class InsertActivity : ThemeableActivity() {

    private var listaPredefinita: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0

    private val simpleIndexViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply { putInt(Utility.TIPO_LISTA, 3) })
    }

    private val sharedSearchViewModel: SharedSearchViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
        super.onCreate(savedInstanceState)

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.fileout)
            sharedSearchViewModel.aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        }

        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0") ?: "0")
            sharedSearchViewModel.advancedSearchFilter.value = currentItem != 0
        }

        // enableEdgeToEdge sets window.isNavigationBarContrastEnforced = true
        // which is used to add a translucent scrim to three-button navigation
        enableEdgeToEdge()

        setContent {

            val scope = rememberCoroutineScope()
            val searchBarState = rememberSearchBarState(SearchBarValue.Expanded)

            RisuscitoTheme {

                Scaffold(
                    // 2. Collega il scrollBehavior allo Scaffold tramite il Modifier.nestedScroll
                    modifier = Modifier
                        .fillMaxSize(),
                    topBar = {
                        val textFieldState = rememberTextFieldState()

                        LaunchedEffect(searchBarState.currentValue) {
                            textFieldState.edit { replace(0, length, "") }
                            sharedSearchViewModel.searchFilter.value = ""
                        }

                        val inputField =
                            @Composable {
                                SearchBarDefaults.InputField(
                                    query = textFieldState.text.toString(),
                                    onQueryChange = {
                                        textFieldState.edit { replace(0, length, it) }
                                        sharedSearchViewModel.searchFilter.value = it
                                    },
                                    expanded = true,
                                    onExpandedChange = {
                                        if (it) scope.launch { searchBarState.animateToExpanded() }
                                        else {
                                            scope.launch { searchBarState.animateToCollapsed() }
                                        }
                                    },
                                    onSearch = { },
                                    placeholder = {
                                        Text(
                                            stringResource(R.string.search_hint),
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = {
                                        IconButton(onClick = {
                                            scope.launch { onBackPressedAction() }
                                        }) {
                                            Icon(
                                                painterResource(R.drawable.arrow_back_24px),
                                                contentDescription = stringResource(R.string.material_drawer_close)
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (textFieldState.text.isNotEmpty()) {
                                            IconButton(onClick = {
                                                textFieldState.edit { replace(0, length, "") }
                                                sharedSearchViewModel.searchFilter.value = ""
                                            }) {
                                                Icon(
                                                    painterResource(R.drawable.close_24px),
                                                    contentDescription = "Cancella"
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        SearchBar(
                            state = searchBarState,
                            inputField = inputField,
                            modifier =
                                Modifier
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                    .fillMaxWidth()
                        )
                        ExpandedFullScreenSearchBar(
                            state = searchBarState,
                            inputField = inputField
                        ) {
                            val advancedSelected by sharedSearchViewModel.advancedSearchFilter.observeAsState()
                            val consegnatiOnlySelected by sharedSearchViewModel.consegnatiOnlyFilter.observeAsState()

                            Column {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    FilterChip(
                                        onClick = {
                                            sharedSearchViewModel.advancedSearchFilter.value =
                                                advancedSelected != true
                                        },
                                        label = {
                                            Text(stringResource(R.string.advanced_search_subtitle))
                                        },
                                        selected = advancedSelected == true,
                                        leadingIcon = if (advancedSelected == true) {
                                            {
                                                Icon(
                                                    painter = painterResource(R.drawable.check_24px),
                                                    contentDescription = "Done icon",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                    )
                                    FilterChip(
                                        onClick = {
                                            sharedSearchViewModel.consegnatiOnlyFilter.value =
                                                consegnatiOnlySelected != true
                                        },
                                        label = {
                                            Text(stringResource(R.string.consegnati_only).uppercase())
                                        },
                                        selected = consegnatiOnlySelected == true,
                                        leadingIcon = if (consegnatiOnlySelected == true) {
                                            {
                                                Icon(
                                                    painter = painterResource(R.drawable.check_24px),
                                                    contentDescription = "Done icon",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                    )
                                }
                                AndroidFragment<SimpleIndexFragment>(
                                    arguments = bundleOf(
                                        SimpleIndexFragment.INDICE_LISTA to 3,
                                        SimpleIndexFragment.IS_SEARCH to true,
                                        SimpleIndexFragment.IS_INSERT to true
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                // After drawing main content, draw status bar protection
                StatusBarProtection()
            }

            val searchDone by sharedSearchViewModel.done.observeAsState()

            LaunchedEffect(searchDone) {
                snapshotFlow { searchDone }
                    .distinctUntilChanged()
                    .collect { done ->
                        if (done == true) {
                            sharedSearchViewModel.done.value = false
                            if (listaPredefinita == 1) {
                                ListeUtils.addToListaDupAndFinish(
                                    this@InsertActivity,
                                    idLista,
                                    listPosition,
                                    sharedSearchViewModel.insertItemId
                                )
                            } else {
                                ListeUtils.updateListaPersonalizzataAndFinish(
                                    this@InsertActivity,
                                    idLista,
                                    sharedSearchViewModel.insertItemId,
                                    listPosition
                                )
                            }
                        }
                    }
            }

        }

        val bundle = intent.extras
        listaPredefinita = bundle?.getInt(FROM_ADD) ?: 0
        idLista = bundle?.getInt(ID_LISTA) ?: 0
        listPosition = bundle?.getInt(POSITION) ?: 0

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        simpleIndexViewModel.itemsResult?.observe(this) { canti ->
            sharedSearchViewModel.titoli = canti.sortedWith(
                compareBy(
                    Collator.getInstance(systemLocale)
                ) { getString(it.titleRes) })
        }

    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        setResult(CustomListsFragment.RESULT_CANCELED)
        finishAfterTransitionWrapper()
    }

    companion object {
        private val TAG = InsertActivity::class.java.canonicalName
        internal const val FROM_ADD = "fromAdd"
        internal const val ID_LISTA = "idLista"
        internal const val POSITION = "position"
    }
}
