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
            "vamonosRecioDb_v2" // <-- NOMBRE CAMBIADO PARA FORZAR LA RECREACIÓN
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Executors.newSingleThreadScheduledExecutor().execute {
                        val creacionTablas = """
                            CREATE TABLE RUTA (
                                ID_RUTA INTEGER PRIMARY KEY,
                                NOMBRE TEXT NOT NULL,
                                COLOR TEXT NOT NULL,
                                HORARIO TEXT,
                                TIEMPO_ESTIMADO_ESPERA TEXT
                            );

                            CREATE TABLE PARADAS (
                                ID_PARADA INTEGER PRIMARY KEY,
                                NOMBRE TEXT NOT NULL,
                                LATITUD REAL NOT NULL,
                                LONGITUD REAL NOT NULL
                            );

                            CREATE TABLE RECORRIDO (
                                ID_RUTA INTEGER NOT NULL,
                                ID_PARADA INTEGER NOT NULL,
                                PRIMARY KEY (ID_RUTA, ID_PARADA),
                                FOREIGN KEY (ID_RUTA) REFERENCES RUTA(ID_RUTA),
                                FOREIGN KEY (ID_PARADA) REFERENCES PARADAS(ID_PARADA)
                            );
                        """

                        val sentenciasInsert = """
                            INSERT INTO RUTA VALUES (1, 'Ruta 1','Azul Claro','Lunes-Viernes: 6:20-19:30 Sábados 6:30-19:00 Domingos: 8:00-17:00','8 minutos');
                            INSERT INTO RUTA VALUES (20, 'Ruta 1-Paseos del Valle','Azul Claro','Lunes-Viernes: 6:30-19:30 Sábados 6:30-19:30','20 minutos');
                            INSERT INTO RUTA VALUES (2, 'Ruta 2','Verde','Lunes-Viernes: 6:00-20:00 Sábados 7:00-19:00 Domingos: 8:00-17:00','15 minutos');
                            INSERT INTO RUTA VALUES (3, 'Ruta 3','Gris','Lunes-Viernes: 6:20-20:25','12 minutos');
                            INSERT INTO RUTA VALUES (4, 'Ruta 4','Azul Oscuro','Lunes-Viernes: 6:20-20:00 Sábados 7:00-18:00','18 minutos');
                            INSERT INTO RUTA VALUES (8, 'Ruta 8','Naranja','Lunes-Viernes: 6:00-20:00 Sábados 6:20-18:00 Domingos: 9:30-16:00','17 minutos');
                            INSERT INTO RUTA VALUES (14, 'Ruta 14','Cafe','Lunes-Viernes: 5:30-20:50 Sábados 5:50-20:50 Domingos: 6:20-20:50','14 minutos');
                            INSERT INTO RUTA VALUES (15, 'Ruta 15','Morado-Blanco','Lunes-Viernes: 5:25-21:15 Sábados 5:25-20:50 Domingos: 6:00-20:50','10 minutos');
                            INSERT INTO RUTA VALUES (16, 'Ruta 16','Morado-Amarillo','Lunes-Viernes: 5:45-10:05 Sábados 6:00-22:05 Domingos: 6:00-22:05','9 minutos');
                            INSERT INTO RUTA VALUES (17, 'Ruta 17','Verde-Amarillo','Lunes-Viernes: 5:30-20:40 Sábados 5:40-20:40 Domingos: 6:00-20:40','8 minutos');
                            INSERT INTO RUTA VALUES (21, 'Transportes de Guadalupe','Rojo-Blanco','Lunes-Viernes: 5:30-21:15 Sábados-Domingos 6:00-21:15','12 minutos');
                            INSERT INTO RUTA VALUES (22, 'Tierra y Libertad','Rojo-Blanco','Lunes-Viernes: 5:30-21:21 Sábados 6:00-21:15 Domingos: 6:00-21:15','11 minutos');
                            INSERT INTO PARADAS VALUES (1, '2 de Marzo', 22.774726, -102.588541);
                            INSERT INTO PARADAS VALUES (2, '2 de Mayo', 22.775583, -102.588846);
                            INSERT INTO PARADAS VALUES (3, '5 de Mayo/Segunda de Guerrero', 22.751439, -102.511585);
                            INSERT INTO PARADAS VALUES (4, '5 de Mayo 38', 22.754155, -102.519806);
                            INSERT INTO PARADAS VALUES (5, '5 de Mayo 80', 22.752971, -102.516082);
                            INSERT INTO RECORRIDO VALUES(4, 131);
                            INSERT INTO RECORRIDO VALUES(4, 128);
                            INSERT INTO RECORRIDO VALUES(4, 147);
                            INSERT INTO RECORRIDO VALUES(4, 310);
                            INSERT INTO RECORRIDO VALUES(4, 78);
                            INSERT INTO RECORRIDO VALUES(4, 179);
                        """.trimIndent()

                        creacionTablas.split(";").forEach { 
                            if (it.isNotBlank()) db.execSQL(it)
                        }
                        sentenciasInsert.lines().forEach { 
                            if (it.isNotBlank()) db.execSQL(it)
                        }
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation(db)
        }
    }
}