package it.cammino.risuscito.ui.composable.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import soup.compose.material.motion.MaterialMotion
import soup.compose.material.motion.MaterialSharedAxisZ
import soup.compose.material.motion.animation.materialFadeThrough

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

@Composable
fun <S> AnimatedFadeContent(
    targetState: S,
    duration: Int = 1000,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    MaterialMotion(
        targetState = targetState,
        transitionSpec = { materialFadeThrough(duration) }
    ) {
        content(it)
    }
}

@Composable
fun <S> AnimatedScaleContent(
    targetState: S,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    MaterialSharedAxisZ(
        targetState = targetState,
        forward = true
    ) {
        content(it)
    }
}