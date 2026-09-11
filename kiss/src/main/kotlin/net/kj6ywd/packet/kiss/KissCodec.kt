package net.kj6ywd.packet.kiss

import java.io.ByteArrayOutputStream

data class KissFrame(
    val port: Int,
    val command: Int,
    val payload: ByteArray,
) {
    companion object {
        const val COMMAND_DATA = 0x00
    }
}

object KissCodec {
    const val FEND = 0xC0
    const val FESC = 0xDB
    const val TFEND = 0xDC
    const val TFESC = 0xDD

    fun encode(payload: ByteArray, port: Int = 0, command: Int = KissFrame.COMMAND_DATA): ByteArray {
        require(port in 0..15) { "KISS port must be 0..15" }
        require(command in 0..15) { "KISS command must be 0..15" }

        val out = ByteArrayOutputStream(payload.size + 3)
        out.write(FEND)
        writeEscaped(out, ((port shl 4) or command).toByte())
        payload.forEach { writeEscaped(out, it) }
        out.write(FEND)
        return out.toByteArray()
    }

    private fun writeEscaped(out: ByteArrayOutputStream, value: Byte) {
        when (value.toInt() and 0xFF) {
            FEND -> {
                out.write(FESC)
                out.write(TFEND)
            }
            FESC -> {
                out.write(FESC)
                out.write(TFESC)
            }
            else -> out.write(value.toInt() and 0xFF)
        }
    }
}

class KissDecoder {
    private val current = ByteArrayOutputStream()
    private var collecting = false
    private var escaped = false

    fun accept(bytes: ByteArray, length: Int = bytes.size): List<KissFrame> {
        require(length in 0..bytes.size)
        val frames = mutableListOf<KissFrame>()

        for (i in 0 until length) {
            val value = bytes[i].toInt() and 0xFF

            if (value == KissCodec.FEND) {
                if (collecting && current.size() > 0) {
                    decodeCurrent()?.let(frames::add)
                }
                current.reset()
                collecting = true
                escaped = false
                continue
            }

            if (!collecting) continue

            if (escaped) {
                when (value) {
                    KissCodec.TFEND -> current.write(KissCodec.FEND)
                    KissCodec.TFESC -> current.write(KissCodec.FESC)
                    else -> current.write(value)
                }
                escaped = false
                continue
            }

            if (value == KissCodec.FESC) {
                escaped = true
            } else {
                current.write(value)
            }
        }

        return frames
    }

    private fun decodeCurrent(): KissFrame? {
        val raw = current.toByteArray()
        if (raw.isEmpty()) return null
        val commandByte = raw[0].toInt() and 0xFF
        return KissFrame(
            port = (commandByte ushr 4) and 0x0F,
            command = commandByte and 0x0F,
            payload = raw.copyOfRange(1, raw.size),
        )
    }
}
