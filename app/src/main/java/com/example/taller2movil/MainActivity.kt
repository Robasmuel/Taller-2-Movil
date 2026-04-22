package com.example.taller2movil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.example.taller2movil.camara.ModuloCamara
import com.example.taller2movil.ui.theme.TemaFotoApp
import com.example.taller2movil.mapa.ModuloMapa

class MainActivity : ComponentActivity() {

    private val modeloVista: ModeloVistaRecorrido by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaFotoApp {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppTaller2Movil(modeloVista = modeloVista)
                }
            }
        }
    }
}

@Composable
fun AppTaller2Movil(modeloVista: ModeloVistaRecorrido) {
    // Puente de ubicación entre módulos
    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Mitad superior (Módulo Cámara)
        Box(modifier = Modifier.weight(1f)) {
            ModuloCamara(
                modeloVista = modeloVista,
                ubicacionActual = ubicacionActual
            )
        }

        // Mitad inferior (Módulo Mapa)
        ModuloMapa(
            modeloVista = modeloVista,
            alCambiarUbicacion = { nuevaUbicacion ->
                // Bug corregido: ahora sí se pasa la ubicación real
                ubicacionActual = nuevaUbicacion
            },
            modifier = Modifier.weight(1f)
        )
    }
}