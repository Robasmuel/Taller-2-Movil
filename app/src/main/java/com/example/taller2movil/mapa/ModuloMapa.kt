package com.example.taller2movil.mapa

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taller2movil.ModeloVistaRecorrido
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ModuloMapa(
    modeloVista: ModeloVistaRecorrido,
    alCambiarUbicacion: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rastreadorUbicacion = remember { RastreadorUbicacion(context) }

    val permisoUbicacion = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    val puntosRuta = modeloVista.puntosRuta
    val fotos = modeloVista.fotos
    val recorridoActivo = modeloVista.recorridoActivo.value

    val cameraPositionState = rememberCameraPositionState()

    // 🔹 INICIAR GPS SOLO CUANDO YA HAY PERMISO
    LaunchedEffect(permisoUbicacion.status.isGranted) {
        if (permisoUbicacion.status.isGranted && locationCallback == null) {

            locationCallback = rastreadorUbicacion.iniciarActualizaciones { lat, lng ->
                val nueva = LatLng(lat, lng)

                ubicacionActual = nueva
                alCambiarUbicacion(nueva)

                if (modeloVista.recorridoActivo.value) {
                    modeloVista.agregarPuntoRuta(lat, lng)
                }
            }
        }
    }

    // 🔹 DETENER GPS
    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let {
                rastreadorUbicacion.detenerActualizaciones(it)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, Color(0xFFB8C7FA), RoundedCornerShape(12.dp))
    ) {

        // 🗺️ MAPA
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = permisoUbicacion.status.isGranted
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {

            // 🔵 RUTA
            if (puntosRuta.size >= 2) {
                Polyline(
                    points = puntosRuta.map {
                        LatLng(it.latitud, it.longitud)
                    },
                    color = Color(0xFF0B4EA2),
                    width = 12f
                )
            }

            // 📍 MARCADORES
            fotos.forEach { foto ->
                MarkerInfoWindow(
                    state = MarkerState(
                        position = LatLng(foto.latitud, foto.longitud)
                    ),
                    title = foto.nombre
                ) { marker ->

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp),
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = marker.title ?: "",
                            color = Color.Black,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // 🔹 BOTONES
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ▶️ INICIAR
                FloatingActionButton(
                    onClick = {
                        if (!permisoUbicacion.status.isGranted) {
                            permisoUbicacion.launchPermissionRequest()
                        } else {
                            modeloVista.iniciarRecorrido(
                                ubicacionActual?.latitude,
                                ubicacionActual?.longitude
                            )
                        }
                    },
                    containerColor = Color(0xFFD7E8FF),
                    contentColor = Color(0xFF184E93)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar")
                }

                // 🗑 BORRAR
                FloatingActionButton(
                    onClick = { modeloVista.borrarRecorrido() },
                    containerColor = Color(0xFFFFE5E5),
                    contentColor = Color.Red
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar")
                }
            }

            // 📍 BOTÓN CENTRAR
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        ubicacionActual?.let {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(it, 17f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp),
                containerColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Ubicación")
            }

            // 🧪 DEBUG (puedes quitar después)
            Text(
                text = "Puntos: ${puntosRuta.size}",
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.White)
                    .padding(8.dp)
            )
        }
    }
}