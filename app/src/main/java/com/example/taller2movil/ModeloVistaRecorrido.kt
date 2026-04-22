package com.example.taller2movil

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.taller2movil.modelo.FotoRecorrido
import com.example.taller2movil.modelo.PuntoRuta

class ModeloVistaRecorrido : ViewModel() {

    val fotos = mutableStateListOf<FotoRecorrido>()
    val puntosRuta = mutableStateListOf<PuntoRuta>()

    var recorridoActivo = mutableStateOf(false)
        private set

    fun agregarFoto(foto: FotoRecorrido) {
        fotos.add(foto)
    }

    fun iniciarRecorrido(lat: Double?, lng: Double?) {
        recorridoActivo.value = true
        puntosRuta.clear()
        if (lat != null && lng != null) {
            agregarPuntoRuta(lat, lng)
        }
    }

    fun detenerRecorrido() {
        recorridoActivo.value = false
    }

    fun borrarRecorrido() {
        fotos.clear()
        puntosRuta.clear()
        recorridoActivo.value = false
    }

    fun agregarPuntoRuta(lat: Double, lng: Double) {
        if (!recorridoActivo.value) return
        puntosRuta.add(PuntoRuta(lat, lng))
    }
}