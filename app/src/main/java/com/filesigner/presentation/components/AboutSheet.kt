package com.filesigner.presentation.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filesigner.R
import com.filesigner.presentation.theme.FileSignerTheme
import timber.log.Timber

private const val APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0"

private data class LibraryInfo(val name: String, val version: String)

private val runtimeLibraries = listOf(
    LibraryInfo("AndroidX Core KTX", "1.15.0"),
    LibraryInfo("AndroidX Lifecycle Runtime KTX", "2.8.7"),
    LibraryInfo("AndroidX Activity Compose", "1.9.3"),
    LibraryInfo("Jetpack Compose (BOM)", "2024.12.01"),
    LibraryInfo("AndroidX Lifecycle ViewModel Compose", "2.8.7"),
    LibraryInfo("AndroidX Lifecycle Runtime Compose", "2.8.7"),
    LibraryInfo("Dagger Hilt Android", "2.59.2"),
    LibraryInfo("AndroidX Hilt Navigation Compose", "1.2.0"),
    LibraryInfo("Timber", "5.0.1"),
    LibraryInfo("Material3 Adaptive", ""),
    LibraryInfo("Material3 Window Size Class", "")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(
    versionName: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        AboutSheetContent(
            versionName = versionName,
            onLicenseLinkClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(APACHE_LICENSE_URL)))
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e, "No browser available to open license URL")
                }
            },
            onGithubLinkClick = { url ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e, "No browser available to open GitHub URL")
                }
            }
        )
    }
}

@Composable
internal fun AboutSheetContent(
    versionName: String,
    onLicenseLinkClick: () -> Unit = {},
    onGithubLinkClick: (String) -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .semantics { contentDescription = "About File Signer" }
    ) {
        // App Info
        item(key = "appInfo") {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 0)) +
                        slideInVertically(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 0)) { it / 2 }
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .testTag("aboutSheetTitle")
                            .semantics { heading() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.about_version, versionName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("aboutSheetVersion")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.about_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("aboutSheetDescription")
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Open-Source Licenses
        item(key = "licenses") {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 100)) +
                        slideInVertically(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 100)) { it / 2 }
            ) {
                Column(modifier = Modifier.testTag("aboutSheetLicensesSection")) {
                    Text(
                        text = stringResource(R.string.about_open_source_licenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.about_apache_2_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onLicenseLinkClick,
                        modifier = Modifier.testTag("aboutSheetLicenseLink")
                    ) {
                        Text(
                            text = APACHE_LICENSE_URL,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Library list
        items(runtimeLibraries, key = { it.name }) { lib ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 200)) +
                        slideInVertically(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 200)) { it / 2 }
            ) {
                val display = if (lib.version.isNotEmpty()) {
                    "\u2022  ${lib.name} ${lib.version}"
                } else {
                    "\u2022  ${lib.name}"
                }
                Text(
                    text = display,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        // Developer
        item(key = "developer") {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 300)) +
                        slideInVertically(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 300)) { it / 2 }
            ) {
                Column(modifier = Modifier.testTag("aboutSheetDeveloperSection")) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.about_developer),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val github = stringResource(R.string.about_developer_github)
                    TextButton(
                        onClick = { onGithubLinkClick("https://$github") },
                        modifier = Modifier.testTag("aboutSheetGithubLink")
                    ) {
                        Text(
                            text = github,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AboutSheetContentPreview() {
    FileSignerTheme {
        Surface {
            AboutSheetContent(versionName = "1.0.0")
        }
    }
}
