package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import it.cammino.risuscito.R

@Composable
fun MenuExpandableItem(
    text: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = { onClick() },
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.arrow_right_24px),
                contentDescription = null
            )
        }
    )
}

@Composable
fun MenuSelectabletem(
    text: String,
    onSelect: (Boolean) -> Unit,
    selected: Boolean = false
) {

    DropdownMenuItem(
        text = { Text(text) },
        onClick = {
            onSelect(!selected)
        },
        trailingIcon = {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onSelect(it)
                }
            )
        }
    )
}

@Composable
fun AccountMenuImage(
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    loggedIn: Boolean,
    profilePhotoUrl: String
) {
    IconButton(
        onClick = {
            if (loggedIn) onProfileClick() else onLoginClick()
        }
    ) {
        if (loggedIn) {
            AsyncImage(
                model = profilePhotoUrl,
                contentDescription = "Profile Button",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(32.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.account_circle_24px),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.google_icon_56dp),
                contentDescription = "Login Button",
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}