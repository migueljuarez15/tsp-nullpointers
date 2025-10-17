package com.example.vamonosrecio.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "RECORRIDO",
    primaryKeys = ["ID_RUTA", "ID_PARADA"],
    foreignKeys = [
        ForeignKey(
            entity = RutaModel::class,
            parentColumns = ["ID_RUTA"],
            childColumns = ["ID_RUTA"]
        ),
        ForeignKey(
            entity = ParadaModel::class,
            parentColumns = ["ID_PARADA"],
            childColumns = ["ID_PARADA"]
        )
    ],
    indices = [Index(value = ["ID_RUTA"]), Index(value = ["ID_PARADA"])]
)
data class RecorridoModel(
    @ColumnInfo(name = "ID_RUTA") val rutaId: Int,
    @ColumnInfo(name = "ID_PARADA") val paradaId: Int,
    @ColumnInfo(name = "ORDEN") val orden: Int // 👈 nuevo campo
)

data class LatLngData(
    @ColumnInfo(name = "LATITUD") val latitud: Double,
    @ColumnInfo(name = "LONGITUD") val longitud: Double,
)