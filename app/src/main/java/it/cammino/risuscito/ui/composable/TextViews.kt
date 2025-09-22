package it.cammino.risuscito.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun BottomSheetTitle(title: String = "Titolo") {
    Text(
        text = title,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(56.dp)
            .padding(horizontal = 24.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
    )
}

@Composable
@Preview
fun GridItemTitle(title: String = "Elemento") {
    Text(
        text = title,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier
            .height(16.dp)
            .padding(0.dp)
    )
}

@Composable
@Preview
fun PageText(title: String = "15", color: Long = 0xFFFCFCFC) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.inversePrimary, CircleShape)
            .background(Color(color)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
@Preview
fun ContextualToolbarTitle(title: String = "1 selezionato") {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(76.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = risuscito_medium_font,
        )
    }
}