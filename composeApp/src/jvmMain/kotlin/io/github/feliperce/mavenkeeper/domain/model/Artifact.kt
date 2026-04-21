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
    val dependencies: List<PomDependency>,
)
