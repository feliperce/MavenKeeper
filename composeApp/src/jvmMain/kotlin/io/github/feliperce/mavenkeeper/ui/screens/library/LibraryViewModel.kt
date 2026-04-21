package io.github.feliperce.mavenkeeper.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.feliperce.mavenkeeper.domain.repository.ArtifactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: ArtifactRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.groups,
                repository.progress,
                repository.repositoryRoot,
                query,
            ) { groups, progress, root, q ->
                LibraryUiState(
                    query = q,
                    allGroups = groups,
                    progress = progress,
                    repositoryRoot = root,
                )
            }.collect { new -> _state.value = new }
        }
    }

    fun onQueryChanged(newQuery: String) {
        query.update { newQuery }
    }

    fun onRescanClick() {
        viewModelScope.launch {
            repository.rescan()
        }
    }
}
