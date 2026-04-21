package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.feliperce.mavenkeeper.ui.components.DeleteVersionDialog
import io.github.feliperce.mavenkeeper.ui.components.PurgeSnapshotsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.transientMessage) {
        state.transientMessage?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.onMessageConsumed()
        }
    }

    state.pendingDelete?.let { target ->
        DeleteVersionDialog(
            artifact = target,
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteCancel,
        )
    }
    state.purgeConfirm?.let { confirm ->
        PurgeSnapshotsDialog(
            snapshotCount = confirm.snapshotCount,
            totalSize = confirm.totalSize,
            onConfirm = viewModel::onPurgeConfirm,
            onDismiss = viewModel::onPurgeCancel,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(data) } },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight(),
            ) {
                ArtifactListPane(
                    state = state,
                    onQueryChange = viewModel::onQueryChanged,
                    onFilterChange = viewModel::onFilterChanged,
                    onGroupClick = viewModel::onGroupSelected,
                    onArtifactClick = viewModel::onArtifactSelected,
                    onRescan = viewModel::onRescanClick,
                    onPurgeSnapshots = viewModel::onPurgeSnapshotsRequest,
                )
            }
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
            ) {
                ArtifactDetailPane(
                    state = state,
                    onOpenInFileManager = viewModel::onOpenInFileManager,
                    onDeleteRequest = viewModel::onDeleteRequest,
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 24.dp),
                )
            }
        }
    }
}
