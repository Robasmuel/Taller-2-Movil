package com.example.taller2movil

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.taller2movil.modelo.FotoRecorrido

class ModeloVistaRecorrido : ViewModel() {

    val fotos = mutableStateListOf<FotoRecorrido>()

    var recorridoActivo = mutableStateOf(false)
        private set

    fun agregarFoto(foto: FotoRecorrido) {
        fotos.add(foto)
    }

    fun iniciarRecorrido() {
        recorridoActivo.value = true
    }

    fun detenerRecorrido() {
        recorridoActivo.value = false
    }

    fun borrarRecorrido() {
        fotos.clear()
        recorridoActivo.value = false
    }
}