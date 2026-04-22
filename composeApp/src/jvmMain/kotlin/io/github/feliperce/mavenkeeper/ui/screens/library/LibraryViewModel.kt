package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.feliperce.mavenkeeper.data.local.FileManager
import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.repository.ArtifactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path

class LibraryViewModel(
    private val repository: ArtifactRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.groups,
                repository.groupSummaries,
                repository.progress,
                repository.repositoryRoot,
            ) { groups, summaries, progress, root ->
                Quad(groups, summaries, progress, root)
            }.collect { (groups, summaries, progress, root) ->
                _state.update { current ->
                    current.copy(
                        allGroups = groups,
                        groupSummaries = summaries,
                        progress = progress,
                        repositoryRoot = root,
                        selectedArtifactKey = current.selectedArtifactKey?.takeIf { key ->
                            groups.any { "${it.groupId}:${it.artifactId}" == key }
                        } ?: groups.firstOrNull()?.let { "${it.groupId}:${it.artifactId}" },
                    )
                }
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _state.update { it.copy(query = newQuery) }
    }

    fun onFilterChanged(filter: LibraryFilter) {
        _state.update { it.copy(filter = filter) }
    }

    fun onGroupSelected(groupId: String?) {
        _state.update { it.copy(selectedGroupId = if (it.selectedGroupId == groupId) null else groupId) }
    }

    fun onArtifactSelected(group: ArtifactGroup) {
        _state.update { it.copy(selectedArtifactKey = "${group.groupId}:${group.artifactId}") }
    }

    fun onRescanClick() {
        viewModelScope.launch { repository.rescan() }
    }

    fun onOpenInFileManager(path: Path) {
        val result = FileManager.open(path)
        result.onFailure { err ->
            _state.update {
                it.copy(transientMessage = TransientMessage.OpenFailure(err.message.orEmpty()))
            }
        }.onSuccess {
            _state.update { it.copy(transientMessage = TransientMessage.OpenSuccess) }
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
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            repository.delete(target)
                .onSuccess {
                    _state.update {
                        it.copy(transientMessage = TransientMessage.VersionDeleted(target.coordinate.version))
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(transientMessage = TransientMessage.DeleteFailed(err.message.orEmpty()))
                    }
                }
        }
    }

    fun onPurgeSnapshotsRequest() {
        val snapshots = _state.value.allGroups.flatMap { it.versions }.filter { it.isSnapshot }
        if (snapshots.isEmpty()) {
            _state.update { it.copy(transientMessage = TransientMessage.NoSnapshots) }
            return
        }
        _state.update {
            it.copy(
                purgeConfirm = LibraryUiState.PurgeConfirmation(
                    snapshotCount = snapshots.size,
                    totalSize = snapshots.sumOf { s -> s.sizeBytes },
                ),
            )
        }
    }

    fun onPurgeCancel() {
        _state.update { it.copy(purgeConfirm = null) }
    }

    fun onPurgeConfirm() {
        _state.update { it.copy(purgeConfirm = null) }
        viewModelScope.launch {
            repository.purgeSnapshots()
                .onSuccess { count ->
                    _state.update { it.copy(transientMessage = TransientMessage.Purged(count)) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(transientMessage = TransientMessage.PurgeFailed(err.message.orEmpty()))
                    }
                }
        }
    }

    fun onMessageConsumed() {
        _state.update { it.copy(transientMessage = null) }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
