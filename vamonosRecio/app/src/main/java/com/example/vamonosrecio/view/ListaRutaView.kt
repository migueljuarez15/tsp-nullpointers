package com.example.vamonosrecio.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vamonosrecio.db.AppDatabase
import com.example.vamonosrecio.model.RutaModel

// Estructura para almacenar el estilo de cada ruta
data class RouteStyle(val background: Brush, val textColor: Color, val borderColor: Color? = null)

// Función para obtener el estilo basado en el nombre de la ruta
fun getRouteStyle(ruta: RutaModel): RouteStyle {
    return when {
        ruta.nombre.equals("Ruta 1") -> RouteStyle(SolidColor(Color(0xFF88CDEE)), Color.White)
        ruta.nombre.equals("Ruta 2") -> RouteStyle(SolidColor(Color(0xFF006400)), Color.White)
        ruta.nombre.equals("Ruta 3") -> RouteStyle(SolidColor(Color(0xFFD3D3D3)), Color.Black)
        ruta.nombre.equals("Ruta 4") -> RouteStyle(SolidColor(Color(0xFF0000FF)), Color.White)
        ruta.nombre.equals("Ruta 8") -> RouteStyle(SolidColor(Color(0xFFDB6600)), Color.White)
        ruta.nombre.equals("Ruta 14") -> RouteStyle(SolidColor(Color(0xFFD8A420)), Color.White)
        ruta.nombre.equals("Ruta 15") -> RouteStyle(SolidColor(Color(0xFF7F007F)), Color.White)
        ruta.nombre.equals("Ruta 16") -> RouteStyle(Brush.horizontalGradient(colors = listOf(Color(0xFFFFFF00), Color(0xFF800080))), Color.White)
        ruta.nombre.equals("Ruta 17") -> RouteStyle(Brush.horizontalGradient(colors = listOf(Color(0xFFFFFF00), Color(0xFF1F7F22))), Color.White)
        ruta.nombre.equals("Transportes de Guadalupe") -> RouteStyle(SolidColor(Color(0xFFFF0000)), Color.White)
        else -> RouteStyle(SolidColor(Color.LightGray), Color.Black) // Estilo por defecto
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRutaView(db: AppDatabase, onBack: () -> Unit, onRouteClick: (RutaModel) -> Unit) {

    // --- PRUEBA: Usando una lista harcoded --- 
    val rutas = listOf(
        RutaModel(1, "Ruta 1", "", "", ""),
        RutaModel(2, "Ruta 2", "", "", ""),
        RutaModel(3, "Ruta 3", "", "", ""),
        RutaModel(4, "Ruta 4", "", "", ""),
        RutaModel(8, "Ruta 8", "", "", ""),
        RutaModel(14, "Ruta 14", "", "", ""),
        RutaModel(15, "Ruta 15", "", "", ""),
        RutaModel(16, "Ruta 16", "", "", ""),
        RutaModel(17, "Ruta 17", "", "", ""),
        RutaModel(21, "Transportes de Guadalupe", "", "", "")
    )

    /*//--- CÓDIGO REAL (COMENTADO TEMPORALMENTE) ---
    // Cargar las rutas desde la base de datos
    val rutas by produceState<List<RutaModel>>(initialValue = emptyList(), producer = {
        value = db.rutaDao().getAllRutas()
    })*/

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Rutas", 
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // Espacio para centrar el título
                }
            )
        }
    ) { paddingValues ->
        if (rutas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando rutas... Si esto persiste, desinstala la app y vuelve a instalarla.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(rutas) { ruta ->
                    RouteButton(ruta = ruta, onClick = { onRouteClick(ruta) })
                }
            }
        }
    }
}

@Composable
fun RouteButton(ruta: RutaModel, onClick: () -> Unit) {
    val style = getRouteStyle(ruta)
    val shape = RoundedCornerShape(12.dp)

    Button(
        onClick = onClick,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(elevation = 4.dp, shape = shape)
            .then(
                if (style.borderColor != null) {
                    Modifier.border(width = 2.dp, color = style.borderColor, shape = shape)
                } else {
                    Modifier
                }
            ),
        contentPadding = PaddingValues(), // Se elimina el padding para que el Box ocupe todo el espacio
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent) // El color se aplica en el Box
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(style.background, shape)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ruta.nombre,
                color = style.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}