package com.ionclaw.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// the app keeps a single light appearance, matching the flutter runner
private val ColorScheme = lightColorScheme(
    primary = BrandPrimary,
    background = ScreenBackground,
    surface = CardSurface
)

@Composable
fun IonClawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
