package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.RutaModel

@Dao
interface RutaDao {
    @Query("SELECT * FROM RUTA")
    suspend fun getAllRutas(): List<RutaModel>

    @Query("SELECT * FROM RUTA WHERE ID_RUTA = :rutaId")
    suspend fun getRutaById(rutaId: Int): RutaModel
}