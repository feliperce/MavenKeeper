package io.github.feliperce.mavenkeeper.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.PomDependency
import io.github.feliperce.mavenkeeper.ui.components.DeleteVersionDialog
import io.github.feliperce.mavenkeeper.ui.components.EmptyState
import io.github.feliperce.mavenkeeper.ui.components.ScopeBadge
import io.github.feliperce.mavenkeeper.ui.components.formatBytes
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArtifactDetailScreen(
    viewModel: ArtifactDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.deleteStatus) {
        when (val status = state.deleteStatus) {
            is ArtifactDetailUiState.DeleteStatus.Success -> {
                snackbar.showSnackbar("Version ${status.versionDeleted} deleted")
                viewModel.onStatusConsumed()
            }
            is ArtifactDetailUiState.DeleteStatus.Failed -> {
                snackbar.showSnackbar("Delete failed: ${status.message}")
                viewModel.onStatusConsumed()
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.group) {
        if (state.group == null && state.deleteStatus !is ArtifactDetailUiState.DeleteStatus.InProgress) {
            // group was removed entirely (all versions deleted) → go back
            onBack()
        }
    }

    state.pendingDelete?.let { target ->
        DeleteVersionDialog(
            artifact = target,
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteCancel,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.artifactId,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.groupId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(data) } },
    ) { innerPadding ->
        val group = state.group
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (group == null) {
                EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No versions",
                    description = "This artifact has no versions left in the local repository.",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = group.versions,
                        key = { it.versionDirectory.toString() },
                    ) { artifact ->
                        VersionCard(
                            artifact = artifact,
                            expanded = artifact.coordinate.version in state.expandedVersions,
                            onToggleExpanded = { viewModel.onToggleExpanded(artifact.coordinate.version) },
                            onDeleteClick = { viewModel.onDeleteRequest(artifact) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VersionCard(
    artifact: Artifact,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        onClick = onToggleExpanded,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artifact.coordinate.version,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${artifact.packaging} • ${artifact.sizeBytes.formatBytes()} • ${artifact.lastModified.format()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete version") },
                            onClick = {
                                menuOpen = false
                                onDeleteClick()
                            },
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider()
                    Text(
                        text = "Path",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        text = artifact.versionDirectory.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Dependencies (${artifact.dependencies.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                    if (artifact.dependencies.isEmpty()) {
                        Text(
                            text = "No declared dependencies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        artifact.dependencies.forEach { dep ->
                            DependencyRow(dep)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DependencyRow(dependency: PomDependency) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${dependency.coordinate.groupId}:${dependency.coordinate.artifactId}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dependency.coordinate.version,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ScopeBadge(dependency.scope)
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

private fun java.time.Instant.format(): String =
    dateFormatter.format(this.atZone(ZoneId.systemDefault()).toLocalDate())
