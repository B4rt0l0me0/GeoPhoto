package com.example.geophoto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val latitude: Double,
    val longitude: Double,
    val cityName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)