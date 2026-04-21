package com.example.taller2movil

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.taller2movil.modelo.FotoRecorrido
import com.example.taller2movil.modelo.PuntoRuta


class ModeloVistaRecorrido : ViewModel() {

    val fotos = mutableStateListOf<FotoRecorrido>()

    var recorridoActivo = mutableStateOf(false)
        private set
    //lista de puntos de la ruta
    val puntosRuta = mutableStateListOf<PuntoRuta>()

    fun agregarFoto(foto: FotoRecorrido) {
        fotos.add(foto)
    }

    fun iniciarRecorrido(lat: Double?, lng: Double?) {
        recorridoActivo.value = true
        puntosRuta.clear()

        val lat = null
        if (lat!= null && lng!= null) {
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
    // Agregar puntos de la ruta
    fun agregarPuntoRuta(lat: Double, lng: Double) {
        if (!recorridoActivo.value) return

        val nuevo = PuntoRuta(lat, lng)

        val ultimo = puntosRuta.lastOrNull()

        // evitar duplicados exactos
        if (ultimo != null &&
            ultimo.latitud == nuevo.latitud &&
            ultimo.longitud == nuevo.longitud
        ) return

        puntosRuta.add(nuevo)
    }

    fun cargarRutaPrueba() {
        puntosRuta.clear()
        puntosRuta.add(com.example.taller2movil.modelo.PuntoRuta(4.6097, -74.0817))
        puntosRuta.add(com.example.taller2movil.modelo.PuntoRuta(4.6099, -74.0820))
        puntosRuta.add(com.example.taller2movil.modelo.PuntoRuta(4.6102, -74.0823))
    }
}