package io.github.feliperce.mavenkeeper.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VersionChip(
    version: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    AssistChip(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        label = {
            Text(
                text = version,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = null,
        modifier = modifier,
    )
}
