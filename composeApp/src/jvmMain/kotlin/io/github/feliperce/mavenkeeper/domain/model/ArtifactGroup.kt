package io.github.feliperce.mavenkeeper.domain.model

data class ArtifactGroup(
    val groupId: String,
    val artifactId: String,
    val versions: List<Artifact>,
) {
    val groupArtifact: String get() = "$groupId:$artifactId"

    val totalSize: Long get() = versions.sumOf { it.sizeBytes }

    val latest: Artifact? get() = versions.maxByOrNull { it.lastModified }
}
