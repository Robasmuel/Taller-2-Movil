package com.example.taller2movil.modelo

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class FotoRecorrido(
    val nombre: String,
    val uri: String,
    val latitud: Double,
    val longitud: Double
)