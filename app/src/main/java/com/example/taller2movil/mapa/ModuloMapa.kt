package com.example.taller2movil.mapa

import android.Manifest
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    val cameraPositionState = rememberCameraPositionState()

    // Iniciar GPS cuando hay permiso
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

    // Detener GPS al salir
    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let {
                rastreadorUbicacion.detenerActualizaciones(it)
            }
        }
    }

    // Bug corregido: botones FUERA del GoogleMap, dentro de un Box
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, Color(0xFFB8C7FA), RoundedCornerShape(12.dp))
    ) {
        // Mapa
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
            // Ruta
            if (puntosRuta.size >= 2) {
                Polyline(
                    points = puntosRuta.map { LatLng(it.latitud, it.longitud) },
                    color = Color(0xFF0B4EA2),
                    width = 12f
                )
            }

            // Marcadores de fotos con miniatura
            fotos.forEach { foto ->
                val context = LocalContext.current
                MarkerInfoWindow(
                    state = MarkerState(
                        position = LatLng(foto.latitud, foto.longitud)
                    ),
                    title = foto.nombre
                ) { marker ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = foto.uri,
                                contentDescription = foto.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = foto.nombre
                                    .removePrefix("TourFoto_")
                                    .removeSuffix(".jpg"),
                                color = Color.Black,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = "%.5f, %.5f".format(foto.latitud, foto.longitud),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Botones de control — ahora correctamente fuera del GoogleMap
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón iniciar recorrido/detener recorrido
            // Botón iniciar/detener recorrido (toggle)
            FloatingActionButton(
                onClick = {
                    if (!permisoUbicacion.status.isGranted) {
                        permisoUbicacion.launchPermissionRequest()
                    } else if (modeloVista.recorridoActivo.value) {
                        modeloVista.detenerRecorrido()
                    } else {
                        modeloVista.iniciarRecorrido(
                            ubicacionActual?.latitude,
                            ubicacionActual?.longitude
                        )
                    }
                },
                containerColor = if (modeloVista.recorridoActivo.value)
                    Color(0xFFFFE5E5) else Color(0xFFD7E8FF),
                contentColor = if (modeloVista.recorridoActivo.value)
                    Color.Red else Color(0xFF184E93)
            ) {
                Icon(
                    imageVector = if (modeloVista.recorridoActivo.value)
                        Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (modeloVista.recorridoActivo.value)
                        "Detener" else "Iniciar"
                )
            }

            // Botón borrar recorrido
            FloatingActionButton(
                onClick = { modeloVista.borrarRecorrido() },
                containerColor = Color(0xFFFFE5E5),
                contentColor = Color.Red
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar")
            }
        }

        // Botón centrar ubicación
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


    }
}