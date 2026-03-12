package com.filesigner.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.filesigner.R

@Composable
fun FilePickerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("filePickerButton"),
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.Outlined.FileOpen,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .testTag("filePickerButtonIcon")
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.choose_file),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("filePickerButtonText")
        )
    }
}
