// Tworzenie i przechowywanie danych w bazie
package com.example.geophoto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PhotoEntity::class], version = 1) // Tworzenie tabeli PhotoEntity w wersji 1
abstract class PhotoDatabase : RoomDatabase() { // Klasa bazy danych
    abstract fun photoDao(): PhotoDao // Korzystanie z zapytań w PhotoDao
    companion object {
        @Volatile
        private var INSTANCE: PhotoDatabase? = null
        fun getDatabase(context: Context): PhotoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder( // Tworzenie bazy danych
                    context.applicationContext,
                    PhotoDatabase::class.java,
                    "photo_database" // Nazwa pliku w jakim zapisywana jest pamięc aplikacji
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
