package io.github.feliperce.mavenkeeper.domain.model

import java.nio.file.Path
import java.time.Instant

data class Artifact(
    val coordinate: MavenCoordinate,
    val packaging: String,
    val sizeBytes: Long,
    val lastModified: Instant,
    val versionDirectory: Path,
    val pomPath: Path,
    val sha1: String?,
    val licenses: List<String>,
    val dependencies: List<PomDependency>,
) {
    val isSnapshot: Boolean get() = coordinate.version.endsWith("-SNAPSHOT", ignoreCase = true)
}
