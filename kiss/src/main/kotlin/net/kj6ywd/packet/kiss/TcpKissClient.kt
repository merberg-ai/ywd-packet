package net.kj6ywd.packet.kiss

import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface KissConnectionState {
    data object Disconnected : KissConnectionState
    data class Connecting(val host: String, val port: Int) : KissConnectionState
    data class Connected(val host: String, val port: Int) : KissConnectionState
    data class Failed(val message: String) : KissConnectionState
}

class TcpKissClient(private val scope: CoroutineScope) {
    private val _state = MutableStateFlow<KissConnectionState>(KissConnectionState.Disconnected)
    val state: StateFlow<KissConnectionState> = _state.asStateFlow()

    private val _frames = MutableSharedFlow<KissFrame>(extraBufferCapacity = 64)
    val frames: SharedFlow<KissFrame> = _frames.asSharedFlow()

    private val writeMutex = Mutex()
    private var socket: Socket? = null
    private var readerJob: Job? = null

    fun connect(host: String, port: Int, timeoutMs: Int = 5_000) {
        require(host.isNotBlank()) { "Host must not be blank" }
        require(port in 1..65535) { "Port must be 1..65535" }

        disconnect()
        _state.value = KissConnectionState.Connecting(host, port)

        readerJob = scope.launch(Dispatchers.IO) {
            val newSocket = Socket()
            try {
                socket = newSocket
                newSocket.tcpNoDelay = true
                newSocket.connect(InetSocketAddress(host, port), timeoutMs)
                _state.value = KissConnectionState.Connected(host, port)

                val decoder = KissDecoder()
                val buffer = ByteArray(4096)
                val input = newSocket.getInputStream()
                while (!newSocket.isClosed) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    decoder.accept(buffer, count).forEach { _frames.emit(it) }
                }
                if (_state.value is KissConnectionState.Connected) {
                    _state.value = KissConnectionState.Disconnected
                }
            } catch (t: Throwable) {
                if (!newSocket.isClosed) {
                    _state.value = KissConnectionState.Failed(t.message ?: t::class.simpleName.orEmpty())
                }
            } finally {
                runCatching { newSocket.close() }
                if (socket === newSocket) socket = null
            }
        }
    }

    fun disconnect() {
        runCatching { socket?.close() }
        socket = null
        readerJob?.cancel()
        readerJob = null
        _state.value = KissConnectionState.Disconnected
    }

    suspend fun send(payload: ByteArray, port: Int = 0) {
        val active = socket ?: error("KISS socket is not connected")
        val encoded = KissCodec.encode(payload, port = port)
        writeMutex.withLock {
            withContext(Dispatchers.IO) {
                active.getOutputStream().apply {
                    write(encoded)
                    flush()
                }
            }
        }
    }
}
