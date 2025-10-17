package com.example.vamonosrecio.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Paradas")
data class ParadaModel(
    @PrimaryKey @ColumnInfo(name = "id_parada") val id: Int = 0,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "latitud") val latitud: Double,
    @ColumnInfo(name = "longitud") val longitud: Double
)