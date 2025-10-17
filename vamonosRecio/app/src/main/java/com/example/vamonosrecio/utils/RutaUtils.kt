package com.example.vamonosrecio.utils

import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.compose.ui.graphics.Color

suspend fun fetchRouteWithOSRM(points: List<LatLng>): List<LatLng> {
    if (points.size < 2) return points

    val simplifiedPoints = if (points.size > 80) points.chunked(points.size / 80).map { it.first() } else points
    val coordinates = simplifiedPoints.joinToString(";") { "${it.longitude},${it.latitude}" }

    val url =
        "https://router.project-osrm.org/route/v1/driving/$coordinates?overview=full&geometries=geojson&steps=false&alternatives=false&continue_straight=true"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return try {
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return points

        val json = JSONObject(body)
        val routes = json.optJSONArray("routes")
        if (routes == null || routes.length() == 0) return points

        val geometry = routes.getJSONObject(0)
            .getJSONObject("geometry")
            .getJSONArray("coordinates")

        (0 until geometry.length()).map { i ->
            val coord = geometry.getJSONArray(i)
            LatLng(coord.getDouble(1), coord.getDouble(0))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        points
    }
}

//Paleta de colores para rutas
val rutaColorsMap = mapOf(
    1 to Color(0xFF88CDEE),
    2 to Color(0xFF064000),
    3 to Color(0xFFA3A3A3),
    4 to Color(0xFF0000FF),
    8 to Color(0xFFDB6600),
    14 to Color(0xFFD8A420),
    15 to Color(0xFF7F007F),
    16 to Color(0xFFFFFF00),
    17 to Color(0xFF1F7F22),
    22 to Color(0xFFFF0000),
)

// 🔢 Devuelve un color según el id de la ruta
fun getRutaColor(rutaId: Int): Color {
    return rutaColorsMap[rutaId] ?: Color(0xFF00796B) // color por defecto
}