package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SportsOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6E230B),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = SportsCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    background = StadiumDarkBg,
    onBackground = StadiumDarkText,
    surface = StadiumDarkSurface,
    onSurface = StadiumDarkText,
    surfaceVariant = StadiumDarkCard,
    onSurfaceVariant = StadiumDarkTextSecondary,
    outline = StadiumDarkBorder,
    error = SportsLiveRed
)

private val LightColorScheme = lightColorScheme(
    primary = SportsOrangeDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF3B0900),
    secondary = SportsBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = Color(0xFF001D36),
    background = StadiumLightBg,
    onBackground = StadiumLightText,
    surface = StadiumLightSurface,
    onSurface = StadiumLightText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = StadiumLightTextSecondary,
    outline = StadiumLightBorder,
    error = SportsLiveRed
)

@Composable
fun SportActuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our vibrant sports palette by default for identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
