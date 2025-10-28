package com.example.geophoto.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.geophoto.data.PhotoDatabase
import com.example.geophoto.data.PhotoEntity
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val photoDao = PhotoDatabase.Companion.getDatabase(application).photoDao()
    val allPhotos: LiveData<List<PhotoEntity>> = photoDao.getAllPhotos()

    fun insertPhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            photoDao.insert(photo)
        }
    }
}