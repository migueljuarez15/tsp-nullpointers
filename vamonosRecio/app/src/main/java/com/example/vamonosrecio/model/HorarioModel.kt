package com.example.vamonosrecio.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "horariosTiempoEspera")
data class HorarioModel(
    @PrimaryKey
    @ColumnInfo(name = "idRuta") val idRuta: Int,
    @ColumnInfo(name = "horario") val horario: String?,
    @ColumnInfo(name = "tiempoEspera") val tiempoEspera: Int?
)
