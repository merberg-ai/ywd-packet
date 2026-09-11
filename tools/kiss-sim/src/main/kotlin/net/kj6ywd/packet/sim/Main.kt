package net.kj6ywd.packet.sim

import java.net.ServerSocket
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread
import net.kj6ywd.packet.core.Ax25Address
import net.kj6ywd.packet.core.Ax25Codec
import net.kj6ywd.packet.kiss.KissCodec
import net.kj6ywd.packet.kiss.KissDecoder

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toIntOrNull() ?: 8001
    val clients = CopyOnWriteArrayList<java.net.Socket>()
    val server = ServerSocket(port)

    println("YWD // KISS SIM")
    println("Listening on 0.0.0.0:$port")
    println("Point YWD Packet at this machine to test without RF.")

    thread(isDaemon = true, name = "sim-beacon") {
        var counter = 1
        while (true) {
            Thread.sleep(3_000)
            val text = "YWD PACKET SIM ${counter++} @ ${LocalTime.now().withNano(0)}"
            val ax25 = Ax25Codec.encodeUi(
                destination = Ax25Address("BEACON"),
                source = Ax25Address("KJ6YWD", 11),
                digipeaters = listOf(Ax25Address("YWDNOD", flag = true)),
                payload = text.toByteArray(),
            )
            val kiss = KissCodec.encode(ax25)
            clients.removeIf { socket ->
                runCatching {
                    socket.getOutputStream().apply {
                        write(kiss)
                        flush()
                    }
                    false
                }.getOrElse {
                    runCatching { socket.close() }
                    true
                }
            }
        }
    }

    while (true) {
        val socket = server.accept()
        socket.tcpNoDelay = true
        clients += socket
        println("Client connected: ${socket.inetAddress.hostAddress}:${socket.port}")

        thread(isDaemon = true, name = "sim-client-${socket.port}") {
            val decoder = KissDecoder()
            val buffer = ByteArray(4096)
            runCatching {
                val input = socket.getInputStream()
                while (!socket.isClosed) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    decoder.accept(buffer, count).forEach { frame ->
                        println("RX KISS port=${frame.port} cmd=${frame.command} bytes=${frame.payload.size}")
                    }
                }
            }
            clients -= socket
            runCatching { socket.close() }
            println("Client disconnected: ${socket.inetAddress.hostAddress}:${socket.port}")
        }
    }
}
