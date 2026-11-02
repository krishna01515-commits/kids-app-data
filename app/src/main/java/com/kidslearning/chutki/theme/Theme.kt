package com.kidslearning.chutki.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CandyOrange,
    onPrimary = Color.White,
    primaryContainer = CandyYellowLight,
    secondary = BubblegumPink,
    onSecondary = Color.White,
    tertiary = SkyBlue,
    background = WarmCream,
    surface = CardSurfaceLight,
    onBackground = Color(0xFF2C3E50),
    onSurface = Color(0xFF2C3E50)
)

private val DarkColorScheme = darkColorScheme(
    primary = CandyYellow,
    onPrimary = Color(0xFF13142B),
    primaryContainer = StarryNightSurface,
    secondary = BubblegumPinkLight,
    onSecondary = Color(0xFF13142B),
    tertiary = SkyBlueLight,
    background = StarryNightBg,
    surface = StarryNightCard,
    onBackground = StarryNightText,
    onSurface = StarryNightText
)

@Composable
fun ChutkiKidsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KidsTypography,
        content = content
    )
}

