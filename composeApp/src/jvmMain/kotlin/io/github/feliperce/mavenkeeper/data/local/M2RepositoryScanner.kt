package io.github.feliperce.mavenkeeper.data.local

import io.github.feliperce.mavenkeeper.data.parser.PomParser
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class M2RepositoryScanner(
    private val pomParser: PomParser,
) {
    data class ScanResult(
        val artifacts: List<Artifact>,
        val progress: ScanProgress,
    )

    fun scan(root: Path): Flow<ScanResult> = flow {
        if (!Files.isDirectory(root)) {
            emit(ScanResult(emptyList(), ScanProgress.Error("Repository directory not found: $root")))
            return@flow
        }

        val collected = mutableListOf<Artifact>()
        emit(ScanResult(emptyList(), ScanProgress.Scanning(0)))

        Files.walk(root).use { stream ->
            val pomPaths = stream.filter { path ->
                path.isRegularFile() && path.name.endsWith(".pom")
            }.iterator()

            var throttle = 0
            while (pomPaths.hasNext()) {
                val pom = pomPaths.next()
                val artifact = buildArtifact(pom) ?: continue
                collected += artifact
                throttle++
                if (throttle >= 50) {
                    emit(ScanResult(collected.toList(), ScanProgress.Scanning(collected.size)))
                    throttle = 0
                }
            }
        }

        val groupCount = collected.asSequence()
            .map { it.coordinate.groupArtifact }
            .distinct()
            .count()
        emit(ScanResult(collected.toList(), ScanProgress.Complete(collected.size, groupCount)))
    }

    private fun buildArtifact(pomPath: Path): Artifact? {
        val parsed = pomParser.parse(pomPath) ?: return null
        val versionDir = pomPath.parent ?: return null
        val lastModified = runCatching {
            Files.getLastModifiedTime(pomPath).toInstant()
        }.getOrDefault(Instant.EPOCH)
        val sizeBytes = versionDir.directorySizeBytes()
        return Artifact(
            coordinate = parsed.coordinate,
            packaging = parsed.packaging,
            sizeBytes = sizeBytes,
            lastModified = lastModified,
            versionDirectory = versionDir,
            pomPath = pomPath,
            dependencies = parsed.dependencies,
        )
    }

    private fun Path.directorySizeBytes(): Long = runCatching {
        listDirectoryEntries()
            .filter { it.isRegularFile() }
            .sumOf { runCatching { Files.size(it) }.getOrDefault(0L) }
    }.getOrDefault(0L)
}
