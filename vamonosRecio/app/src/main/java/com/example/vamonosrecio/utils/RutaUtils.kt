package com.example.vamonosrecio.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.google.android.gms.maps.model.LatLng

suspend fun fetchRouteWithOSRM(points: List<LatLng>): List<LatLng> {
    if (points.size < 2) return points

    val client = OkHttpClient()
    val coordinates = points.joinToString(";") { "${it.longitude},${it.latitude}" }
    val url = "https://router.project-osrm.org/route/v1/driving/$coordinates?overview=full&geometries=geojson"

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string() ?: return points

    val json = JSONObject(responseBody)
    val routes = json.getJSONArray("routes")
    if (routes.length() == 0) return points

    val geometry = routes.getJSONObject(0)
        .getJSONObject("geometry")
        .getJSONArray("coordinates")

    return (0 until geometry.length()).map { i ->
        val coord = geometry.getJSONArray(i)
        LatLng(coord.getDouble(1), coord.getDouble(0))
    }
}