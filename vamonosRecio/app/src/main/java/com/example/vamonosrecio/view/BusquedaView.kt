package com.example.vamonosrecio.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaView(navController: NavController) {
    val context = LocalContext.current

    /*// Inicializar Places (por si no se ha hecho)
    remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, "TU_API_KEY_AQUI")
        }
    }*/

    val placesClient = remember { Places.createClient(context) }

    var searchText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<Pair<String, String>>()) } // name, placeId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar destino") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    if (it.length > 2) {
                        buscarLugaresConPlaces(placesClient, it) { resultados ->
                            results = resultados
                        }
                    } else {
                        results = emptyList()
                    }
                },
                placeholder = { Text("Buscar ubicación...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            results.forEach { (name, placeId) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            obtenerCoordenadas(placesClient, placeId) { latLng ->
                                if (latLng != null) {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("selectedPlace", latLng)
                                    navController.popBackStack()
                                }
                            }
                        }
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 🔍 Busca lugares con autocompletado de Google Places
 */
fun buscarLugaresConPlaces(
    placesClient: com.google.android.libraries.places.api.net.PlacesClient,
    query: String,
    onResult: (List<Pair<String, String>>) -> Unit
) {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            val results = response.autocompletePredictions.map {
                it.getFullText(null).toString() to it.placeId
            }
            onResult(results)
        }
        .addOnFailureListener {
            it.printStackTrace()
            onResult(emptyList())
        }
}

/**
 * 📍 Obtiene coordenadas (LatLng) desde un placeId
 */
fun obtenerCoordenadas(
    placesClient: com.google.android.libraries.places.api.net.PlacesClient,
    placeId: String,
    onLatLngFound: (LatLng?) -> Unit
) {
    val placeFields = listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
    val request = FetchPlaceRequest.newInstance(placeId, placeFields)

    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            onLatLngFound(response.place.latLng)
        }
        .addOnFailureListener {
            it.printStackTrace()
            onLatLngFound(null)
        }
}
