package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import io.github.feliperce.mavenkeeper.ui.components.ArtifactListItem
import io.github.feliperce.mavenkeeper.ui.components.EmptyState
import io.github.feliperce.mavenkeeper.ui.components.LoadingState
import io.github.feliperce.mavenkeeper.ui.components.SearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onArtifactClick: (groupId: String, artifactId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Maven Library") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                SearchField(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
        floatingActionButton = {
            SmallFloatingActionButton(onClick = viewModel::onRescanClick) {
                Icon(Icons.Filled.Refresh, contentDescription = "Rescan")
            }
        },
    ) { innerPadding ->
        LibraryContent(
            state = state,
            padding = innerPadding,
            onRescan = viewModel::onRescanClick,
            onArtifactClick = onArtifactClick,
        )
    }
}

@Composable
private fun LibraryContent(
    state: LibraryUiState,
    padding: PaddingValues,
    onRescan: () -> Unit,
    onArtifactClick: (groupId: String, artifactId: String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        when {
            state.progress is ScanProgress.Error -> {
                EmptyState(
                    icon = Icons.Filled.FolderOff,
                    title = "Repository not found",
                    description = state.progress.message,
                    action = {
                        Button(onClick = onRescan) { Text("Try again") }
                    },
                )
            }

            state.isScanning && !state.hasArtifacts -> {
                val count = (state.progress as? ScanProgress.Scanning)?.discovered ?: 0
                LoadingState(label = "Scanning local repository… ($count found)")
            }

            !state.hasArtifacts -> {
                EmptyState(
                    icon = Icons.Filled.FolderOff,
                    title = "Your local repository is empty",
                    description = state.repositoryRoot?.toString()
                        ?: "No .m2 directory detected on this machine.",
                    action = {
                        Button(onClick = onRescan) { Text("Rescan") }
                    },
                )
            }

            state.visibleGroups.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.SearchOff,
                    title = "No matches",
                    description = "Try a different search term.",
                )
            }

            else -> {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isScanning) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Scanning… ${(state.progress as ScanProgress.Scanning).discovered} found",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    items(
                        items = state.visibleGroups,
                        key = { "${it.groupId}:${it.artifactId}" },
                    ) { group ->
                        ArtifactListItem(
                            group = group,
                            onClick = { onArtifactClick(group.groupId, group.artifactId) },
                        )
                    }
                }
            }
        }
    }
}
