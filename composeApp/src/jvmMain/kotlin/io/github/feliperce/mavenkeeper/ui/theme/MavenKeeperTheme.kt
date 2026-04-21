package io.github.feliperce.mavenkeeper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MavenKeeperTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
