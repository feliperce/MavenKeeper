package io.github.feliperce.mavenkeeper.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Library : Destination

    @Serializable
    data class Detail(val groupId: String, val artifactId: String) : Destination

    @Serializable
    data object Settings : Destination
}
