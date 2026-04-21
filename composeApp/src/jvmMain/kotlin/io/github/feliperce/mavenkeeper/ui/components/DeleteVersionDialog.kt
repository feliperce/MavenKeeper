package io.github.feliperce.mavenkeeper.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.feliperce.mavenkeeper.domain.model.Artifact

@Composable
fun DeleteVersionDialog(
    artifact: Artifact,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete version?") },
        text = {
            Text(
                buildString {
                    append("This will permanently delete ")
                    append(artifact.coordinate.toString())
                    append(" from disk, freeing ")
                    append(artifact.sizeBytes.formatBytes())
                    append(". This action cannot be undone.")
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
