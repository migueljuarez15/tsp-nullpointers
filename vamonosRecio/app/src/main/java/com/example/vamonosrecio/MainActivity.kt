package com.example.vamonosrecio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vamonosrecio.db.AppDatabase
import com.example.vamonosrecio.view.AppNavigation
import com.google.android.libraries.places.api.Places
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "vamonosRecio.db"
        )
            .createFromAsset("databases/vamonosRecio.db")
            .fallbackToDestructiveMigration() // fuerza recreación si hay conflicto
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inicializa Google Places una sola vez
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDkcaTrFPn2PafDX85VmT-XEKS2qnk7oe8")
        }

        setContent {
            AppNavigation(db)
        }
    }
}