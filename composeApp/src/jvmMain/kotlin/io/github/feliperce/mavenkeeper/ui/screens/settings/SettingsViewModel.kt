package io.github.feliperce.mavenkeeper.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.feliperce.mavenkeeper.data.local.M2PathProvider
import io.github.feliperce.mavenkeeper.domain.repository.ArtifactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class SettingsViewModel(
    private val repository: ArtifactRepository,
    private val pathProvider: M2PathProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            resolvedPath = pathProvider.resolve().toString(),
            draftPath = pathProvider.currentOverride()?.toString().orEmpty(),
        ),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun onPathChanged(path: String) {
        _state.update { it.copy(draftPath = path, validationError = null) }
    }

    fun onApplyClick() {
        val raw = _state.value.draftPath.trim()
        val override = raw.takeIf { it.isNotEmpty() }?.let(::Path)
        if (override != null && !(override.exists() && override.isDirectory())) {
            _state.update { it.copy(validationError = SettingsValidationError.INVALID_PATH) }
            return
        }
        viewModelScope.launch {
            repository.setRepositoryRoot(override)
            val resolved = pathProvider.resolve().toString()
            _state.update { it.copy(resolvedPath = resolved, validationError = null) }
            repository.rescan()
        }
    }

    fun onRestoreDefaultClick() {
        viewModelScope.launch {
            repository.setRepositoryRoot(null)
            val resolved = pathProvider.resolve().toString()
            _state.update {
                it.copy(
                    draftPath = "",
                    resolvedPath = resolved,
                    validationError = null,
                )
            }
            repository.rescan()
        }
    }
}

enum class SettingsValidationError {
    INVALID_PATH,
}

data class SettingsUiState(
    val resolvedPath: String,
    val draftPath: String,
    val validationError: SettingsValidationError? = null,
)
