package io.github.feliperce.mavenkeeper.domain.model

data class GroupSummary(
    val groupId: String,
    val artifactCount: Int,
    val versionCount: Int,
    val totalSize: Long,
) {
    val shortName: String get() = groupId.substringAfterLast('.')
}
