package com.example.geophoto.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

class PhotoListActivity : ComponentActivity() {
    private val photoViewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPhotoTheme {
                PhotoListScreen(photoViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(photoViewModel: PhotoViewModel) {
    val photos by photoViewModel.allPhotos.observeAsState(emptyList())
    val context = LocalContext.current // <--- pobieramy kontekst tutaj!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista zdjęć") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(photos) { photo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            val intent = Intent(context, PhotoDetailActivity::class.java).apply {
                                putExtra("filePath", photo.filePath)
                                putExtra("latitude", photo.latitude)
                                putExtra("longitude", photo.longitude)
                                putExtra("cityName", photo.cityName)
                            }
                            context.startActivity(intent)
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(photo.filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Lat: ${photo.latitude}, Lon: ${photo.longitude}\nMiasto: ${photo.cityName ?: "Nieznane"}", color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
