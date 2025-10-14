import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// Clase de datos para los ítems de la barra de navegación
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// --- 1. La Pantalla Principal (Contenedor) ---
@Composable
fun MainScreen() {
    // Scaffold es un layout predefinido de Material Design
    // que facilita la colocación de TopBar, BottomBar, etc.
    Scaffold(
        topBar = { TopSearchBar() },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        // Este es el contenedor principal del contenido.
        // El mapa iría aquí.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica el padding para no solaparse con las barras
        ) {
            MyMapComponent()
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MyMapComponent() {
    val context = LocalContext.current
    // Cliente para obtener la ubicación del proveedor de Google
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Ubicación por defecto (Zacatecas, por si no se obtiene la ubicación)
    val zacatecas = LatLng(22.7709, -102.5833)
    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para controlar la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 12f)
    }

    // Launcher para solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            // Si se conceden los permisos, obtenemos la última ubicación
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    deviceLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    // Este efecto se ejecuta una sola vez cuando el composable entra en la pantalla
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Cuando la ubicación del dispositivo cambia, actualizamos la cámara
    LaunchedEffect(deviceLocation) {
        deviceLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    // El componente de Google Maps
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Puedes añadir un marcador en la ubicación del usuario si lo deseas
        deviceLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Mi Ubicación"
            )
        }
    }
}

// --- 2. La Barra de Búsqueda Superior ---
@Composable
fun TopSearchBar() {
    // Usamos un TextField para simular la barra de búsqueda
    TextField(
        value = "", // El valor del texto (vacío para el placeholder)
        onValueChange = { /* Acción al cambiar el texto */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Buscar") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Icono de búsqueda") },
        shape = RoundedCornerShape(32.dp), // Esquinas redondeadas
        colors = TextFieldDefaults.colors(
            // Quita la línea indicadora de abajo para un look más limpio
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.surface,
            // Establece un color de fondo gris claro
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        singleLine = true
    )
}

// --- 3. La Barra de Navegación Inferior ---
@Composable
fun BottomNavigationBar() {
    // Estado para recordar qué ítem está seleccionado
    var selectedItemIndex by remember { mutableStateOf(0) }

    val items = listOf(
        BottomNavItem("Mapa", Icons.Default.Map, "map"),
        BottomNavItem("Alertar", Icons.Default.Notifications, "alerts"),
        BottomNavItem("Cuenta", Icons.Default.AccountCircle, "account")
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = { selectedItemIndex = index },
                label = { Text(item.label) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                }
            )
        }
    }
}


// --- Preview para ver el diseño en Android Studio ---
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // Aquí puedes envolver tu pantalla en un tema para la previsualización
    // Theme.YourAppTheme {
    MainScreen()
    // }
}