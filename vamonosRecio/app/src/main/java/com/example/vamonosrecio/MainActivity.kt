package com.example.vamonosrecio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vamonosrecio.db.AppDatabase
import com.example.vamonosrecio.view.AppNavigation
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
        setContent {
            AppNavigation(db)
        }
    }
}