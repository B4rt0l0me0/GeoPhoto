package com.example.geophoto.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MapActivity : ComponentActivity() {
    private val photoViewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPhotoTheme {
                MapScreen(photoViewModel)
            }
        }
    }
}

@Composable
fun MapScreen(photoViewModel: PhotoViewModel) {
    val photos by photoViewModel.allPhotos.observeAsState(emptyList())
    val cameraPositionState = rememberCameraPositionState()

    GoogleMap(
        modifier = Modifier,
        cameraPositionState = cameraPositionState
    ) {
        photos.forEach { photo ->
            Marker(
                state = MarkerState(LatLng(photo.latitude, photo.longitude)),
                title = "Zdjęcie",
                snippet = "Lat: ${photo.latitude}, Lon: ${photo.longitude}"
            )
        }
    }

    LaunchedEffect(photos) {
        photos.firstOrNull()?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude),
                14f
            )
        }
    }
}
