package com.example.geophoto.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeoPhotoColorScheme = lightColorScheme(
    primary = Color.Black, // Kolor dla przycisków
    onPrimary = Color.White, // Kolor czcionki
    background = Color.White, // Kolor tła
)

@Composable
fun GeoPhotoTheme(content: @Composable () -> Unit) { // Funkcja ustawiająca różne ustawienia dla aplikacji
    MaterialTheme(
        colorScheme = GeoPhotoColorScheme, // Ustawienie kolorów dla całej aplikacji
        content = content // zmienna przekazująca wygląd wewnątrz motywu
    )
}