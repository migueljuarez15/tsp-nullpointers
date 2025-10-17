package com.example.vamonosrecio.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.vamonosrecio.db.AppDatabase
import androidx.room.Room
import com.example.vamonosrecio.R

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "rutas_db"
        ).createFromAsset("database/rutas.db")
            .build()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        CoroutineScope(Dispatchers.IO).launch {
            val paradas = db.paradaDao().getAllParadas().take(10) // ejemplo: 10 primeras
            launch(Dispatchers.Main) {
                mostrarParadas(paradas)
            }
        }
    }

    private fun mostrarParadas(paradas: List<com.example.vamonosrecio.model.ParadaModel>) {
        val puntos = paradas.map { LatLng(it.latitud, it.longitud) }

        // Marcadores
        for (p in puntos) {
            map.addMarker(MarkerOptions().position(p))
        }

        // Polilínea (recorrido)
        map.addPolyline(PolylineOptions().addAll(puntos).width(8f))

        // Centrar mapa
        if (puntos.isNotEmpty()) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(puntos.first(), 13f))
        }
    }
}
