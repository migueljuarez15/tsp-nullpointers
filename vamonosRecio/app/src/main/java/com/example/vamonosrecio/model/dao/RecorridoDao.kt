package com.example.vamonosrecio.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.vamonosrecio.model.LatLngData
import com.example.vamonosrecio.model.RecorridoModel

@Dao
interface RecorridoDao {
    @Query("SELECT * FROM RECORRIDO WHERE ID_RUTA = :rutaId ORDER BY ORDEN ASC")
    suspend fun getRecorridoByRuta(rutaId: Int): List<RecorridoModel>

    @Query("""
        SELECT P.LATITUD, P.LONGITUD
        FROM RECORRIDO R
        INNER JOIN PARADAS P ON R.ID_PARADA = P.ID_PARADA
        WHERE R.ID_RUTA = :rutaId
        ORDER BY R.ORDEN ASC
    """)
    suspend fun getCoordenadasPorRuta(rutaId: Int): List<LatLngData>
}