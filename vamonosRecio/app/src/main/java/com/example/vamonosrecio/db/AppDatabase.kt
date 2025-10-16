package com.example.vamonosrecio.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vamonosrecio.model.ParadaModel
import com.example.vamonosrecio.model.RecorridoModel
import com.example.vamonosrecio.model.RutaModel
import com.example.vamonosrecio.model.dao.ParadaDao
import com.example.vamonosrecio.model.dao.RecorridoDao
import com.example.vamonosrecio.model.dao.RutaDao


@Database(entities = [RutaModel::class, ParadaModel::class, RecorridoModel::class], version = 6, exportSchema = false) // <-- VERSIÓN INCREMENTADA A 6
abstract class AppDatabase : RoomDatabase() {
    abstract fun rutaDao(): RutaDao
    abstract fun paradaDao(): ParadaDao
    abstract fun recorridoDao(): RecorridoDao
}