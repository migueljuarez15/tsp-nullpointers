package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.ParadaModel

@Dao
interface ParadaDao {
    @Query("SELECT * FROM Paradas")
    suspend fun getAllParadas(): List<ParadaModel>

    @Query("SELECT * FROM Paradas WHERE id_parada IN (:ids)")
    suspend fun getParadasByIds(ids: List<Int>): List<ParadaModel> // De momento no se usa en ninguna parte
}