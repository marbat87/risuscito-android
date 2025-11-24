package it.cammino.risuscito.ui.composable.animations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut

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
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState,
        transitionSpec = {
            materialFadeThroughIn(durationMillis = duration) togetherWith materialFadeThroughOut(
                durationMillis = duration
            )
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