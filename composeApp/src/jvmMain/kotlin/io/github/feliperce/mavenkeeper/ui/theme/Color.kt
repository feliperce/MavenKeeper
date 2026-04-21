package io.github.feliperce.mavenkeeper.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF3B6EB5)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFD5E3FF)
private val OnPrimaryContainer = Color(0xFF00264C)

private val Secondary = Color(0xFF545F71)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFD8E3F8)
private val OnSecondaryContainer = Color(0xFF111C2B)

private val Tertiary = Color(0xFF6C5677)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFF4D9FF)
private val OnTertiaryContainer = Color(0xFF26132F)

private val Error = Color(0xFFBA1A1A)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF410002)

private val Background = Color(0xFFF9F9FF)
private val OnBackground = Color(0xFF1A1B21)
private val Surface = Color(0xFFF9F9FF)
private val OnSurface = Color(0xFF1A1B21)
private val SurfaceVariant = Color(0xFFE0E2EC)
private val OnSurfaceVariant = Color(0xFF44474E)
private val Outline = Color(0xFF74777F)
private val OutlineVariant = Color(0xFFC4C6D0)

val AppLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FF),
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF20487D),
    onPrimaryContainer = Color(0xFFD5E3FF),
    secondary = Color(0xFFBCC7DB),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3D4758),
    onSecondaryContainer = Color(0xFFD8E3F8),
    tertiary = Color(0xFFD8BDE3),
    onTertiary = Color(0xFF3C2946),
    tertiaryContainer = Color(0xFF543F5E),
    onTertiaryContainer = Color(0xFFF4D9FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
)
