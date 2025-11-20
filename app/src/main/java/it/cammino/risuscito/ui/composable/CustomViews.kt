package it.cammino.risuscito.ui.composable

import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.postDelayed
import it.cammino.risuscito.R
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel
import kotlinx.coroutines.delay

@Composable
fun Hint(
    hintText: String,
    onDismiss: () -> Unit = {}
) {
    val swipeToDismissBoxState =
        rememberSwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            SwipeToDismissBoxDefaults.positionalThreshold
        )

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        backgroundContent = {
            SwipeToDismissBackground(swipeToDismissBoxState)
        },
        onDismiss = {
            onDismiss()
        }
    ) {
        ListItem(
            modifier = Modifier.height(88.dp),
            supportingContent = {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            headlineContent = {},
            leadingContent = {
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.info_24px),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .zIndex(1f)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    VerticalDivider()
                }
            }
        )
    }
}

@Composable
fun SwipeToDismissBackground(
    swipeToDismissBoxState: SwipeToDismissBoxState
) {
    when (swipeToDismissBoxState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd,
        SwipeToDismissBoxValue.EndToStart -> {
            Icon(
                painter = painterResource(R.drawable.delete_sweep_24px),
                contentDescription = "Remove item",
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .wrapContentSize(if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }

        SwipeToDismissBoxValue.Settled -> {}
    }
}

@Preview
@Composable
fun MainHintLayoutPreview() {
    RisuscitoTheme {
        Hint(
            hintText = stringResource(id = R.string.showcase_rename_desc) + System.lineSeparator() + stringResource(
                R.string.showcase_delete_desc
            )
        )
    }
}

@Composable
fun EmptyListView(iconRes: Int, textRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), // Occupa solo l'altezza necessaria
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = stringResource(id = textRes),
            modifier = Modifier
                .size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Spazio tra immagine e testo
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Colore secondario del testo
            fontFamily = risuscito_medium_font,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth() // Per centrare il testo se è multiriga
        )
    }
}

@Preview
@Composable
fun EmptyListViewPreview() {
    EmptyListView(
        iconRes = R.drawable.ic_sunglassed_star,
        textRes = R.string.no_favourites_short
    )
}

@Composable
fun WebView(
    modifier: Modifier = Modifier,
    canto: Canto?,
    content: String?,
    initialScale: Int,
    autoScroll: Boolean,
    scrollSpeed: Float,
    onScrollChange: (Int, Int) -> Unit,
    onZoomChange: (Int) -> Unit = {}
) {

    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = InitialScrollWebClient(canto, onZoomChange)
        }

    }

    LaunchedEffect(content) {
        val webSettings = webView.settings
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.loadWithOverviewMode = true

        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        if (!content.isNullOrEmpty()) {
            val newContent = content.replace(OLD_META, NEW_META)
            webView.loadDataWithBaseURL(
                "",
                newContent,
                DEFAULT_MIME_TYPE,
                ECONDING_UTF8,
                ""
            )
        }
    }

    webView.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
        onScrollChange(scrollX, scrollY)
    }

    LaunchedEffect(initialScale) {
        webView.setInitialScale(initialScale)
    }

    // 1. Crea un riferimento aggiornato a scrollSpeed.
    // Questo valore verrà aggiornato a ogni ricomposizione, ma non farà ripartire il LaunchedEffect.
    val updatedScrollSpeed by rememberUpdatedState(scrollSpeed)

    // AGGIUNGI QUESTO NUOVO LaunchedEffect
    LaunchedEffect(autoScroll) {
        if (autoScroll) {
            // Questo ciclo si interrompe non appena `autoScroll` diventa `false`
            while (true) {
                val realScrollSpeed = (updatedScrollSpeed * 100).toInt()
                Log.d("webView", "WebView: Eseguo scrollBy: $realScrollSpeed")
                webView.scrollBy(0, realScrollSpeed)
                delay(700) // Aspetta 700ms prima di scorrere di nuovo
            }
        }
    }

    AndroidView(factory = { webView }, modifier = modifier)
}

open class InitialScrollWebClient(val canto: Canto?, val onZoomChange: (Int) -> Unit = {}) :
    WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        view.postDelayed(600) {
            if ((canto?.scrollX
                    ?: 0) > 0 || (canto?.scrollY ?: 0) > 0
            )
                view.scrollTo(
                    canto?.scrollX
                        ?: 0, canto?.scrollY ?: 0
                )
        }
        super.onPageFinished(view, url)
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        super.onScaleChanged(view, oldScale, newScale)
        onZoomChange((newScale * 100).toInt())
    }

}

private const val OLD_META =
    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"
private const val NEW_META =
    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
            "<style type=\"text/css\">\n" +
            "   @font-face {\n" +
            "      font-family: 'MediumFont';\n" +
            "      src: url(\"file:///android_asset/fonts/DMSans_medium.ttf\")\n" +
            "   }\n" +
            "   @font-face {\n" +
            "         font-family: 'PreFont';\n" +
            "         src: url(\"file:///android_asset/fonts/FiraMono_regular.ttf\")\n" +
            "      }\n" +
            "   h2 {\n" +
            "      font-family: 'MediumFont';\n" +
            "      text-align: center;\n" +
            "   }\n" +
            "   h3 {\n" +
            "      text-align: center;\n" +
            "   }\n" +
            "   h4 {\n" +
            "         font-family: 'MediumFont';\n" +
            "         text-align: left;\n" +
            "         margin-left: 50px;\n" +
            "   }\n" +
            "   pre {\n" +
            "      font-family: 'PreFont';\n" +
            "      text-align: left;\n" +
            "   }\n" +
            "</style>"

private const val ECONDING_UTF8 = "utf-8"
private const val DEFAULT_MIME_TYPE = "text/html; charset=utf-8"

@Composable
fun StateNotificationView(iconRes: Int, textRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(textRes),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun StateNotificationViewPreview() {
    StateNotificationView(
        iconRes = R.drawable.music_off_24px,
        textRes = R.string.no_record
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaPlayerView(
    seekbarViewMode: PaginaRenderViewModel.SeekBarMode,
    playButtonMode: PaginaRenderViewModel.PlayButtonMode,
    playButtonAnimated: Boolean = false,
    onPlayButtonClick: () -> Unit,
    playButtonEnabled: Boolean = false,
    timeText: String,
    seekBarValue: Float = 0f,
    seekBarMaxValue: Float = 0f,
    seekBarEnabled: Boolean = false,
    onValueChange: (Float) -> Unit

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = timeText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.width(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            AnimatedScaleContent(
                seekbarViewMode,
            ) { state ->
                when (state) {
                    PaginaRenderViewModel.SeekBarMode.LOADINGBAR -> {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    PaginaRenderViewModel.SeekBarMode.SEEKBAR -> {
                        Slider(
                            value = seekBarValue,
                            onValueChange = { onValueChange(it) },
                            valueRange = 0f..seekBarMaxValue,
                            enabled = seekBarEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        AnimatedScaleContent(
            playButtonMode,
        ) {
            when (it) {
                PaginaRenderViewModel.PlayButtonMode.LOADING -> {
                    CircularWavyProgressIndicator(modifier = Modifier.wrapContentSize())
                }

                PaginaRenderViewModel.PlayButtonMode.PLAY -> {
                    val image =
                        AnimatedImageVector.animatedVectorResource(R.drawable.play_to_pause_anim)
                    FilledTonalIconButton(
                        onClick = {
                            onPlayButtonClick()
                        },
                        enabled = playButtonEnabled
                    ) {
                        Image(
                            painter = rememberAnimatedVectorPainter(image, playButtonAnimated),
                            contentDescription = "PlayPause",
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(5.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScrollPlayerView(
    playButtonAnimated: Boolean = false,
    onPlayButtonClick: () -> Unit,
    seekBarValue: Float = 0.02f,
    onValueChange: (Float) -> Unit

) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = stringResource(R.string.percent_progress, (seekBarValue * 100).toInt()),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.width(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            Slider(
                value = seekBarValue,
                onValueChange = { onValueChange(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        val image =
            AnimatedImageVector.animatedVectorResource(R.drawable.play_to_pause_circle_anim)
        FilledTonalIconButton(
            onClick = {
                onPlayButtonClick()
            }) {
            Image(
                painter = rememberAnimatedVectorPainter(image, playButtonAnimated),
                contentDescription = "PlayPauseSroll",
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(5.dp))
    }
}

@Composable
fun <S> AnimatedFadeContent(
    targetState: S,
    duration: Int = 1000,
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(duration)
            ) togetherWith fadeOut(animationSpec = tween(duration))
        },
        label = "Animated Content"
    ) {
        content(it)
    }
}

@Composable
fun <S> AnimatedScaleContent(
    targetState: S,
    duration: Int = 300,
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState,
        transitionSpec = {
            scaleIn(
                animationSpec = tween(duration)
            ) togetherWith scaleOut(animationSpec = tween(duration))
        },
        label = "Animated Content"
    ) {
        content(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicBackNavitagionButton(onBackPressedAction: () -> Unit) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
            ),
        tooltip = { PlainTooltip { Text(stringResource(R.string.material_drawer_close)) } },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = { onBackPressedAction() }
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24px),
                contentDescription = stringResource(R.string.material_drawer_close),
            )
        }
    }
}
