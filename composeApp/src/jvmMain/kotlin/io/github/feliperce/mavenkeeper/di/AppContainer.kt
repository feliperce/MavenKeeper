package io.github.feliperce.mavenkeeper.di

import io.github.feliperce.mavenkeeper.data.local.M2PathProvider
import io.github.feliperce.mavenkeeper.data.local.M2RepositoryScanner
import io.github.feliperce.mavenkeeper.data.parser.PomParser
import io.github.feliperce.mavenkeeper.data.repository.LocalArtifactRepositoryImpl
import io.github.feliperce.mavenkeeper.domain.repository.ArtifactRepository
import io.github.feliperce.mavenkeeper.ui.screens.library.LibraryViewModel
import io.github.feliperce.mavenkeeper.ui.screens.settings.SettingsViewModel

class AppContainer {
    val pathProvider: M2PathProvider = M2PathProvider()
    private val pomParser: PomParser = PomParser()
    private val scanner: M2RepositoryScanner = M2RepositoryScanner(pomParser)
    val artifactRepository: ArtifactRepository = LocalArtifactRepositoryImpl(pathProvider, scanner)

    fun libraryViewModel(): LibraryViewModel = LibraryViewModel(artifactRepository)

    fun settingsViewModel(): SettingsViewModel = SettingsViewModel(artifactRepository, pathProvider)
}
