package com.example.taller2movil.mapa

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taller2movil.ModeloVistaRecorrido
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ModuloMapa(

    modeloVista: ModeloVistaRecorrido,
    alCambiarUbicacion: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val rastreadorUbicacion = remember { RastreadorUbicacion(context) }


    val permisoUbicacion = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
    val scope = rememberCoroutineScope()
    val recorridoActivo = modeloVista.recorridoActivo.value
    val recorridoActivoActualizado by rememberUpdatedState(recorridoActivo)
    val puntosRuta = modeloVista.puntosRuta
    val fotos = modeloVista.fotos

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        if (!permisoUbicacion.status.isGranted) {
            permisoUbicacion.launchPermissionRequest()
        }
    }

    LaunchedEffect(permisoUbicacion.status.isGranted) {
        if (permisoUbicacion.status.isGranted && locationCallback == null) {
            locationCallback = rastreadorUbicacion.iniciarActualizaciones { lat, lng ->
                val nuevaUbicacion = LatLng(lat, lng)
                ubicacionActual = nuevaUbicacion
                alCambiarUbicacion(nuevaUbicacion)

                if (recorridoActivoActualizado) {
                    modeloVista.agregarPuntoRuta(lat, lng)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let { rastreadorUbicacion.detenerActualizaciones(it) }
        }
    }

    if (!permisoUbicacion.status.isGranted) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFB8C7FA), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sorry, location permission is needed to track your location",
                color = Color.Red,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    LaunchedEffect(ubicacionActual) {
        ubicacionActual?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 17f),
                durationMs = 1000
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, Color(0xFFB8C7FA), RoundedCornerShape(12.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            if (puntosRuta.isNotEmpty()) {
                Polyline(
                    points = puntosRuta.map { punto ->
                        LatLng(punto.latitud, punto.longitud)
                    },
                    color = Color(0xFF0B4EA2),
                    width = 14f,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND,
                    pattern = emptyList<PatternItem>()
                )
            }

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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    ubicacionActual?.let {
                        modeloVista.iniciarRecorrido(it.latitude, it.longitude)
                    }
                    modeloVista.cargarRutaPrueba()
                },
                containerColor = Color(0xFFD7E8FF),
                contentColor = Color(0xFF184E93),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Iniciar recorrido"
                )
            }

            FloatingActionButton(
                onClick = { modeloVista.borrarRecorrido() },
                containerColor = Color(0xFFFFF1F1),
                contentColor = Color(0xFFB3261E),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar recorrido"
                )
            }
        }

        FloatingActionButton(
            onClick = {
                    scope.launch {
                        centrarEnUbicacionActual(
                            cameraPositionState = cameraPositionState,
                            ubicacionActual = ubicacionActual
                        )
                    }
                },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp),
            containerColor = Color.White,
            contentColor = Color(0xFFB3261E),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación"
            )
        }

        if (recorridoActivo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(
                        color = Color(0xCC0B4EA2),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Text(
                    text = "Recorriendo...",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private suspend fun centrarEnUbicacionActual(
    cameraPositionState: CameraPositionState,
    ubicacionActual: LatLng?
) {
    ubicacionActual?.let {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(it, 17f),
            durationMs = 800
        )
    }
}