package org.nexlibre.orbita.feature

import android.text.format.DateFormat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.nexlibre.orbita.core.Appearance
import java.util.Date
import java.text.DateFormat as JavaDateFormat

fun themePalette(theme: String, systemDark: Boolean): ColorScheme = when (theme) {
    "amoled" -> darkColorScheme(primary = Color.White, onPrimary = Color.Black,
        background = Color.Black, onBackground = Color.White, surface = Color.Black, onSurface = Color.White,
        secondary = Color.White, secondaryContainer = Color(0xFF151515), onSecondaryContainer = Color.White,
        surfaceVariant = Color.Black, onSurfaceVariant = Color.White, outline = Color.White)
    "pixel" -> lightColorScheme(primary = Color(0xFF475D92), background = Color(0xFFF5F2FA),
        surface = Color(0xFFF5F2FA), secondaryContainer = Color(0xFFDCE2F9))
    "liquid" -> darkColorScheme(primary = Color(0xFFDAEEFF), background = Color(0xFF08131F),
        surface = Color(0xFF142C40), secondaryContainer = Color(0xFF28465F))
    else -> if (theme == "dark" || (theme == "auto" && systemDark))
        darkColorScheme(primary = Color(0xFFAFDFA1), background = Color(0xFF101813), surface = Color(0xFF17231D))
    else lightColorScheme(primary = Color(0xFF315E39), background = Color(0xFFF5F8F2), surface = Color(0xFFEDF3E9))
}

@Composable
fun ClockWidget(appearance: Appearance) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(Date()) }
    // Effect exists only while the home clock is visible; no frame-by-frame timer.
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(60_000 - System.currentTimeMillis() % 60_000) } }
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(DateFormat.getTimeFormat(context).format(now), fontSize = appearance.clockSize.sp,
            color = MaterialTheme.colorScheme.onBackground)
        if (appearance.clockDate) Text(JavaDateFormat.getDateInstance(JavaDateFormat.FULL).format(now),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun LauncherIcon(bitmap: ImageBitmap?, label: String, appearance: Appearance) {
    val size = appearance.iconSize.dp
    if (appearance.monochrome || appearance.theme == "amoled") {
        // Original white monograms preserve legibility without whitening opaque app icon backgrounds.
        Surface(Modifier.size(size), shape = RoundedCornerShape(14.dp), color = Color.Black,
            contentColor = Color.White, border = BorderStroke(1.dp, Color.White)) {
            Box(contentAlignment = Alignment.Center) {
                Text(label.take(2).uppercase(), fontSize = (appearance.iconSize * 0.32f).sp, maxLines = 1)
            }
        }
    } else if (bitmap != null) Image(bitmap, null, Modifier.size(size))
    else Text(label.take(1), Modifier.size(size), fontSize = 24.sp)
}

@Composable
fun pressScale(source: MutableInteractionSource, appearance: Appearance, reduced: Boolean): Float {
    val pressed by source.collectIsPressedAsState()
    val damping = when (appearance.motion) { "soft" -> 1f; "weighty" -> 0.65f; else -> 0.82f }
    val stiffness = when (appearance.motion) { "soft" -> 650f; "weighty" -> 220f; else -> 400f }
    val scale by animateFloatAsState(if (pressed && !reduced) 0.92f else 1f,
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness), label = "pressWeight")
    return if (reduced) 1f else scale
}
