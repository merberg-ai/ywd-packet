package net.kj6ywd.packet.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.kj6ywd.packet.MonitorItem
import net.kj6ywd.packet.PacketViewModel
import net.kj6ywd.packet.kiss.KissConnectionState
import net.kj6ywd.packet.ui.theme.YwdBackground
import net.kj6ywd.packet.ui.theme.YwdCyan
import net.kj6ywd.packet.ui.theme.YwdCyanDim
import net.kj6ywd.packet.ui.theme.YwdDanger
import net.kj6ywd.packet.ui.theme.YwdMagenta
import net.kj6ywd.packet.ui.theme.YwdMuted
import net.kj6ywd.packet.ui.theme.YwdPanel
import net.kj6ywd.packet.ui.theme.YwdPanelRaised
import net.kj6ywd.packet.ui.theme.YwdSilver

@Composable
fun PacketTerminalScreen(viewModel: PacketViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mono = FontFamily.Monospace

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YwdBackground)
            .padding(12.dp),
    ) {
        Header(state.connection, mono)
        Spacer(Modifier.height(10.dp))
        ConnectionPanel(
            host = state.host,
            port = state.port,
            connection = state.connection,
            onHostChange = viewModel::setHost,
            onPortChange = viewModel::setPort,
            onToggle = viewModel::toggleConnection,
            mono = mono,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            TabLabel("MONITOR", selected = true, mono = mono)
            Spacer(Modifier.width(4.dp))
            TabLabel("TERMINAL // P2", selected = false, mono = mono)
        }
        HorizontalDivider(color = YwdCyanDim)

        if (state.monitor.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "NO FRAMES // CONNECT TCP KISS",
                    color = YwdMuted,
                    fontFamily = mono,
                    fontSize = 13.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.monitor.asReversed()) { item -> MonitorRow(item, mono) }
            }
        }

        HorizontalDivider(color = YwdCyanDim)
        Text(
            text = "P0 // PASSIVE MONITOR  •  TX SESSION ENGINE NOT ENABLED",
            color = YwdMuted,
            fontFamily = mono,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 7.dp),
        )
    }
}

@Composable
private fun Header(connection: KissConnectionState, mono: FontFamily) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("YWD // PACKET", color = YwdCyan, fontFamily = mono, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("AX.25 FIELD TERMINAL", color = YwdMagenta, fontFamily = mono, fontSize = 11.sp)
        }
        val (label, color) = when (connection) {
            is KissConnectionState.Connected -> "● LINK" to YwdCyan
            is KissConnectionState.Connecting -> "◌ LINK" to YwdMagenta
            is KissConnectionState.Failed -> "× FAIL" to YwdDanger
            KissConnectionState.Disconnected -> "○ OFF" to YwdMuted
        }
        Text(label, color = color, fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConnectionPanel(
    host: String,
    port: String,
    connection: KissConnectionState,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onToggle: () -> Unit,
    mono: FontFamily,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YwdPanel, RoundedCornerShape(3.dp))
            .border(BorderStroke(1.dp, YwdCyanDim), RoundedCornerShape(3.dp))
            .padding(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = host,
                onValueChange = onHostChange,
                modifier = Modifier.weight(1f),
                label = { Text("TNC HOST", fontFamily = mono, fontSize = 10.sp) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = mono, color = YwdSilver, fontSize = 13.sp),
                colors = terminalFieldColors(),
            )
            Spacer(Modifier.width(7.dp))
            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                modifier = Modifier.width(92.dp),
                label = { Text("PORT", fontFamily = mono, fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = mono, color = YwdSilver, fontSize = 13.sp),
                colors = terminalFieldColors(),
            )
        }
        Spacer(Modifier.height(7.dp))
        Button(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (connection is KissConnectionState.Connected) YwdMagenta else YwdCyan,
                contentColor = YwdBackground,
            ),
            shape = RoundedCornerShape(2.dp),
        ) {
            Text(
                text = if (connection is KissConnectionState.Connected || connection is KissConnectionState.Connecting) "DISCONNECT" else "CONNECT TCP KISS",
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
            )
        }
        if (connection is KissConnectionState.Failed) {
            Text(
                text = "ERR // ${connection.message}",
                color = YwdDanger,
                fontFamily = mono,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun terminalFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = YwdCyan,
    unfocusedBorderColor = YwdCyanDim,
    focusedLabelColor = YwdCyan,
    unfocusedLabelColor = YwdMuted,
    cursorColor = YwdMagenta,
)

@Composable
private fun TabLabel(text: String, selected: Boolean, mono: FontFamily) {
    Text(
        text = text,
        color = if (selected) YwdBackground else YwdMuted,
        fontFamily = mono,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier
            .background(if (selected) YwdCyan else YwdPanelRaised)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun MonitorRow(item: MonitorItem, mono: FontFamily) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YwdPanelRaised)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row {
            Text(item.time, color = YwdMagenta, fontFamily = mono, fontSize = 10.sp)
            Spacer(Modifier.width(8.dp))
            Text(item.summary, color = YwdSilver, fontFamily = mono, fontSize = 11.sp, modifier = Modifier.weight(1f))
        }
        Text(item.detail, color = YwdMuted, fontFamily = mono, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
