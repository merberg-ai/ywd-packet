package net.kj6ywd.packet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.kj6ywd.packet.core.Ax25Codec
import net.kj6ywd.packet.kiss.KissConnectionState
import net.kj6ywd.packet.kiss.KissFrame
import net.kj6ywd.packet.kiss.TcpKissClient

data class MonitorItem(
    val time: String,
    val summary: String,
    val detail: String,
)

data class PacketUiState(
    val host: String = "192.168.1.11",
    val port: String = "8001",
    val connection: KissConnectionState = KissConnectionState.Disconnected,
    val monitor: List<MonitorItem> = emptyList(),
)

class PacketViewModel : ViewModel() {
    private val client = TcpKissClient(viewModelScope)
    private val _uiState = MutableStateFlow(PacketUiState())
    val uiState: StateFlow<PacketUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            client.state.collect { connection ->
                _uiState.update { it.copy(connection = connection) }
            }
        }
        viewModelScope.launch {
            client.frames.collect { frame ->
                if (frame.command == KissFrame.COMMAND_DATA) addMonitorFrame(frame)
            }
        }
    }

    fun setHost(value: String) = _uiState.update { it.copy(host = value) }
    fun setPort(value: String) = _uiState.update { it.copy(port = value.filter(Char::isDigit)) }

    fun toggleConnection() {
        when (_uiState.value.connection) {
            is KissConnectionState.Connected,
            is KissConnectionState.Connecting -> client.disconnect()
            else -> {
                val port = _uiState.value.port.toIntOrNull() ?: return
                client.connect(_uiState.value.host.trim(), port)
            }
        }
    }

    private fun addMonitorFrame(frame: KissFrame) {
        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val item = runCatching {
            val ax25 = Ax25Codec.decode(frame.payload)
            MonitorItem(
                time = now,
                summary = ax25.monitorSummary(),
                detail = "KISS ${frame.port} // ${frame.payload.size} bytes // PID ${ax25.pid?.let { "%02X".format(it) } ?: "--"}",
            )
        }.getOrElse {
            MonitorItem(
                time = now,
                summary = "RAW AX.25 // ${frame.payload.size} bytes",
                detail = frame.payload.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) },
            )
        }

        _uiState.update { state ->
            state.copy(monitor = (state.monitor + item).takeLast(250))
        }
    }

    override fun onCleared() {
        client.disconnect()
        super.onCleared()
    }
}
