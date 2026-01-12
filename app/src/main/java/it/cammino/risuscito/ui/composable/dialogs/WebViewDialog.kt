package it.cammino.risuscito.ui.composable.dialogs

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import it.cammino.risuscito.ui.composable.DialogTitle
import it.cammino.risuscito.utils.extension.capitalize

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebViewDialog(
    dialogTitleRes: Int = 0,
    iconRes: Int = 0,
    htmlString: String,
    onDismissRequest: () -> Unit,
    buttonTextRes: Int = 0
) {

    // Stato per gestire la visibilità dello spinner
    var isLoading by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 24.dp)
            ) {
                if (iconRes > 0) {
                    Icon(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        painter = painterResource(iconRes),
                        contentDescription = "webview dialog",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                if (iconRes > 0 && dialogTitleRes > 0)
                    Spacer(modifier = Modifier.height(16.dp))
                if (dialogTitleRes > 0) DialogTitle(title = stringResource(dialogTitleRes))
                if (iconRes > 0 || dialogTitleRes > 0) Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp), // Altezza fissa o dinamica
                    contentAlignment = Alignment.Center // Centra il contenuto (lo spinner)
                ) {
                    // Use AndroidView factory correctly
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    AndroidView(
                        modifier = Modifier.matchParentSize(),
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                // Configurazione WebViewClient per gestire lo stato di caricamento
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(
                                        view: WebView?,
                                        url: String?,
                                        favicon: Bitmap?
                                    ) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                    }
                                }

                                settings.javaScriptEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                setBackgroundColor(0)
                            }
                        },
                        update = { webView ->
                            // This is called whenever htmlString changes
                            if (htmlString.isNotEmpty()) {
                                if (htmlString.startsWith("http") || htmlString.startsWith("file")) {
                                    webView.loadUrl(htmlString)
                                } else {
                                    webView.loadDataWithBaseURL(
                                        null,
                                        htmlString,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            }
                        }
                    )

                    // Visualizza lo spinner sopra la WebView se isLoading è true
                    if (isLoading) {
                        LoadingIndicator()
                    }
                }

                if (buttonTextRes > 0) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .align(Alignment.End)
                    ) {
                        TextButton(
                            onClick = {
                                onDismissRequest()
                            }
                        ) {
                            Text(stringResource(buttonTextRes).capitalize(LocalContext.current))
                        }
                    }
                }
            }
        }
    }
}