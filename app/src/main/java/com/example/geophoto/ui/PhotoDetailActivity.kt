// Zdjęcie po kliknięciu
package com.example.geophoto.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.Color
class PhotoDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filePath = intent.getStringExtra("filePath") // Pobranie danych z listy
        val latitude = intent.getDoubleExtra("latitude", 0.0) // Pobranie danych z listy
        val longitude = intent.getDoubleExtra("longitude", 0.0) // Pobranie danych z listy
        val cityName = intent.getStringExtra("cityName") ?: "Nieznane" // Pobranie danych z listy

        setContent {
            Scaffold( // Napis u góry
                topBar = {
                    TopAppBar(
                        title = { Text("Podgląd zdjęcia") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black
                        )
                    )
                }
            ) { padding ->
                Column( // Układ pionowy dla zdjęcia
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (filePath != null) { // Wyswietlanie zdjęcia
                        Image(
                            painter = rememberAsyncImagePainter(filePath), // Pobranie zdjęcia ze ścieżki
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lat: $latitude, Lon: $longitude", color = Color.Black) // Informacje o współrzędnych
                    Text("Miasto: $cityName", color = Color.Black) // Informacje o zdjęciu
                }
            }
        }
    }
}
