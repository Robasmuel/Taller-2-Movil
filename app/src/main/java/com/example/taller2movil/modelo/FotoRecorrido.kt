package com.example.taller2movil.modelo

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class FotoRecorrido(
    val uri: Uri,
    val nombreArchivo: String,
    val tomadaEn: LocalDateTime = LocalDateTime.now(),
    val ubicacion: LatLng
)