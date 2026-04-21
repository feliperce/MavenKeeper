package io.github.feliperce.mavenkeeper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.feliperce.mavenkeeper.di.AppContainer
import io.github.feliperce.mavenkeeper.ui.screens.library.LibraryScreen
import io.github.feliperce.mavenkeeper.ui.screens.settings.SettingsScreen

@Composable
fun MavenKeeperNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Library,
        modifier = modifier,
    ) {
        composable<Destination.Library> {
            val vm = viewModel { container.libraryViewModel() }
            LibraryScreen(viewModel = vm)
        }
        composable<Destination.Settings> {
            val vm = viewModel { container.settingsViewModel() }
            SettingsScreen(viewModel = vm)
        }
    }
}
