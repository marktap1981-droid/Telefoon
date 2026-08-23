package nl.voorraadbeheer.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// "Fresh Grocery" — felle, speelse kleuren met veel afgeronde vormen.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1FAE5C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F5E3),
    onPrimaryContainer = Color(0xFF0B3D22),
    secondary = Color(0xFFF5A524),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCEBCF),
    onSecondaryContainer = Color(0xFF6B4400),
    error = Color(0xFFE5484D),
    onError = Color.White,
    errorContainer = Color(0xFFFBDCDD),
    onErrorContainer = Color(0xFF6B1113),
    background = Color(0xFFFAFAF8),
    onBackground = Color(0xFF1B1C1A),
    surface = Color.White,
    onSurface = Color(0xFF1B1C1A),
    surfaceVariant = Color(0xFFEDF0EB),
    onSurfaceVariant = Color(0xFF444844),
    outline = Color(0xFFCDD1CB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF34C77A),
    onPrimary = Color(0xFF06281A),
    primaryContainer = Color(0xFF16482C),
    onPrimaryContainer = Color(0xFFB6F0CE),
    secondary = Color(0xFFF5A524),
    onSecondary = Color(0xFF2B1900),
    secondaryContainer = Color(0xFF493100),
    onSecondaryContainer = Color(0xFFFDE3B8),
    error = Color(0xFFFF6B6E),
    onError = Color(0xFF410004),
    errorContainer = Color(0xFF641416),
    onErrorContainer = Color(0xFFFFD9D9),
    background = Color(0xFF14171A),
    onBackground = Color(0xFFF1F3F1),
    surface = Color(0xFF1E2226),
    onSurface = Color(0xFFF1F3F1),
    surfaceVariant = Color(0xFF2A2F32),
    onSurfaceVariant = Color(0xFFC2C7C2),
    outline = Color(0xFF3C4145),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun VoorraadbeheerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
