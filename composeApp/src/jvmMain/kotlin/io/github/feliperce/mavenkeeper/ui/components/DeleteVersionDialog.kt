package io.github.feliperce.mavenkeeper.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.ui.screens.library.LibraryPreviewSamples
import io.github.feliperce.mavenkeeper.ui.theme.MavenKeeperTheme
import mavenkeeper.composeapp.generated.resources.Res
import mavenkeeper.composeapp.generated.resources.delete_dialog_cancel
import mavenkeeper.composeapp.generated.resources.delete_dialog_confirm
import mavenkeeper.composeapp.generated.resources.delete_dialog_text
import mavenkeeper.composeapp.generated.resources.delete_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteVersionDialog(
    artifact: Artifact,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    Res.string.delete_dialog_text,
                    artifact.coordinate.toString(),
                    artifact.sizeBytes.formatBytes(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(Res.string.delete_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.delete_dialog_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun DeleteVersionDialogPreview() {
    MavenKeeperTheme {
        DeleteVersionDialog(
            artifact = LibraryPreviewSamples.artifact(),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
