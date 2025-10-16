package com.example.vamonosrecio.view

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vamonosrecio.db.AppDatabase
import com.example.vamonosrecio.model.ParadaModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds

// --- DATA CLASS ---
data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

// --- MAIN SCREEN ---
@Composable
fun MainScreen(db: AppDatabase, onMenuClick: () -> Unit, routeId: Int? = null) {
    Scaffold(
        topBar = { CustomTopBar(onMenuClick = onMenuClick) },
        bottomBar = { CustomBottomNavBar() },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MyMapComponent(db, routeId)
        }
    }
}

// --- GOOGLE MAP COMPONENT ---
@SuppressLint("MissingPermission")
@Composable
fun MyMapComponent(db: AppDatabase, routeId: Int?) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 📍 Coordenadas por defecto (Zacatecas - Guadalupe)
    val zacatecas = LatLng(22.7709, -102.5833)
    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

    // Cargar paradas desde la BD
    val paradas by produceState(initialValue = emptyList<ParadaModel>()) {
        value = db.paradaDao().getAllParadas()
        println("🔹 Se cargaron ${value.size} paradas desde la base de datos")
    }

    // Estado de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 13f)
    }

    // 🚨 Permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    deviceLocation = if (location != null) {
                        LatLng(location.latitude, location.longitude)
                    } else zacatecas
                }
                .addOnFailureListener {
                    deviceLocation = zacatecas
                }
        } else {
            deviceLocation = zacatecas
        }
    }

    // Lanzar permisos una vez
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Centrar cámara inicial
    LaunchedEffect(deviceLocation) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(deviceLocation ?: zacatecas, 13f)
        )
    }

    // 📐 Limitar área visible del mapa (Zacatecas - Guadalupe)
    val zacatecasBounds = LatLngBounds(
        LatLng(22.6000, -102.8000), // suroeste — un poco más abajo y a la izquierda
        LatLng(22.9000, -102.4000)  // noreste — un poco más arriba y a la derecha
    )

    // --- Mapa ---
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = deviceLocation != null,
            latLngBoundsForCameraTarget = zacatecasBounds,
            minZoomPreference = 12f,
            maxZoomPreference = 18f
        )
    ) {
        // 📍 Ubicación del usuario
        deviceLocation?.let {
            Marker(
                state = MarkerState(it),
                title = "Mi ubicación"
            )
        }

        // 🔵 Dibujar círculos de paradas según el nivel de zoom
        val zoom = cameraPositionState.position.zoom
        val step = when {
            zoom > 17 -> 1
            zoom > 15 -> 3
            zoom > 13 -> 6
            else -> 0 // No se muestran puntos si el zoom es bajo
        }

        if (step > 0) {
            paradas.filterIndexed { index, _ -> index % step == 0 }.forEach { parada ->
                Circle(
                    center = LatLng(parada.latitud, parada.longitud),
                    radius = 10.0,
                    strokeColor = Color(0xFF1565C0),
                    fillColor = Color(0x441565C0)
                )
            }
        }
    }
}

// --- CUSTOM TOP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(onMenuClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // 👈 añade espacio superior
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) { // Se usa el parámetro onMenuClick
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }

            var searchText by remember { mutableStateOf("") }

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Buscar") },
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFE2E8F0),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            IconButton(onClick = { /* acción secundaria */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }
        }
    }
}

// --- FLOATING BUTTON (CENTRAL) ---
/*@Composable
fun CenterFloatingButton() {
    Surface(
        modifier = Modifier
            .size(56.dp),
        shape = CircleShape,
        color = Color(0xFFE2E8F0).copy(alpha = 0.9f),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Expandir",
                tint = Color(0xFF475569)
            )
        }
    }
}*/

// --- CUSTOM BOTTOM NAV BAR ---
@Composable
fun CustomBottomNavBar() {
    var selectedIndex by remember { mutableStateOf(0) }

    val items = listOf(
        BottomNavItem("Mapa", Icons.Default.Map),
        BottomNavItem("Alertar", Icons.Default.Notifications),
        BottomNavItem("Cuenta", Icons.Default.Person)
    )

    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // 👈 añade espacio inferior
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedIndex = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selectedIndex == index) Color(0xFF137fec)
                        else Color(0xFF475569)
                    )
                    Text(
                        text = item.label,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedIndex == index) Color(0xFF137fec)
                        else Color(0xFF475569),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}