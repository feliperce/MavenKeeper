package io.github.feliperce.mavenkeeper.domain.repository

import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.GroupSummary
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

interface ArtifactRepository {
    val groups: StateFlow<List<ArtifactGroup>>
    val groupSummaries: StateFlow<List<GroupSummary>>
    val progress: StateFlow<ScanProgress>
    val repositoryRoot: StateFlow<Path?>

    suspend fun rescan()
    suspend fun setRepositoryRoot(path: Path?)
    suspend fun delete(artifact: Artifact): Result<Unit>
    suspend fun purgeSnapshots(): Result<Int>
}
