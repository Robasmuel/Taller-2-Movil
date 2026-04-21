package com.example.taller2movil.camara

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.accompanist.permissions.*
import com.google.android.gms.maps.model.LatLng
import com.example.taller2movil.ModeloVistaRecorrido
import com.example.taller2movil.modelo.FotoRecorrido
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ModuloCamara(
    modeloVista: ModeloVistaRecorrido,
    ubicacionActual: LatLng?
) {
    val estadoPermisoCamara = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!estadoPermisoCamara.status.isGranted) {
            estadoPermisoCamara.launchPermissionRequest()
        }
    }

    when {
        estadoPermisoCamara.status.isGranted -> {
            ContenidoCamara(
                fotos = modeloVista.fotos,
                recorridoActivo = modeloVista.recorridoActivo.value,
                ubicacionActual = ubicacionActual,
                alTomarFoto = { foto -> modeloVista.agregarFoto(foto) }
            )
        }
        estadoPermisoCamara.status.shouldShowRationale -> {
            ContenidoPermisoDenegado(
                mensaje = "La aplicación necesita acceso a la cámara para tomar fotos durante el recorrido.",
                alReintentar = { estadoPermisoCamara.launchPermissionRequest() }
            )
        }
        else -> {
            ContenidoPermisoDenegado(
                mensaje = "Permiso de cámara denegado. Ve a Ajustes → Aplicaciones → Taller2Movil → Permisos y activa la cámara.",
                alReintentar = { estadoPermisoCamara.launchPermissionRequest() }
            )
        }
    }
}

@Composable
private fun ContenidoCamara(
    fotos: List<FotoRecorrido>,
    recorridoActivo: Boolean,
    ubicacionActual: LatLng?,
    alTomarFoto: (FotoRecorrido) -> Unit
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var ladoLente by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var capturadorImagen: ImageCapture? by remember { mutableStateOf(null) }
    val ejecutorCamara: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val vistaPreviaView = remember {
        PreviewView(contexto).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(ladoLente) {
        val futuroProveedor = ProcessCameraProvider.getInstance(contexto)
        futuroProveedor.addListener({
            val proveedorCamara = futuroProveedor.get()
            val vistaPrevia = Preview.Builder().build().also {
                it.surfaceProvider = vistaPreviaView.surfaceProvider
            }
            val capturaImagen = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            capturadorImagen = capturaImagen

            val selectorCamara = CameraSelector.Builder()
                .requireLensFacing(ladoLente)
                .build()

            try {
                proveedorCamara.unbindAll()
                proveedorCamara.bindToLifecycle(lifecycleOwner, selectorCamara, vistaPrevia, capturaImagen)
            } catch (e: Exception) {
                Log.e("ModuloCamara", "Error al enlazar la cámara", e)
            }
        }, ContextCompat.getMainExecutor(contexto))
    }

    DisposableEffect(Unit) {
        onDispose { ejecutorCamara.shutdown() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(factory = { vistaPreviaView }, modifier = Modifier.fillMaxSize())

            // Gradiente superior con badge REC
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color(0xCC000000), Color.Transparent)))
            ) {
                if (recorridoActivo) {
                    InsigniaRecorrido(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )
                }

                if (ubicacionActual == null && recorridoActivo) {
                    Text(
                        text = "Sin GPS",
                        color = Color(0xFFFFCC00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color(0x99000000), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Gradiente inferior con botones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonCambiarCamara(alHacerClick = {
                        ladoLente = if (ladoLente == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                    })
                    BotonDisparador(
                        habilitado = recorridoActivo,
                        alHacerClick = {
                            tomarFoto(
                                contexto = contexto,
                                capturadorImagen = capturadorImagen,
                                ejecutor = ejecutorCamara,
                                ubicacion = ubicacionActual,
                                alGuardarFoto = alTomarFoto
                            )
                        }
                    )
                    Spacer(modifier = Modifier.size(56.dp))
                }
            }

            // Overlay cuando el recorrido no está activo
            if (!recorridoActivo) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x55000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inicia el recorrido\npara tomar fotos",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        TiraFotos(fotos = fotos)
    }
}

@Composable
private fun InsigniaRecorrido(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0xCCE53935), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(7.dp).background(Color.White, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text("REC", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BotonDisparador(habilitado: Boolean, alHacerClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (habilitado) Color(0xFF5B9BD5) else Color(0xFFB0BEC5)
            )
            .clickable(enabled = habilitado, onClick = alHacerClick)
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Tomar foto",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun BotonCambiarCamara(alHacerClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF5B9BD5))
            .clickable(onClick = alHacerClick)
    ) {
        Icon(
            imageVector = Icons.Default.FlipCameraAndroid,
            contentDescription = "Cambiar cámara",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun TiraFotos(fotos: List<FotoRecorrido>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Fotos del recorrido (${fotos.size})", color = Color(0xFF888888), fontSize = 12.sp)
        }

        if (fotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Las fotos tomadas aparecerán aquí", color = Color(0xFFAAAAAA), fontSize = 12.sp)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                items(fotos, key = { it.nombre }) { foto ->
                    MiniaturaFoto(foto)
                }
            }
        }
    }
}

@Composable
private fun MiniaturaFoto(foto: FotoRecorrido) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = foto.uri,
            contentDescription = foto.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
        )
        Text(
            text = foto.nombre.removePrefix("TourFoto_").removeSuffix(".jpg").take(8),
            color = Color.White,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp)
        )
    }
}

@Composable
fun ContenidoPermisoDenegado(mensaje: String, alReintentar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mensaje,
                color = Color(0xFFE53935),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = alReintentar) {
                Text("Conceder permiso", color = Color(0xFF1565C0))
            }
        }
    }
}

private fun tomarFoto(
    contexto: Context,
    capturadorImagen: ImageCapture?,
    ejecutor: ExecutorService,
    ubicacion: LatLng?,
    alGuardarFoto: (FotoRecorrido) -> Unit
) {
    val captura = capturadorImagen ?: return

    val marcaTiempo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val nombreArchivo = "TourFoto_$marcaTiempo.jpg"

    val valoresContenido = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Taller2Movil")
        }
    }

    val opcionesSalida = ImageCapture.OutputFileOptions.Builder(
        contexto.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        valoresContenido
    ).build()

    captura.takePicture(opcionesSalida, ejecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                val uriGuardado: Uri = resultado.savedUri ?: Uri.EMPTY
                val nuevaFoto = FotoRecorrido(
                    uri = uriGuardado.toString(),
                    nombre = nombreArchivo,
                    latitud = ubicacion?.latitude ?: 0.0,
                    longitud = ubicacion?.longitude ?: 0.0
                )
                ContextCompat.getMainExecutor(contexto).execute {
                    alGuardarFoto(nuevaFoto)
                    Toast.makeText(contexto, "📸 Foto guardada en la galería", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(excepcion: ImageCaptureException) {
                Log.e("ModuloCamara", "Error al capturar: ${excepcion.message}", excepcion)
                ContextCompat.getMainExecutor(contexto).execute {
                    Toast.makeText(contexto, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}