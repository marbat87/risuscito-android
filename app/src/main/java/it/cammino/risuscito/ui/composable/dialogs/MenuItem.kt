package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import it.cammino.risuscito.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenuExpandableItem(
    text: String,
    onClick: () -> Unit,
    iconRes: Int = 0,
    menuItemIndex: Int = 0,
    menuSize: Int = 0
) {
    if (iconRes > 0) {
        DropdownMenuItem(
            text = { Text(text) },
            shape = MenuDefaults.itemShape(menuItemIndex, menuSize).shape,
            onClick = onClick,
            trailingContent = {
                Icon(
                    modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                    painter = painterResource(R.drawable.arrow_right_24px),
                    contentDescription = null
                )
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                    painter = painterResource(iconRes),
                    contentDescription = text
                )
            }
        )
    } else {
        DropdownMenuItem(
            text = { Text(text) },
            shape = MenuDefaults.itemShape(menuItemIndex, menuSize).shape,
            onClick = onClick,
            trailingContent = {
                Icon(
                    modifier = Modifier.size(MenuDefaults.TrailingIconSize),
                    painter = painterResource(R.drawable.arrow_right_24px),
                    contentDescription = null
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenuSimpleItem(
    textRes: Int,
    onClick: () -> Unit,
    iconRes: Int = 0,
    menuItemIndex: Int = 0,
    menuSize: Int = 1
) {
    if (iconRes > 0) {
        DropdownMenuItem(
            text = { Text(stringResource(textRes)) },
            shape = MenuDefaults.itemShape(menuItemIndex, menuSize).shape,
            onClick = onClick,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(textRes)
                )
            }
        )
    } else {
        DropdownMenuItem(
            text = { Text(stringResource(textRes)) },
            shape = MenuDefaults.itemShape(menuItemIndex, menuSize).shape,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenuCheckableItem(
    text: String,
    onCheckedChange: (Boolean) -> Unit,
    checked: Boolean = false,
    itemIndex: Int = 0,
    itemsCount: Int = 1
) {

    CheckableDropdownMenuItem(
        checked = checked,
        text = { Text(text) },
        onCheckedChange = onCheckedChange,
        shapes = MenuDefaults.itemShape(itemIndex, itemsCount),
        leadingIcon = {
            Checkbox(
                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                checked = false,
                onCheckedChange = onCheckedChange
            )
        },
        checkedLeadingIcon = {
            Checkbox(
                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                checked = true,
                onCheckedChange = onCheckedChange
            )
        }
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountMenuImage(
    onClick: (Boolean) -> Unit,
    loggedIn: Boolean,
    profilePhotoUrl: String
) {
    IconButton(
        onClick = {
            onClick(loggedIn)
        }
    ) {
        if (loggedIn) {
            AsyncImage(
                model = profilePhotoUrl,
                contentDescription = "Profile Button",
                modifier = Modifier
                    .clip(MaterialShapes.Circle.toShape())
                    .size(32.dp),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(R.drawable.account_circle_24px),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.google_icon_56dp),
                contentDescription = "Login Button",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(32.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
            )
        }
    }
}