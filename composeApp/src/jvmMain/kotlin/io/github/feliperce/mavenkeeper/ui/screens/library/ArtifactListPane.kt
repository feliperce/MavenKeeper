package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.GroupSummary
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import io.github.feliperce.mavenkeeper.ui.components.EmptyState
import io.github.feliperce.mavenkeeper.ui.components.LoadingState
import io.github.feliperce.mavenkeeper.ui.components.SearchField
import io.github.feliperce.mavenkeeper.ui.components.formatBytes

@Composable
fun ArtifactListPane(
    state: LibraryUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilter) -> Unit,
    onGroupClick: (String?) -> Unit,
    onArtifactClick: (ArtifactGroup) -> Unit,
    onRescan: () -> Unit,
    onPurgeSnapshots: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListTopBar(
            state = state,
            onRescan = onRescan,
            onPurgeSnapshots = onPurgeSnapshots,
        )
        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = "Pesquisar groupId:artifactId",
        )
        FilterChipRow(
            current = state.filter,
            onFilterChange = onFilterChange,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.progress is ScanProgress.Error -> {
                    EmptyState(
                        icon = Icons.Filled.FolderOff,
                        title = "Repository not found",
                        description = state.progress.message,
                        action = {
                            TextButton(onClick = onRescan) { Text("Tentar novamente") }
                        },
                    )
                }
                state.isScanning && !state.hasArtifacts -> {
                    val count = (state.progress as? ScanProgress.Scanning)?.discovered ?: 0
                    LoadingState(label = "Escaneando… ($count encontrados)")
                }
                !state.hasArtifacts -> {
                    EmptyState(
                        icon = Icons.Filled.FolderOff,
                        title = "Repositório vazio",
                        description = state.repositoryRoot?.toString() ?: ".m2 não detectado",
                        action = {
                            TextButton(onClick = onRescan) { Text("Rescan") }
                        },
                    )
                }
                else -> {
                    ListContent(
                        state = state,
                        onGroupClick = onGroupClick,
                        onArtifactClick = onArtifactClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListTopBar(
    state: LibraryUiState,
    onRescan: () -> Unit,
    onPurgeSnapshots: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ".m2 / repository",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${state.totalArtifacts} artefatos · ${state.totalVersions} versões · ${state.totalSize.formatBytes()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onPurgeSnapshots) {
            Icon(
                Icons.Filled.CleaningServices,
                contentDescription = "Purgar snapshots",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRescan) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Rescan",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    current: LibraryFilter,
    onFilterChange: (LibraryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LibraryFilter.entries.forEach { filter ->
            FilterChip(
                selected = current == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun ListContent(
    state: LibraryUiState,
    onGroupClick: (String?) -> Unit,
    onArtifactClick: (ArtifactGroup) -> Unit,
) {
    val summariesToShow = state.groupSummaries.take(8)
    val visibleArtifacts = state.artifactsInSelectedGroup

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (summariesToShow.isNotEmpty()) {
            item("groups-header") {
                SectionLabel("GROUPS (${state.groupSummaries.size})")
            }
            items(summariesToShow, key = { "g-${it.groupId}" }) { summary ->
                GroupRow(
                    summary = summary,
                    selected = state.selectedGroupId == summary.groupId,
                    onClick = { onGroupClick(summary.groupId) },
                )
            }
        }

        item("artifacts-header") {
            val label = state.selectedGroupId?.substringAfterLast('.')?.uppercase()
                ?: "TODOS OS ARTEFATOS"
            SectionLabel("ARTEFATOS${state.selectedGroupId?.let { " DE $label" }.orEmpty()}")
        }

        if (visibleArtifacts.isEmpty()) {
            item("artifacts-empty") {
                EmptyState(
                    icon = Icons.Filled.SearchOff,
                    title = "Nada aqui",
                    description = "Ajuste filtros ou busca.",
                    modifier = Modifier.padding(top = 40.dp),
                )
            }
        } else {
            items(visibleArtifacts, key = { "${it.groupId}:${it.artifactId}" }) { group ->
                ArtifactRow(
                    group = group,
                    selected = state.selectedArtifactKey == "${group.groupId}:${group.artifactId}",
                    onClick = { onArtifactClick(group) },
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 6.dp),
    )
}

@Composable
private fun GroupRow(
    summary: GroupSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(10.dp),
                    ),
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.groupId,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${summary.artifactCount} artefatos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = summary.totalSize.formatBytes(),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ArtifactRow(
    group: ArtifactGroup,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TypeBadge(group.versions.firstOrNull()?.packaging?.uppercase() ?: "?")
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.artifactId,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                ArtifactRowMeta(group)
            }
        }
    }
}

@Composable
private fun ArtifactRowMeta(group: ArtifactGroup) {
    val latest = group.latest
    val text = buildAnnotatedString {
        if (latest != null) {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                append(latest.coordinate.version)
            }
            append(" · ")
        }
        append("${group.versions.size} versões · ${group.totalSize.formatBytes()}")
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TypeBadge(type: String) {
    Text(
        text = type,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

