package com.example.agritwin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Neutral50,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = WaterBlue,
    onSecondary = Neutral50,
    secondaryContainer = SkyBlue,
    onSecondaryContainer = Neutral900,
    tertiary = SoilBrown,
    onTertiary = Neutral50,
    tertiaryContainer = Color(0xFFDCCCC0),
    onTertiaryContainer = Neutral900,
    error = ErrorRed,
    onError = Neutral50,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = ErrorRed,
    background = Neutral50,
    onBackground = Neutral900,
    surface = Neutral100,
    onSurface = Neutral900,
    surfaceVariant = Neutral200,
    onSurfaceVariant = Neutral600,
    outline = Neutral400,
    outlineVariant = Neutral300,
    scrim = Neutral900,
    inverseSurface = Neutral800,
    inverseOnSurface = Neutral50,
    inversePrimary = Green300
)

@Composable
fun AgriTwinTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AgriTwinTypography,
        content = content
    )
}