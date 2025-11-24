package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    secondary = GreenSecondaryDark,
    tertiary = GreenAccentDark,

    background = GreenDark,
    surface = GreenSurfaceDark,
    surfaceVariant = GreenDark.copy(alpha = 0.7f),

    onPrimary = TextOnDarkPrimary,
    onSecondary = TextOnDark,
    onTertiary = TextOnDark,
    onBackground = TextOnDark,
    onSurface = TextOnDark,

    outline = TextOnDarkSoft
)

private val LightColors = lightColorScheme(
    primary = GreenPrimaryLight,
    secondary = GreenSecondaryLight,
    tertiary = GreenAccentLight,

    background = GreenBackgroundLight,
    surface = GreenSurfaceLight,
    surfaceVariant = GreenAccentLight.copy(alpha = 0.2f),

    onPrimary = TextOnDarkPrimary,
    onSecondary = TextOnLight,
    onTertiary = TextOnLightSoft,
    onBackground = TextOnLight,
    onSurface = TextOnLight,

    outline = TextOnLightMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else {
            if (darkTheme) DarkColors else LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
