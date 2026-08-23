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
private val FreshGreen = Color(0xFF1FAE5C)
private val FreshGreenDark = Color(0xFF0B3D22)
private val FreshGreenContainer = Color(0xFFD9F5E3)
private val FreshAmber = Color(0xFFF5A524)
private val FreshAmberContainer = Color(0xFFFCEBCF)
private val FreshRed = Color(0xFFE5484D)
private val FreshRedContainer = Color(0xFFFBDCDD)

private val LightColors = lightColorScheme(
    primary = FreshGreen,
    onPrimary = Color.White,
    primaryContainer = FreshGreenContainer,
    onPrimaryContainer = FreshGreenDark,
    secondary = FreshAmber,
    onSecondary = Color.White,
    secondaryContainer = FreshAmberContainer,
    onSecondaryContainer = Color(0xFF6B4400),
    error = FreshRed,
    onError = Color.White,
    errorContainer = FreshRedContainer,
    onErrorContainer = Color(0xFF6B1113),
    background = Color(0xFFFAFAF8),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FD79A),
    onPrimary = FreshGreenDark,
    primaryContainer = Color(0xFF115C33),
    onPrimaryContainer = FreshGreenContainer,
    secondary = FreshAmber,
    onSecondary = Color(0xFF3D2800),
    error = Color(0xFFF2A0A2),
    onError = Color(0xFF5C0D0F),
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
