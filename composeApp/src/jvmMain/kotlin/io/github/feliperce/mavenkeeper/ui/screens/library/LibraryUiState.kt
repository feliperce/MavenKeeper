package io.github.feliperce.mavenkeeper.ui.screens.library

import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import java.nio.file.Path

data class LibraryUiState(
    val query: String = "",
    val allGroups: List<ArtifactGroup> = emptyList(),
    val progress: ScanProgress = ScanProgress.Idle,
    val repositoryRoot: Path? = null,
) {
    val visibleGroups: List<ArtifactGroup>
        get() = if (query.isBlank()) {
            allGroups
        } else {
            val q = query.trim().lowercase()
            allGroups.filter { group ->
                group.artifactId.lowercase().contains(q) ||
                    group.groupId.lowercase().contains(q)
            }
        }

    val isScanning: Boolean get() = progress is ScanProgress.Scanning
    val hasArtifacts: Boolean get() = allGroups.isNotEmpty()
}
