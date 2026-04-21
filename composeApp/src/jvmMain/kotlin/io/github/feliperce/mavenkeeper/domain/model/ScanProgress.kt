package io.github.feliperce.mavenkeeper.domain.model

sealed interface ScanProgress {
    data object Idle : ScanProgress
    data class Scanning(val discovered: Int) : ScanProgress
    data class Complete(val totalArtifacts: Int, val totalGroups: Int) : ScanProgress
    data class Error(val message: String) : ScanProgress
}
