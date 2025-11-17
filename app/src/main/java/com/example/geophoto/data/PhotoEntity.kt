// Dane opisujące zdjęcie
package com.example.geophoto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos") // Nazwa tabeli
data class PhotoEntity( // Klasa reprezentujaca zdjecie
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Klucz głowny dla zdjęcia
    val filePath: String, // Ścieżka do zdjęcia
    val latitude: Double, // Szerokość geograficzna
    val longitude: Double, // Długość geograficzna
    val cityName: String? = null, // Nazwa miasta
    val timestamp: Long = System.currentTimeMillis() // Czas zapisu zdjęcia
)