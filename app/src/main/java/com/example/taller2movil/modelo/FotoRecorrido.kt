package com.example.taller2movil.modelo

import android.net.Uri
//import com.google.android.gms.maps.model.LatLng
// import java.time.LocalDateTime

data class FotoRecorrido(
    val nombre: String,
    val uri: Uri,
    val latitud: Double,
    val longitud: Double
)