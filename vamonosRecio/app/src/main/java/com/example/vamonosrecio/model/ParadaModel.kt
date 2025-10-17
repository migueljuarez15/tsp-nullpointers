package com.example.vamonosrecio.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PARADAS")
data class ParadaModel(
    @PrimaryKey @ColumnInfo(name = "ID_PARADA") val id: Int = 0,
    @ColumnInfo(name = "NOMBRE") val nombre: String,
    @ColumnInfo(name = "LATITUD") val latitud: Double,
    @ColumnInfo(name = "LONGITUD") val longitud: Double
)