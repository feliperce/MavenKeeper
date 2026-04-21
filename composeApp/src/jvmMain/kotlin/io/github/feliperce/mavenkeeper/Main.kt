package io.github.feliperce.mavenkeeper

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.feliperce.mavenkeeper.di.AppContainer
import io.github.feliperce.mavenkeeper.ui.theme.MavenKeeperTheme

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1200.dp, 800.dp))
    val container = remember { AppContainer() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "MavenKeeper",
        state = windowState,
    ) {
        MavenKeeperTheme {
            MavenKeeperApp(container)
        }
    }
}
