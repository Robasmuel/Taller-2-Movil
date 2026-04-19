package com.example.taller2movil.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val esquemaColoresOscuro = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color.White,
    secondary = Color(0xFFFF7043),
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1A),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun TemaFotoApp(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = esquemaColoresOscuro,
        content = content
    )
}