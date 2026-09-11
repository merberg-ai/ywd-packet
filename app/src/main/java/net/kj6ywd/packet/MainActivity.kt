package net.kj6ywd.packet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.kj6ywd.packet.ui.PacketTerminalScreen
import net.kj6ywd.packet.ui.theme.YwdPacketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YwdPacketTheme {
                PacketTerminalScreen()
            }
        }
    }
}
