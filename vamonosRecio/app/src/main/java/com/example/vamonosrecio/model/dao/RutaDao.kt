package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.RutaModel

@Dao
interface RutaDao {
    @Query("SELECT * FROM Rutas")
    suspend fun getAllRutas(): List<RutaModel>

    @Query("SELECT * FROM Rutas WHERE idRuta = :rutaId")
    suspend fun getRutaById(rutaId: Int): RutaModel

    @Query("SELECT polilinea FROM Rutas WHERE idRuta = :rutaId")
    fun getPolilineaById(rutaId: Int): String
}
