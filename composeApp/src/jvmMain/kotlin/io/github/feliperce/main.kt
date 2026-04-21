package io.github.feliperce

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MavenKeeper",
    ) {
        App()
    }
}