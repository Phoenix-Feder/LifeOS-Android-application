package com.lifeos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Google Calendar-inspired palette: white surfaces, Google blue accent,
// neutral grays, saturated event colors for priority/status.
private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueContainer = Color(0xFFD2E3FC)
private val GoogleGreen = Color(0xFF188038)
private val Ink = Color(0xFF3C4043)
private val InkSoft = Color(0xFF5F6368)
private val Paper = Color(0xFFFFFFFF)
private val PaperDim = Color(0xFFF8F9FA)
private val Hairline = Color(0xFFDADCE0)

private val LightColors = lightColorScheme(
    primary = GoogleBlue,
    onPrimary = Color.White,
    primaryContainer = GoogleBlueContainer,
    onPrimaryContainer = Color(0xFF0842A0),
    secondary = GoogleGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEEAD6),
    background = PaperDim,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = InkSoft,
    outline = Hairline,
    error = Color(0xFFD93025)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0842A0),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = GoogleBlueContainer,
    secondary = Color(0xFF81C995),
    background = Color(0xFF202124),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF2A2B2E),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF35363A),
    onSurfaceVariant = Color(0xFFBDC1C6),
    outline = Color(0xFF5F6368)
)

val LifeOSTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 17.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.2.sp)
)

val LifeOSShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun LifeOSTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = LifeOSTypography, shapes = LifeOSShapes, content = content)
}
