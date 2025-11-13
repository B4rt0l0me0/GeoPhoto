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
import com.example.geophoto.R
import com.example.geophoto.data.PhotoEntity
import android.location.Geocoder
import java.util.Locale
import android.location.Location
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    private val photoViewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPhotoTheme {
                GeoPhotoApp(photoViewModel)
            }
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
    var exifText by remember { mutableStateOf("") }

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
                    val localPath = zapiszKopieZdjecia(context, uri)
                    val localUri = Uri.fromFile(File(localPath))
                    exifText = odczytajExif(localUri, context, fusedLocationClient, photoViewModel)
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
        topBar = { TopAppBar(title = { Text("GeoPhoto", color = Color.Black) }) }
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
            Text(
                text = exifText,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryLauncher.launch(intent)
                }) {
                    Text("Galeria", color = Color.White)
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
                    Text("Aparat", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, MapActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) { Text("Mapa wszystkich zdjęć", color = Color.White) }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, PhotoListActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) { Text("Zdjęcia (lista)", color = Color.White) }
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
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val exif = ExifInterface(inputStream)
            val latLong = FloatArray(2)
            val hasLatLong = exif.getLatLong(latLong)
            inputStream.close()

            // Pomocnicza funkcja do geokodowania współrzędnych na nazwę miasta (lub null)
            fun geocodeCity(lat: Double, lon: Double): String? {
                return try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val addr = addresses?.firstOrNull()
                    // próbujemy kilka pól, aby uzyskać sensowną nazwę
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                } catch (e: Exception) {
                    null
                }
            }

            if (hasLatLong) {
                val lat = latLong[0].toDouble()
                val lon = latLong[1].toDouble()
                val cityName = geocodeCity(lat, lon)

                viewModel.insertPhoto(
                    PhotoEntity(
                        filePath = uri.toString(),
                        latitude = lat,
                        longitude = lon,
                        cityName = cityName
                    )
                )
                return "GPS: $lat, $lon\nMiasto: ${cityName ?: "Nieznane "}"
            } else {
                // suspendujemy i czekamy na lastLocation
                val loc: Location? = try {
                    suspendCancellableCoroutine { cont ->
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location -> cont.resume(location) }
                            .addOnFailureListener { e -> cont.resumeWithException(e) }
                    }
                } catch (e: Exception) {
                    null
                }

                if (loc != null) {
                    val lat = loc.latitude
                    val lon = loc.longitude
                    val cityName = geocodeCity(lat, lon)

                    viewModel.insertPhoto(
                        PhotoEntity(
                            filePath = uri.toString(),
                            latitude = lat,
                            longitude = lon,
                            cityName = cityName
                        )
                    )
                    return "GPS: $lat, $lon\nMiasto: ${cityName ?: "Nieznane"}"
                } else {
                    return "Brak GPS — lokalizacja zostanie dodana"
                }
            }
        } else {
            return "Błąd otwarcia zdjęcia"
        }
    } catch (e: Exception) {
        return "Błąd EXIF: ${e.message}"
    }
}

fun zapiszKopieZdjecia(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
    val dir = context.getExternalFilesDir("images")
    if (dir != null && !dir.exists()) dir.mkdirs()
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    inputStream.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}
