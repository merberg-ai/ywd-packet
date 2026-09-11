package net.kj6ywd.packet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val YwdBackground = Color(0xFF05080A)
val YwdPanel = Color(0xFF0B1116)
val YwdPanelRaised = Color(0xFF111A21)
val YwdCyan = Color(0xFF43E7FF)
val YwdCyanDim = Color(0xFF1C8190)
val YwdMagenta = Color(0xFFFF4FC8)
val YwdSilver = Color(0xFFB8C5CC)
val YwdMuted = Color(0xFF6E7D86)
val YwdDanger = Color(0xFFFF5570)

private val YwdColors = darkColorScheme(
    primary = YwdCyan,
    secondary = YwdMagenta,
    background = YwdBackground,
    surface = YwdPanel,
    surfaceVariant = YwdPanelRaised,
    onPrimary = YwdBackground,
    onSecondary = YwdBackground,
    onBackground = YwdSilver,
    onSurface = YwdSilver,
    error = YwdDanger,
)

@Composable
fun YwdPacketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YwdColors,
        content = content,
    )
}
