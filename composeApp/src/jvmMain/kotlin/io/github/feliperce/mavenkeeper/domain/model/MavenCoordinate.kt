package io.github.feliperce.mavenkeeper.domain.model

data class MavenCoordinate(
    val groupId: String,
    val artifactId: String,
    val version: String,
) {
    override fun toString(): String = "$groupId:$artifactId:$version"

    val groupArtifact: String get() = "$groupId:$artifactId"
}
