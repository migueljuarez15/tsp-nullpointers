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

// --- DATA CLASS ---
data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

// --- MAIN SCREEN ---
@Composable
fun MainScreen(db: AppDatabase, onMenuClick: () -> Unit) {
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
            MyMapComponent(db)
        }
    }
}

// --- GOOGLE MAP COMPONENT ---
@SuppressLint("MissingPermission")
@Composable
fun MyMapComponent(db: AppDatabase) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val zacatecas = LatLng(22.7709, -102.5833)
    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

    // Cargar las paradas desde la base de datos
    val paradas by produceState<List<ParadaModel>>(initialValue = emptyList()) {
        value = db.paradaDao().getAllParadas()
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 12f)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val newLatLng = LatLng(location.latitude, location.longitude)

                        // ✅ Solo actualiza si está dentro de México (rango aproximado)
                        val isInMexico = location.latitude in 14.0..33.0 &&
                                location.longitude in -118.0..-86.0

                        deviceLocation = if (isInMexico) newLatLng else zacatecas
                    } else {
                        deviceLocation = zacatecas
                    }
                }
                .addOnFailureListener {
                    deviceLocation = zacatecas
                }
        } else {
            deviceLocation = zacatecas
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Mueve la cámara solo cuando ya hay una ubicación
    LaunchedEffect(deviceLocation) {
        val target = deviceLocation ?: zacatecas
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(target, 14f)
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Marcador para la ubicación del usuario
        Marker(
            state = MarkerState(position = deviceLocation ?: zacatecas),
            title = if (deviceLocation != null) "Mi Ubicación" else "Zacatecas"
        )

        // Marcadores para cada parada de autobús
        paradas.forEach { parada ->
            Marker(
                state = MarkerState(position = LatLng(parada.latitud, parada.longitud)),
                title = parada.nombre,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
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