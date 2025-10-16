package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.RecorridoModel

@Dao
interface RecorridoDao {
    @Query("SELECT * FROM RECORRIDO WHERE ID_RUTA = :rutaId")
    suspend fun getRecorridoByRuta(rutaId: Int): List<RecorridoModel>
}