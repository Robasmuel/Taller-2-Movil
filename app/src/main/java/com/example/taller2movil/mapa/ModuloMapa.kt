package com.example.taller2movil.mapa

import android.Manifest
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.taller2movil.ModeloVistaRecorrido

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ModuloMapa(
    modeloVista: ModeloVistaRecorrido,
    alCambiarUbicacion: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val permisoUbicacion = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!permisoUbicacion.status.isGranted) {
            permisoUbicacion.launchPermissionRequest()
        }
    }

    if (!permisoUbicacion.status.isGranted) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(1.dp, Color(0xFFB8C7FA)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sorry, location permission is needed to track your location",
                color = Color.Red
            )
        }
        return
    }

    val bogota = LatLng(4.6097, -74.0817)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 14f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}