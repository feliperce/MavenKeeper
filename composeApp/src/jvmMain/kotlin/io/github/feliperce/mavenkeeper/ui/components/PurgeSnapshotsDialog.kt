package io.github.feliperce.mavenkeeper.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.feliperce.mavenkeeper.ui.theme.MavenKeeperTheme
import mavenkeeper.composeapp.generated.resources.Res
import mavenkeeper.composeapp.generated.resources.purge_dialog_cancel
import mavenkeeper.composeapp.generated.resources.purge_dialog_confirm
import mavenkeeper.composeapp.generated.resources.purge_dialog_text
import mavenkeeper.composeapp.generated.resources.purge_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PurgeSnapshotsDialog(
    snapshotCount: Int,
    totalSize: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.purge_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    Res.string.purge_dialog_text,
                    snapshotCount,
                    totalSize.formatBytes(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(Res.string.purge_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.purge_dialog_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun PurgeSnapshotsDialogPreview() {
    MavenKeeperTheme {
        PurgeSnapshotsDialog(
            snapshotCount = 12,
            totalSize = 45_000_000,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
