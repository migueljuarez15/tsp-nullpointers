package com.example.vamonosrecio.view

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vamonosrecio.db.AppDatabase
import com.example.vamonosrecio.model.ParadaModel
import com.example.vamonosrecio.model.LatLngData
import com.example.vamonosrecio.utils.fetchRouteWithOSRM
import com.example.vamonosrecio.utils.getRutaColor
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.withContext

// --- DATA CLASS ---
data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

// --- MAIN SCREEN ---
@Composable
fun MainScreen(db: AppDatabase, onMenuClick: () -> Unit, onSearchClick: () -> Unit, navController: NavController, routeId: Int? = null) {
    Scaffold(
        topBar = { CustomTopBar(onMenuClick = onMenuClick, navController = navController) },
        bottomBar = { CustomBottomNavBar() },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MyMapComponent(db, routeId, navController)
        }
    }
}

// --- GOOGLE MAP COMPONENT ---
@SuppressLint("MissingPermission")
@Composable
fun MyMapComponent(db: AppDatabase, routeId: Int?, navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val zacatecas = LatLng(22.7709, -102.5833)
    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

    val paradas by produceState(initialValue = emptyList<ParadaModel>()) {
        value = db.paradaDao().getAllParadas()
        println("🔵 Se cargaron ${value.size} paradas")
    }

    // AQUI SE PONE LAS COORDENADAS PARA HCER EL RECORRIDO
    val coordenadasRuta by produceState(initialValue = emptyList<LatLngData>(), routeId) {
        value = if (routeId != null) db.recorridoDao().getCoordenadasPorRuta(routeId)
        else emptyList()
        println("🟢 Ruta $routeId tiene ${value.size} puntos de recorrido")
    }

    // ✅ Evita crash al obtener la ruta desde OSRM
    val polylinePoints by produceState(initialValue = emptyList<LatLng>(), coordenadasRuta) {
        if (coordenadasRuta.isNotEmpty()) {
            val puntos = coordenadasRuta.map { LatLng(it.latitud, it.longitud) }
            try {
                // Ejecuta en hilo de IO (seguro)
                value = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchRouteWithOSRM(puntos)
                }
                println("🟣 Polyline generada con ${value.size} puntos decodificados")
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Error al generar polyline: ${e.message}")
                value = emptyList()
            }
        } else value = emptyList()
    }

    val rutaSeleccionada by produceState(initialValue = null as com.example.vamonosrecio.model.RutaModel?, routeId) {
        value = routeId?.let { db.rutaDao().getRutaById(it) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 13f)
    }

    // 🔵 Estado para guardar el marcador del lugar buscado
    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }

    // 🔄 Escucha la ubicación seleccionada desde la pantalla de búsqueda
    val selectedPlaceFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<LatLng?>("selectedPlace", null)

    val selectedPlace by selectedPlaceFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let {
            selectedMarker = it
            println("📍 Centrar cámara en: ${it.latitude}, ${it.longitude}")
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    deviceLocation = location?.let { LatLng(it.latitude, it.longitude) } ?: zacatecas
                }
        } else deviceLocation = zacatecas
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(deviceLocation) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(deviceLocation ?: zacatecas, 13f))
    }

    val zacatecasBounds = LatLngBounds(
        LatLng(22.5500, -102.9500),
        LatLng(23.0000, -102.3000)
    )

    var mostrarPopup by remember { mutableStateOf(routeId != null) }
    val mostrarPolyline = mostrarPopup && polylinePoints.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = deviceLocation != null,
                latLngBoundsForCameraTarget = zacatecasBounds,
                minZoomPreference = 11f,
                maxZoomPreference = 18f
            )
        ) {
            // --- ZOOM ACTUAL ---
            val zoom = cameraPositionState.position.zoom

            // --- PARADAS CACHÉ ---
            val paradasMemo = remember(paradas) { paradas }

            // --- PARADA SELECCIONADA ---
            var paradaSeleccionada by remember { mutableStateOf<ParadaModel?>(null) }

            // --- CARGAR Y ESCALAR ÍCONO SOLO UNA VEZ ---
            val context = LocalContext.current
            val paradaBitmap = remember {
                try {
                    context.assets.open("icons/paradaCamion.png").use {
                        BitmapFactory.decodeStream(it)
                    }?.let { Bitmap.createScaledBitmap(it, 80, 80, false) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (zoom < 16f) {
                // 👇 Versión ligera con Círculos (más fluida)
                paradasMemo.forEachIndexed { index, parada ->
                    if (index % 3 == 0) { // dibuja 1 de cada 3 para optimizar
                        Circle(
                            center = LatLng(parada.latitud, parada.longitud),
                            radius = 12.0,
                            strokeColor = Color(0xFF1565C0),
                            fillColor = Color(0x441565C0)
                        )
                    }
                }
            } else if (paradaBitmap != null) {
                // 👇 Versión detallada con íconos clicables
                paradasMemo.forEach { parada ->
                    Marker(
                        state = MarkerState(position = LatLng(parada.latitud, parada.longitud)),
                        title = parada.nombre,
                        icon = BitmapDescriptorFactory.fromBitmap(paradaBitmap),
                        onClick = {
                            paradaSeleccionada = parada
                            true
                        }
                    )
                }
            }

            // ✅ Ahora la polyline usa OSRM y no crashea
            if (mostrarPolyline) {
                val rutaColor = getRutaColor(routeId ?: 0)
                val selectedColor = rutaColor.copy(alpha = 1f)
                val normalColor = rutaColor.copy(alpha = 0.6f)

                Polyline(
                    points = polylinePoints,
                    color = if (mostrarPopup) selectedColor else normalColor,
                    width = if (mostrarPopup) 10f else 6f
                )
            }

            // ✅ Marker del lugar buscado
            selectedMarker?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    title = "Destino seleccionado",
                    snippet = "Ubicación buscada"
                )
            }
        }

        if (mostrarPopup && rutaSeleccionada != null) {
            RutaInfoPopup(
                ruta = rutaSeleccionada!!,
                onClose = { mostrarPopup = false }
            )
        }
    }
}

// --- CUSTOM TOP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(onMenuClick: () -> Unit, navController: NavController) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                val context = LocalContext.current
                val bitmap = remember("icons/rutasCamion.png") {
                    try {
                        context.assets.open("icons/rutasCamion.png").use { BitmapFactory.decodeStream(it) }
                    } catch (e: java.io.IOException) {
                        e.printStackTrace()
                        null
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Menú"
                    )
                }
            }

            var searchText by remember { mutableStateOf("") }

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Buscar destino") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
                    .clickable { navController.navigate(Screen.Busqueda.route) },
                enabled = false, // desactiva edición directa
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFE2E8F0),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

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
                .navigationBarsPadding()
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

// --- POPUP INFO DE RUTA ---
@Composable
fun RutaInfoPopup(ruta: com.example.vamonosrecio.model.RutaModel, onClose: () -> Unit) {
    var expandido by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter // 👈 lo fija abajo
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "El trayecto de la ${ruta.nombre} se muestra en el mapa.",
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { expandido = !expandido }) {
                            Icon(
                                imageVector = if (expandido) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }

                if (expandido) {
                    Spacer(Modifier.height(8.dp))
                    Text("Horario: ${ruta.horario?.ifEmpty { "6:00am - 9:30pm" }}")
                    Text("Tiempo de espera aproximado: ${ruta.tiempoEstimadoEspera?.ifEmpty { "15 min" }}")
                }
            }
        }
    }
}
