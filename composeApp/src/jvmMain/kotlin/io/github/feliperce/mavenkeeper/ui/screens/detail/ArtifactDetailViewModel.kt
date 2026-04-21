package io.github.feliperce.mavenkeeper.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.repository.ArtifactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtifactDetailViewModel(
    private val groupId: String,
    private val artifactId: String,
    private val repository: ArtifactRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ArtifactDetailUiState(groupId = groupId, artifactId = artifactId),
    )
    val state: StateFlow<ArtifactDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.groups.collect { groups ->
                val match = groups.firstOrNull {
                    it.groupId == groupId && it.artifactId == artifactId
                }
                _state.update { it.copy(group = match) }
            }
        }
    }

    fun onToggleExpanded(version: String) {
        _state.update { current ->
            val next = current.expandedVersions.toMutableSet()
            if (!next.add(version)) next.remove(version)
            current.copy(expandedVersions = next)
        }
    }

    fun onDeleteRequest(artifact: Artifact) {
        _state.update { it.copy(pendingDelete = artifact) }
    }

    fun onDeleteCancel() {
        _state.update { it.copy(pendingDelete = null) }
    }

    fun onDeleteConfirm() {
        val target = _state.value.pendingDelete ?: return
        _state.update {
            it.copy(pendingDelete = null, deleteStatus = ArtifactDetailUiState.DeleteStatus.InProgress)
        }
        viewModelScope.launch {
            val result = repository.delete(target)
            result
                .onSuccess {
                    _state.update {
                        it.copy(deleteStatus = ArtifactDetailUiState.DeleteStatus.Success(target.coordinate.version))
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            deleteStatus = ArtifactDetailUiState.DeleteStatus.Failed(
                                error.message ?: "Unknown error",
                            ),
                        )
                    }
                }
        }
    }

    fun onStatusConsumed() {
        _state.update { it.copy(deleteStatus = ArtifactDetailUiState.DeleteStatus.Idle) }
    }
}
