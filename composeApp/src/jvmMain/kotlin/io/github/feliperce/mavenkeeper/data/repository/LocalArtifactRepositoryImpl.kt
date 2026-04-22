package io.github.feliperce.mavenkeeper.data.repository

import io.github.feliperce.mavenkeeper.data.local.M2PathProvider
import io.github.feliperce.mavenkeeper.data.local.M2RepositoryScanner
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.GroupSummary
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import io.github.feliperce.mavenkeeper.domain.repository.ArtifactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class LocalArtifactRepositoryImpl(
    private val pathProvider: M2PathProvider,
    private val scanner: M2RepositoryScanner,
) : ArtifactRepository {

    private val _groups = MutableStateFlow<List<ArtifactGroup>>(emptyList())
    private val _groupSummaries = MutableStateFlow<List<GroupSummary>>(emptyList())
    private val _progress = MutableStateFlow<ScanProgress>(ScanProgress.Idle)
    private val _root = MutableStateFlow(pathProvider.resolve())
    private val scanMutex = Mutex()

    override val groups: StateFlow<List<ArtifactGroup>> = _groups.asStateFlow()
    override val groupSummaries: StateFlow<List<GroupSummary>> = _groupSummaries.asStateFlow()
    override val progress: StateFlow<ScanProgress> = _progress.asStateFlow()
    override val repositoryRoot: StateFlow<Path?> = _root.asStateFlow()

    override suspend fun rescan() = withContext(Dispatchers.IO) {
        scanMutex.withLock {
            val root = pathProvider.resolve()
            _root.value = root
            scanner.scan(root).collect { result ->
                _groups.value = aggregateGroups(result.artifacts)
                _groupSummaries.value = summarizeByGroupId(result.artifacts)
                _progress.value = result.progress
            }
        }
    }

    override suspend fun setRepositoryRoot(path: Path?) {
        pathProvider.setOverride(path)
        _root.value = pathProvider.resolve()
    }

    override suspend fun delete(artifact: Artifact): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            deleteVersionDir(artifact.versionDirectory)
            updateInMemoryAfterDelete(listOf(artifact))
        }
    }

    override suspend fun purgeSnapshots(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshots = _groups.value.flatMap { it.versions }.filter { it.isSnapshot }
            snapshots.forEach { runCatching { deleteVersionDir(it.versionDirectory) } }
            updateInMemoryAfterDelete(snapshots)
            snapshots.size
        }
    }

    private fun deleteVersionDir(dir: Path) {
        if (!Files.exists(dir)) return
        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun updateInMemoryAfterDelete(removed: List<Artifact>) {
        val removedDirs = removed.map { it.versionDirectory }.toSet()
        val updatedGroups = _groups.value
            .map { group ->
                group.copy(versions = group.versions.filterNot { it.versionDirectory in removedDirs })
            }
            .filter { it.versions.isNotEmpty() }
        _groups.value = updatedGroups
        _groupSummaries.value = summarizeByGroupId(updatedGroups.flatMap { it.versions })
    }

    private fun aggregateGroups(artifacts: List<Artifact>): List<ArtifactGroup> =
        artifacts
            .groupBy { it.coordinate.groupId to it.coordinate.artifactId }
            .map { (key, versions) ->
                val (groupId, artifactId) = key
                ArtifactGroup(
                    groupId = groupId,
                    artifactId = artifactId,
                    versions = versions.sortedByDescending { it.lastModified },
                )
            }
            .sortedBy { it.groupArtifact.lowercase() }

    private fun summarizeByGroupId(artifacts: List<Artifact>): List<GroupSummary> =
        artifacts
            .groupBy { it.coordinate.groupId }
            .map { (groupId, versions) ->
                val artifactCount = versions.map { it.coordinate.artifactId }.distinct().size
                GroupSummary(
                    groupId = groupId,
                    artifactCount = artifactCount,
                    versionCount = versions.size,
                    totalSize = versions.sumOf { it.sizeBytes },
                )
            }
            .sortedByDescending { it.totalSize }
}
