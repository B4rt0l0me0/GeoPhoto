package com.example.geophoto.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import com.example.geophoto.ui.PhotoViewModel
import com.example.geophoto.R
import com.example.geophoto.data.PhotoEntity

class MainActivity : ComponentActivity() {
    private val photoViewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPhotoApp(photoViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoPhotoApp(photoViewModel: PhotoViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhoto by remember { mutableStateOf<Uri?>(null) }
    var exifText by remember { mutableStateOf("Brak danych EXIF") }

    // --- launchery ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            Toast.makeText(context, "Uprawnienia przyznane", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Brak wymaganych uprawnień", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentPhoto = uri
                coroutineScope.launch {
                    exifText = odczytajExif(uri, context, fusedLocationClient, photoViewModel)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            currentPhoto = tempCameraUri
            coroutineScope.launch {
                exifText = odczytajExif(tempCameraUri!!, context, fusedLocationClient, photoViewModel)
            }
        } else {
            Toast.makeText(context, "Nie zrobiono zdjęcia", Toast.LENGTH_SHORT).show()
        }
    }


    Scaffold(
        topBar = { TopAppBar(title = { Text("GeoPhoto") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = currentPhoto?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(exifText)
            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryLauncher.launch(intent)
                }) {
                    Text("Galeria")
                }

                Button(onClick = {
                    val hasPermissions = listOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ).all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }

                    if (!hasPermissions) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                        return@Button
                    }

                    val photoFile = utworzPlik(context)
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", photoFile
                    )
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                }) {
                    Text("Aparat")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, MapActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) { Text("Mapa wszystkich zdjęć") }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, PhotoListActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) { Text("Zdjęcia (lista)") }
        }
    }
}


// --- Pomocnicze funkcje ---

fun utworzPlik(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile("GeoPhoto_${timeStamp}_", ".jpg", storageDir)
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
suspend fun odczytajExif(
    uri: Uri,
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: PhotoViewModel
): String {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val exif = ExifInterface(inputStream)
            val latLong = FloatArray(2)
            val hasLatLong = exif.getLatLong(latLong)
            inputStream.close()

            if (hasLatLong) {
                val lat = latLong[0].toDouble()
                val lon = latLong[1].toDouble()
                viewModel.insertPhoto(
                    PhotoEntity(
                        filePath = uri.toString(),
                        latitude = lat,
                        longitude = lon
                    )
                )
                "GPS: $lat, $lon"
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.insertPhoto(
                            PhotoEntity(
                                filePath = uri.toString(),
                                latitude = loc.latitude,
                                longitude = loc.longitude
                            )
                        )
                    }
                }
                "Brak GPS — lokalizacja zostanie dodana"
            }
        } else "Błąd otwarcia zdjęcia"
    } catch (e: Exception) {
        "Błąd EXIF: ${e.message}"
    }
}
