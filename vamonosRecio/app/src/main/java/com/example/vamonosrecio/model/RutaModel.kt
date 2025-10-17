package com.example.vamonosrecio.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Rutas")
data class RutaModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idRuta") val id: Int = 0,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "polilinea") val polilinea: String?,
    @ColumnInfo(name = "color") val color: String?
)