package it.cammino.risuscito.ui.composable.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.layoutMinMargins
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.OSUtils

@Composable
fun AppInfoScreen(
    modifier: Modifier,
    versionName: String,
    versionCode: Long,
    onChangelogClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {

    RisuscitoTheme {

        Column(
            modifier = modifier.verticalScroll(rememberScrollState()).padding(layoutMinMargins()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // --- SEZIONE HERO (Icona e Nome) ---
            AppHeaderSection(versionName = versionName, versionCode = versionCode)

            // --- SEZIONE DESCRIZIONE ---
//            DescriptionSection()
            MainActionsSection(
                onChangelogClick = onChangelogClick,
                onPrivacyClick = onPrivacyClick
            )

            // --- SEZIONE SVILUPPATORE ---
            DeveloperSection()

            OtherActionsSection()
        }

    }
}

@Composable
fun AppHeaderSection(
    versionName: String,
    versionCode: Long
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icona dell'app con forma "Squircle" o molto arrotondata (Expressive)
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp), // Molto arrotondato
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 0.dp // M3 preferisce tonalità piuttosto che ombre
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Sostituire con l'icona reale dell'app
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder
                    contentDescription = "App Icon",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge della versione a pillola
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "${stringResource(R.string.version)} $versionName ($versionCode)",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

//@Composable
//fun DescriptionSection() {
//    // Card "Piatta" ma colorata per separare il contenuto
//    Surface(
//        shape = RoundedCornerShape(24.dp), // Angoli tipici Expressive
//        color = MaterialTheme.colorScheme.surfaceContainerLow
//    ) {
//        Column(modifier = Modifier.padding(24.dp)) {
//            Text(
//                text = "About",
//                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            Text(
//                text = "Questa applicazione è progettata per semplificare la tua vita quotidiana con un'interfaccia fluida e moderna. Costruita interamente con Jetpack Compose seguendo le linee guida Material 3.",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                lineHeight = 24.sp
//            )
//        }
//    }
//}

@Composable
fun MainActionsSection(
    onChangelogClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(28.dp), // Angoli extra large
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Pulsanti Azione (Stile grande e accessibile)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ContactRow(
                    icon = painterResource(id = R.drawable.policy_24px),
                    label = stringResource(id = R.string.privacy),
                    onClick = onPrivacyClick
                )
                ContactRow(
                    icon = painterResource(id = R.drawable.list_alt_24px),
                    label = stringResource(id = R.string.changelog),
                    onClick = onChangelogClick
                )
            }
        }
    }
}

@Composable
fun DeveloperSection() {

    val ctx = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(id = R.string.mal_developer),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )

        // Card Sviluppatore Grande
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(28.dp), // Angoli extra large
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Avatar e Nome
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("MB", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Marbat87",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Engineer & Designer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Pulsanti Azione (Stile grande e accessibile)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    ContactRow(
//                        icon = painterResource(id = R.drawable.language_24px),
//                        label = "Visita il sito web",
//                        onClick = onWebClick
//                    )
                    ContactRow(
                        icon = painterResource(id = R.drawable.mail_24px),
                        label = stringResource(id = R.string.mal_send_email),
                        onClick = {
                            OSUtils.sendMailOnClickAction(ctx)
                        }
                    )
                    ContactRow(
                        icon = painterResource(id = R.drawable.code_24px),
                        label = "Codice sorgente su GitHub",
                        onClick = {
                            OSUtils.createWebsiteOnClickAction(
                                ctx,
                                "https://github.com/marbat87/risuscito-android".toUri()
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OtherActionsSection() {

    val ctx = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(28.dp), // Angoli extra large
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Pulsanti Azione (Stile grande e accessibile)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ContactRow(
                    icon = painterResource(id = R.drawable.star_24px),
                    label = stringResource(id = R.string.rate_five_stars),
                    onClick = { OSUtils.rateOnClickAction(ctx) }
                )
                ContactRow(
                    icon = painterResource(id = R.drawable.share_24px),
                    label = stringResource(id = R.string.share_app),
                    onClick = { OSUtils.shareAppOnClickAction(ctx) }
                )
                ContactRow(
                    icon = painterResource(id = R.drawable.file_download_24px),
                    label = stringResource(id = R.string.update_app),
                    onClick = { OSUtils.rateOnClickAction(ctx) }
                )
            }
        }
    }
}

@Composable
fun ContactRow(
    icon: Painter,
    label: String,
    onClick: () -> Unit
) {
    // Usiamo Surface cliccabile invece di ListItem standard per più controllo sullo stile
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent, // Trasparente per fondersi con la card
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            // Freccetta opzionale o icona esterna
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppInfoScreenPreview() {
    MaterialTheme {
        AppInfoScreen(
            versionName = "2.4.0",
            versionCode = 152,
            modifier = Modifier,
            onChangelogClick = {},
            onPrivacyClick = {})
    }
}