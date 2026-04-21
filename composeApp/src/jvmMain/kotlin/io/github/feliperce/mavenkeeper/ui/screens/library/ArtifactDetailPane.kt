package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item("header") {
            DetailHeader(group = group, onOpenInFileManager = onOpenInFileManager)
            Spacer(Modifier.height(20.dp))
        }
        item("stats") {
            StatCards(group = group)
            Spacer(Modifier.height(24.dp))
        }
        item("versions-header") {
            SectionHeader(
                title = "Versões instaladas",
                hint = "clique em uma versão para abrir no Finder",
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
        val latest = group.latest
        if (latest != null && latest.dependencies.isNotEmpty()) {
            item("deps-header") {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = "Dependências",
                    hint = "de ${latest.coordinate.version}",
                )
            }
            items(latest.dependencies, key = { "${it.coordinate}-${it.scope}" }) { dep ->
                DependencyRow(dep)
            }
        }
    }
}

@Composable
private fun DetailHeader(
    group: ArtifactGroup,
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
            val latestLicense = group.latest?.licenses?.firstOrNull()
            if (latestLicense != null) {
                Text(
                    text = "Licença: $latestLicense",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        Button(
            onClick = { onOpenInFileManager(group.latest?.versionDirectory ?: return@Button) },
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
            Text("Abrir no Finder")
        }
    }
}

@Composable
private fun StatCards(group: ArtifactGroup) {
    val latest = group.latest
    val latestUsed = latest?.lastModified?.let { formatRelative(it) } ?: "—"
    val license = latest?.licenses?.firstOrNull() ?: "não declarada"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(label = "Versões", value = group.versions.size.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Tamanho total", value = group.totalSize.formatBytes(), modifier = Modifier.weight(1f))
        StatCard(label = "Último uso", value = latestUsed, modifier = Modifier.weight(1f))
        StatCard(label = "Licença", value = license, valueStyleSmall = true, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyleSmall: Boolean = false,
) {
    Card(
        modifier = modifier,
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
                style = if (valueStyleSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, hint: String? = null) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
