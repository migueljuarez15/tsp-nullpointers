package com.example.vamonosrecio.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

fun decodePolyline(encodedPath: String): List<LatLng> {
    return PolyUtil.decode(encodedPath)
}
