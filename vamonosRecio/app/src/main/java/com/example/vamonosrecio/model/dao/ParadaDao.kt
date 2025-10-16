package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.ParadaModel

@Dao
interface ParadaDao {
    @Query("SELECT * FROM PARADAS")
    suspend fun getAllParadas(): List<ParadaModel>

    @Query("SELECT * FROM PARADAS WHERE ID_PARADA IN (:ids)")
    suspend fun getParadasByIds(ids: List<Int>): List<ParadaModel>
}