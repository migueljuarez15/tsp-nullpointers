package com.example.vamonosrecio.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vamonosrecio.model.HorarioModel
import com.example.vamonosrecio.model.ParadaModel
import com.example.vamonosrecio.model.RutaModel
import com.example.vamonosrecio.model.dao.HorarioDao
import com.example.vamonosrecio.model.dao.ParadaDao
import com.example.vamonosrecio.model.dao.RutaDao


@Database(entities = [RutaModel::class, ParadaModel::class, HorarioModel::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rutaDao(): RutaDao
    abstract fun paradaDao(): ParadaDao
    abstract fun horarioDao() : HorarioDao
}