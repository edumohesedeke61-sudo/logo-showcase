package ng.lightgrab.courier.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Brand {
    val Navy = Color(0xFF14213D)
    val Forest = Color(0xFF386641)
    val Leaf = Color(0xFFA7C957)
    val Cream = Color(0xFFF2E8CF)
    val White = Color(0xFFFFFFFF)
    val Sand = Color(0xFFE6DAB8)
    val Muted = Color(0xFF55607A)
    val Danger = Color(0xFFB3261E)
    val Warning = Color(0xFFE9A23B)
}

private val Colors = lightColorScheme(
    primary = Brand.Forest,
    onPrimary = Brand.White,
    primaryContainer = Brand.Leaf,
    onPrimaryContainer = Brand.Navy,
    secondary = Brand.Navy,
    onSecondary = Brand.White,
    secondaryContainer = Brand.Sand,
    onSecondaryContainer = Brand.Navy,
    tertiary = Brand.Leaf,
    onTertiary = Brand.Navy,
    background = Brand.Cream,
    onBackground = Brand.Navy,
    surface = Brand.White,
    onSurface = Brand.Navy,
    surfaceVariant = Brand.Sand,
    onSurfaceVariant = Brand.Muted,
    surfaceContainer = Brand.White,
    surfaceContainerLow = Brand.White,
    surfaceContainerHigh = Brand.White,
    error = Brand.Danger,
    onError = Brand.White,
    outline = Color(0xFF8A93A8),
)

// Larger-than-default type for outdoor readability.
private val Type = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold),
)

@Composable
fun LightGrabTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, content = content)
}
