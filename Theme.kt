package com.example.streampulse.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    secondary = Color(0xFFA78BFA),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

@Composable
fun StreamPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}

val GroupPalette = listOf(
    Color(0xFF2563EB) to Color(0xFF06B6D4),
    Color(0xFFDC2626) to Color(0xFFF97316),
    Color(0xFF9333EA) to Color(0xFFEC4899),
    Color(0xFF059669) to Color(0xFF0D9488),
    Color(0xFFD97706) to Color(0xFFEAB308),
    Color(0xFF7C3AED) to Color(0xFFC026D3),
    Color(0xFFE11D48) to Color(0xFF7F1D1D),
    Color(0xFF16A34A) to Color(0xFF065F46)
)

fun groupColors(group: String): Pair<Color, Color> =
    GroupPalette[Math.floorMod(group.hashCode(), GroupPalette.size)]
