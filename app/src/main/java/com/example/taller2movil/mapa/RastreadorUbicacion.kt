package com.example.taller2movil.mapa

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*

class RastreadorUbicacion(context: Context) {

    private val clienteUbicacion = LocationServices.getFusedLocationProviderClient(context)

    private val solicitudUbicacion = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        3000L
    ).apply {
        setMinUpdateIntervalMillis(2000L)
    }.build()

    @SuppressLint("MissingPermission")
    fun iniciarActualizaciones(
        alRecibirUbicacion: (Double, Double) -> Unit
    ): LocationCallback {
        val callback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                val ubicacion = resultado.lastLocation ?: return
                alRecibirUbicacion(ubicacion.latitude, ubicacion.longitude)
            }
        }

        clienteUbicacion.requestLocationUpdates(
            solicitudUbicacion,
            callback,
            Looper.getMainLooper()
        )

        return callback
    }

    fun detenerActualizaciones(callback: LocationCallback) {
        clienteUbicacion.removeLocationUpdates(callback)
    }
}