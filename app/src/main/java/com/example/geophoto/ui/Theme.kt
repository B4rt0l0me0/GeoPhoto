package com.example.geophoto.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeoPhotoColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun GeoPhotoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GeoPhotoColorScheme,
        typography = Typography(),
        content = content
    )
}
