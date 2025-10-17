package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.HorarioModel

@Dao
interface HorarioDao {
    @Query("SELECT * FROM horariosTiempoEspera WHERE idRuta = :rutaId")
    suspend fun getHorarioByRutaId(rutaId: Int): HorarioModel?
}
