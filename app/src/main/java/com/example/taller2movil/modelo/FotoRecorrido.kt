package com.example.taller2movil.modelo

import android.net.Uri

data class FotoRecorrido(
    val nombre: String,
    val uri: Uri,
    val latitud: Double,
    val longitud: Double
)