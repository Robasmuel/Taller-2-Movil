package com.example.taller2movil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.google.android.gms.maps.model.LatLng
import com.example.taller2movil.camara.ModuloCamara
import com.example.taller2movil.ui.theme.TemaFotoApp

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
    // Puente de ubicación: Persona 2 actualiza esto, Persona 1 lo lee
    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Mitad superior — Módulo Cámara (Persona 1)
        Box(modifier = Modifier.weight(1f)) {
            ModuloCamara(
                modeloVista = modeloVista,
                ubicacionActual = ubicacionActual
            )
        }

        // Mitad inferior — Módulo Mapa (Persona 2)
        // TODO Persona 2: reemplaza este Box con tu ModuloMapa
        // ModuloMapa(
        //     modeloVista = modeloVista,
        //     alCambiarUbicacion = { ubicacionActual = it }
        // )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = "[ Módulo Mapa — Persona 2 ]",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}