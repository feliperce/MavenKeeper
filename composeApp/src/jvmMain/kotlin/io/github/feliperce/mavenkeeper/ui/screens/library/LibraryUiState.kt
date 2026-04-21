package io.github.feliperce.mavenkeeper.ui.screens.library

import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.GroupSummary
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class LibraryFilter(val label: String) {
    ALL("Todos"),
    RECENT("Recentes"),
    LARGEST("Maiores"),
    SNAPSHOTS("Snapshots"),
    STALE("Stale"),
}

data class LibraryUiState(
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter.ALL,
    val allGroups: List<ArtifactGroup> = emptyList(),
    val groupSummaries: List<GroupSummary> = emptyList(),
    val progress: ScanProgress = ScanProgress.Idle,
    val repositoryRoot: Path? = null,
    val selectedGroupId: String? = null,
    val selectedArtifactKey: String? = null,
    val pendingDelete: Artifact? = null,
    val purgeConfirm: PurgeConfirmation? = null,
    val transientMessage: String? = null,
) {
    val isScanning: Boolean get() = progress is ScanProgress.Scanning
    val hasArtifacts: Boolean get() = allGroups.isNotEmpty()

    val filteredGroups: List<ArtifactGroup> by lazy {
        allGroups
            .asSequence()
            .filter { group -> matchesQuery(group) }
            .let { seq ->
                when (filter) {
                    LibraryFilter.ALL -> seq
                    LibraryFilter.RECENT -> seq.filter { it.versions.any { v -> v.lastModified.isAfter(thirtyDaysAgo) } }
                    LibraryFilter.LARGEST -> seq.sortedByDescending { it.totalSize }
                    LibraryFilter.SNAPSHOTS -> seq.filter { it.versions.any { v -> v.isSnapshot } }
                    LibraryFilter.STALE -> seq.filter { it.versions.none { v -> v.lastModified.isAfter(sixMonthsAgo) } }
                }
            }
            .toList()
    }

    val artifactsInSelectedGroup: List<ArtifactGroup>
        get() = selectedGroupId?.let { gid ->
            filteredGroups.filter { it.groupId == gid }
        } ?: filteredGroups

    val selectedArtifact: ArtifactGroup?
        get() {
            val key = selectedArtifactKey ?: return null
            return allGroups.firstOrNull { "${it.groupId}:${it.artifactId}" == key }
        }

    val totalSize: Long get() = allGroups.sumOf { it.totalSize }
    val totalVersions: Int get() = allGroups.sumOf { it.versions.size }
    val totalArtifacts: Int get() = allGroups.size

    private fun matchesQuery(group: ArtifactGroup): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return group.artifactId.lowercase().contains(q) ||
            group.groupId.lowercase().contains(q)
    }

    data class PurgeConfirmation(val snapshotCount: Int, val totalSize: Long)

    private companion object {
        val thirtyDaysAgo: Instant get() = Instant.now().minus(30, ChronoUnit.DAYS)
        val sixMonthsAgo: Instant get() = Instant.now().minus(180, ChronoUnit.DAYS)
    }
}
