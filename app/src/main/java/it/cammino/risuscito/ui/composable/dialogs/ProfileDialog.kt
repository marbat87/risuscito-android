package it.cammino.risuscito.ui.composable.dialogs

import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil3.compose.AsyncImage
import it.cammino.risuscito.R
import it.cammino.risuscito.viewmodels.SharedProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialog(
    viewModel: SharedProfileViewModel,
    onItemClick: (ProfileMenuItem) -> Unit,
) {

    if (viewModel.showProfileDialog.value) {
        val coroutineScope: CoroutineScope = rememberCoroutineScope()
        val animateTrigger = remember { mutableStateOf(false) }
        LaunchedEffect(key1 = Unit) {
            launch {
                delay(100)
                animateTrigger.value = true
            }
        }

        Dialog(
            onDismissRequest = {
                coroutineScope.launch {
                    startDismissWithExitAnimation(
                        animateTrigger,
                        { viewModel.showProfileDialog.value = false })
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {

            val dialogWindow = getDialogWindow()

            SideEffect {
                dialogWindow.let { window ->
                    window?.setDimAmount(0f)
                    window?.setWindowAnimations(-1)
                }
            }

            AnimatedSlideInTransition(visible = animateTrigger.value) {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RoundedCornerShape(0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = viewModel.profileEmailStr.value,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    startDismissWithExitAnimation(
                                        animateTrigger,
                                        {
                                            viewModel.showProfileDialog.value = false
                                        })
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.close_24px),
                                    contentDescription = "Close"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = viewModel.profilePhotoUrl,
                            contentDescription = "Profile Button",
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(72.dp),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.account_circle_24px),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.profileNameStr.value,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(28.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .weight(weight = 1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                profileMenuItems.forEach { item ->
                                    ListItem(
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(item.iconRes),
                                                contentDescription = stringResource(item.label)
                                            )
                                        },
                                        headlineContent = {
                                            Text(stringResource(item.label))
                                        },
                                        modifier = Modifier.clickable {
                                            coroutineScope.launch {
                                                startDismissWithExitAnimation(
                                                    animateTrigger,
                                                    {
                                                        viewModel.showProfileDialog.value = false
                                                        onItemClick(item)
                                                    })
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ProfileMenuItem(
    val label: Int,
    val iconRes: Int,
) {
    GDRIVE_BACKUP(
        R.string.gdrive_backup,
        R.drawable.cloud_upload_24px
    ),

    GDRIVE_RESTORE(
        R.string.gdrive_restore,
        R.drawable.cloud_download_24px
    ),

    GDRIVE_REFRESH(
        R.string.gdrive_refresh,
        R.drawable.refresh_24px
    ),

    GOOGLE_SIGNOUT(
        R.string.gplus_signout,
        R.drawable.person_remove_24px
    ),

    GOOGLE_REVOKE(
        R.string.gplus_revoke,
        R.drawable.person_off_24px
    )

}

val profileMenuItems =
    listOf(
        ProfileMenuItem.GDRIVE_BACKUP,
        ProfileMenuItem.GDRIVE_RESTORE,
        ProfileMenuItem.GDRIVE_REFRESH,
        ProfileMenuItem.GOOGLE_SIGNOUT,
        ProfileMenuItem.GOOGLE_REVOKE
    )

//@Composable
//internal fun AnimatedScaleInTransition(
//    visible: Boolean,
//    content: @Composable AnimatedVisibilityScope.() -> Unit
//) {
//    AnimatedVisibility(
//        visible = visible,
//        enter = scaleIn(
//            animationSpec = tween(300)
//        ),
//        exit = scaleOut(
//            animationSpec = tween(300)
//        ),
//        content = content
//    )
//}

@Composable
internal fun AnimatedSlideInTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(300),
            // Fa entrare l'elemento partendo da destra (100% della larghezza)
            initialOffsetX = { fullWidth -> fullWidth }
        ),
        exit = slideOutHorizontally(
            animationSpec = tween(300),
            // Fa uscire l'elemento verso destra (100% della larghezza)
            targetOffsetX = { fullWidth -> fullWidth }
        ),
        content = content
    )
}

private suspend fun startDismissWithExitAnimation(
    animateTrigger: MutableState<Boolean>,
    onDismissRequest: () -> Unit
) {
    animateTrigger.value = false
    delay(300)
    onDismissRequest()
}

@ReadOnlyComposable
@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window