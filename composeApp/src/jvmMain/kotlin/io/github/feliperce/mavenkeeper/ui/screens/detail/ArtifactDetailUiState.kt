package io.github.feliperce.mavenkeeper.ui.screens.detail

import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup

data class ArtifactDetailUiState(
    val groupId: String,
    val artifactId: String,
    val group: ArtifactGroup? = null,
    val expandedVersions: Set<String> = emptySet(),
    val pendingDelete: Artifact? = null,
    val deleteStatus: DeleteStatus = DeleteStatus.Idle,
) {
    val title: String get() = "$groupId:$artifactId"
    val shouldNavigateBack: Boolean get() = group == null && deleteStatus is DeleteStatus.Success

    sealed interface DeleteStatus {
        data object Idle : DeleteStatus
        data object InProgress : DeleteStatus
        data class Success(val versionDeleted: String) : DeleteStatus
        data class Failed(val message: String) : DeleteStatus
    }
}
