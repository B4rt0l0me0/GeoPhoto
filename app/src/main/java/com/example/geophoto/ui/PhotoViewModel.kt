// Łączenie UI z bazą danych
package com.example.geophoto.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.geophoto.data.PhotoDatabase
import com.example.geophoto.data.PhotoEntity
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) { // Tworzenie klasy

    private val photoDao = PhotoDatabase.Companion.getDatabase(application).photoDao() // Umożliwienie komunikacji z bazą danych
    val allPhotos: LiveData<List<PhotoEntity>> = photoDao.getAllPhotos() // Lista zdjęć

    fun insertPhoto(photo: PhotoEntity) { // Funkcja operująca na bazie
        viewModelScope.launch { // Uruchomienie funkcji w tle
            photoDao.insert(photo) // Wstawienie zdjęcia do bazy
        }
    }
}