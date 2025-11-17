// Operacje na bazie danych
package com.example.geophoto.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao // Interfejs dla Room, gdzie będą operacje
interface PhotoDao {
    @Insert()
    suspend fun insert(photo: PhotoEntity) // Wprowadzenie do bazy danych zdjęcia w tle

    @Query("SELECT * FROM photos ORDER BY timestamp DESC") // Zapytanie do bazy danych mające wyświetlić wszystkie zdjęcia posortowane od najnowszego do najstarszego
    fun getAllPhotos(): LiveData<List<PhotoEntity>>
}