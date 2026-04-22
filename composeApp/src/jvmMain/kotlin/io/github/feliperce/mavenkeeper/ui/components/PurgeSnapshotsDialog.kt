package io.github.feliperce.mavenkeeper.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.feliperce.mavenkeeper.ui.theme.MavenKeeperTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PurgeSnapshotsDialog(
    snapshotCount: Int,
    totalSize: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Purgar snapshots?") },
        text = {
            Text(
                "Isso irá remover $snapshotCount versão(ões) SNAPSHOT, liberando ${totalSize.formatBytes()}. " +
                    "Esta ação não pode ser desfeita.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Purgar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
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
