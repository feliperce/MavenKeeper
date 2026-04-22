package io.github.feliperce.mavenkeeper

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.feliperce.mavenkeeper.di.AppContainer
import io.github.feliperce.mavenkeeper.ui.navigation.Destination
import io.github.feliperce.mavenkeeper.ui.navigation.MavenKeeperNavHost
import mavenkeeper.composeapp.generated.resources.Res
import mavenkeeper.composeapp.generated.resources.nav_library
import mavenkeeper.composeapp.generated.resources.nav_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun MavenKeeperApp(container: AppContainer) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        container.artifactRepository.rescan()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(navController = navController)
            MavenKeeperNavHost(
                navController = navController,
                container = container,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AppNavigationRail(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationRail {
        RailItem(
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
            label = stringResource(Res.string.nav_library),
            selected = currentDestination?.hasRoute<Destination.Library>() == true,
            onClick = {
                navController.navigate(Destination.Library) {
                    popUpTo(Destination.Library) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        RailItem(
            icon = Icons.Filled.Settings,
            label = stringResource(Res.string.nav_settings),
            selected = currentDestination?.hasRoute<Destination.Settings>() == true,
            onClick = {
                navController.navigate(Destination.Settings) {
                    launchSingleTop = true
                }
            },
        )
    }
}

@Composable
private fun RailItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = { Text(label) },
    )
}
