package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.PomDependency
import io.github.feliperce.mavenkeeper.ui.components.EmptyState
import io.github.feliperce.mavenkeeper.ui.components.ScopeBadge
import io.github.feliperce.mavenkeeper.ui.components.formatBytes
import java.nio.file.Path
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class DetailTab { VERSIONS, DEPENDENCIES }

@Composable
fun ArtifactDetailPane(
    state: LibraryUiState,
    onOpenInFileManager: (Path) -> Unit,
    onDeleteRequest: (Artifact) -> Unit,
    contentPadding: PaddingValues = PaddingValues(24.dp),
) {
    val group = state.selectedArtifact
    if (group == null) {
        EmptyState(
            icon = Icons.Filled.Inventory2,
            title = "Selecione um artefato",
            description = "Escolha um item da lista para ver detalhes.",
        )
        return
    }
    val latest = group.latest
    val dependencies = latest?.dependencies.orEmpty()
    val artifactFolder = latest?.versionDirectory?.parent
    var selectedTab by remember(group.groupArtifact) { mutableStateOf(DetailTab.VERSIONS) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item("header") {
            DetailHeader(
                group = group,
                artifactFolder = artifactFolder,
                onOpenInFileManager = onOpenInFileManager,
            )
            Spacer(Modifier.height(20.dp))
        }
        item("stats") {
            StatCards(group = group)
            Spacer(Modifier.height(24.dp))
        }
        item("tabs") {
            DetailTabs(
                selected = selectedTab,
                onSelectedChange = { selectedTab = it },
                versionsCount = group.versions.size,
                depsCount = dependencies.size,
            )
            Spacer(Modifier.height(16.dp))
        }
        when (selectedTab) {
            DetailTab.VERSIONS -> {
                item("versions-hint") {
                    Text(
                        text = "clique em uma versão para abrir sua pasta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                itemsIndexed(group.versions, key = { _, a -> a.versionDirectory.toString() }) { index, artifact ->
                    VersionRow(
                        artifact = artifact,
                        isLatest = index == 0,
                        onDelete = { onDeleteRequest(artifact) },
                        onOpen = { onOpenInFileManager(artifact.versionDirectory) },
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
            DetailTab.DEPENDENCIES -> {
                if (dependencies.isEmpty()) {
                    item("deps-empty") { DependenciesEmpty() }
                } else {
                    item("deps-info") {
                        Text(
                            text = "da versão ${latest!!.coordinate.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    items(dependencies, key = { "${it.coordinate}-${it.scope}" }) { dep ->
                        DependencyRow(dep)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    group: ArtifactGroup,
    artifactFolder: Path?,
    onOpenInFileManager: (Path) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.groupId,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = group.artifactId,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (artifactFolder != null) {
            Button(
                onClick = { onOpenInFileManager(artifactFolder) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    Icons.Filled.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(18.dp)
                        .height(18.dp),
                )
                Text("Abrir pasta")
            }
        }
    }
}

@Composable
private fun StatCards(group: ArtifactGroup) {
    val latest = group.latest
    val latestUsed = latest?.lastModified?.let { formatRelative(it) } ?: "—"
    val license = latest?.licenses?.firstOrNull() ?: "não declarada"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(label = "Versões", value = group.versions.size.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Tamanho total", value = group.totalSize.formatBytes(), modifier = Modifier.weight(1f))
        StatCard(label = "Último uso", value = latestUsed, modifier = Modifier.weight(1f))
        StatCard(label = "Licença", value = license, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun DetailTabs(
    selected: DetailTab,
    onSelectedChange: (DetailTab) -> Unit,
    versionsCount: Int,
    depsCount: Int,
) {
    SecondaryTabRow(
        selectedTabIndex = selected.ordinal,
        containerColor = Color.Transparent,
    ) {
        Tab(
            selected = selected == DetailTab.VERSIONS,
            onClick = { onSelectedChange(DetailTab.VERSIONS) },
            text = {
                TabLabel(
                    title = "Versões instaladas",
                    count = versionsCount,
                    active = selected == DetailTab.VERSIONS,
                )
            },
        )
        Tab(
            selected = selected == DetailTab.DEPENDENCIES,
            onClick = { onSelectedChange(DetailTab.DEPENDENCIES) },
            text = {
                TabLabel(
                    title = "Dependências",
                    count = depsCount,
                    active = selected == DetailTab.DEPENDENCIES,
                )
            },
        )
    }
}

@Composable
private fun TabLabel(title: String, count: Int, active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
        Box(
            modifier = Modifier
                .background(
                    if (active) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DependenciesEmpty() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.AccountTree,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Nenhuma dependência declarada",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VersionRow(
    artifact: Artifact,
    isLatest: Boolean,
    onDelete: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        onClick = onOpen,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = artifact.coordinate.version,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isLatest) {
                        Badge(
                            text = "LATEST",
                            tonal = MaterialTheme.colorScheme.primaryContainer,
                            onTonal = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    if (artifact.isSnapshot) {
                        Badge(
                            text = "SNAPSHOT",
                            tonal = MaterialTheme.colorScheme.tertiaryContainer,
                            onTonal = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
                Text(
                    text = "${artifact.sizeBytes.formatBytes()} · ${formatRelative(artifact.lastModified)} · ${artifact.packaging}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (artifact.sha1 != null) {
                Text(
                    text = artifact.sha1.take(10),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete ${artifact.coordinate.version}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Badge(text: String, tonal: Color, onTonal: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        ),
        color = onTonal,
        modifier = Modifier
            .background(tonal, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun DependencyRow(dependency: PomDependency) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(6.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${dependency.coordinate.groupId}:${dependency.coordinate.artifactId}",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dependency.coordinate.version,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        ScopeBadge(dependency.scope)
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

fun formatRelative(instant: java.time.Instant): String {
    val now = java.time.Instant.now()
    val hours = ChronoUnit.HOURS.between(instant, now)
    val days = ChronoUnit.DAYS.between(instant, now)
    return when {
        hours < 1 -> "agora"
        hours < 24 -> "${hours}h atrás"
        days < 7 -> "${days}d atrás"
        days < 30 -> "${days / 7} sem atrás"
        days < 365 -> "${days / 30} mês atrás"
        else -> dateFormatter.format(instant.atZone(ZoneId.systemDefault()).toLocalDate())
    }
}
