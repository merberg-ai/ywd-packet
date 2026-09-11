package net.kj6ywd.packet.core

import java.io.ByteArrayOutputStream

/** A single AX.25 address field. Bit 7 is exposed as [flag] because its meaning depends on position. */
data class Ax25Address(
    val callsign: String,
    val ssid: Int = 0,
    val flag: Boolean = false,
) {
    init {
        require(callsign.isNotBlank()) { "Callsign must not be blank" }
        require(callsign.length <= 6) { "AX.25 callsigns are at most six characters" }
        require(callsign.all { it.isLetterOrDigit() }) { "Callsign must be alphanumeric" }
        require(ssid in 0..15) { "SSID must be in 0..15" }
    }

    val normalizedCallsign: String = callsign.uppercase()

    override fun toString(): String = if (ssid == 0) normalizedCallsign else "$normalizedCallsign-$ssid"
}

data class Ax25Frame(
    val destination: Ax25Address,
    val source: Ax25Address,
    val digipeaters: List<Ax25Address>,
    val control: Int,
    val pid: Int?,
    val information: ByteArray,
) {
    val controlName: String get() = Ax25Codec.controlName(control)

    fun monitorSummary(): String {
        val path = if (digipeaters.isEmpty()) "" else digipeaters.joinToString(prefix = " via ") {
            if (it.flag) "${it}*" else it.toString()
        }
        val text = information.toString(Charsets.UTF_8)
            .replace("\r", "")
            .replace("\n", " ")
            .trim()
        val suffix = if (text.isBlank()) "" else "  $text"
        return "$source > $destination$path  $controlName$suffix"
    }
}

object Ax25Codec {
    const val PID_NO_LAYER_3 = 0xF0
    const val CONTROL_UI = 0x03

    fun encodeUi(
        destination: Ax25Address,
        source: Ax25Address,
        digipeaters: List<Ax25Address> = emptyList(),
        payload: ByteArray,
        pid: Int = PID_NO_LAYER_3,
    ): ByteArray {
        val addresses = listOf(destination, source) + digipeaters
        require(addresses.size <= 10) { "Too many AX.25 addresses" }

        val out = ByteArrayOutputStream()
        addresses.forEachIndexed { index, address ->
            out.write(encodeAddress(address, last = index == addresses.lastIndex))
        }
        out.write(CONTROL_UI)
        out.write(pid and 0xFF)
        out.write(payload)
        return out.toByteArray()
    }

    fun decode(frame: ByteArray): Ax25Frame {
        require(frame.size >= 15) { "AX.25 frame is too short" }

        var offset = 0
        val addresses = mutableListOf<Ax25Address>()
        var last = false
        while (!last) {
            require(offset + 7 <= frame.size) { "Truncated AX.25 address field" }
            val (address, isLast) = decodeAddress(frame, offset)
            addresses += address
            offset += 7
            last = isLast
            require(addresses.size <= 10) { "Too many AX.25 addresses" }
        }
        require(addresses.size >= 2) { "AX.25 frame needs destination and source" }
        require(offset < frame.size) { "Missing AX.25 control field" }

        val control = frame[offset++].toInt() and 0xFF
        val needsPid = isIFrame(control) || isUiFrame(control)
        val pid = if (needsPid) {
            require(offset < frame.size) { "Missing AX.25 PID" }
            frame[offset++].toInt() and 0xFF
        } else {
            null
        }

        return Ax25Frame(
            destination = addresses[0],
            source = addresses[1],
            digipeaters = addresses.drop(2),
            control = control,
            pid = pid,
            information = frame.copyOfRange(offset, frame.size),
        )
    }

    fun controlName(control: Int): String {
        val c = control and 0xFF
        return when {
            isIFrame(c) -> "I"
            (c and 0x03) == 0x01 -> when (c and 0x0F) {
                0x01 -> "RR"
                0x05 -> "RNR"
                0x09 -> "REJ"
                0x0D -> "SREJ"
                else -> "S?"
            }
            else -> when (c and 0xEF) {
                0x03 -> "UI"
                0x2F -> "SABM"
                0x6F -> "SABME"
                0x43 -> "DISC"
                0x63 -> "UA"
                0x0F -> "DM"
                0x87 -> "FRMR"
                else -> "U?"
            }
        }
    }

    fun isIFrame(control: Int): Boolean = (control and 0x01) == 0
    fun isUiFrame(control: Int): Boolean = (control and 0xEF) == CONTROL_UI

    private fun encodeAddress(address: Ax25Address, last: Boolean): ByteArray {
        val result = ByteArray(7)
        val padded = address.normalizedCallsign.padEnd(6, ' ')
        for (i in 0 until 6) {
            result[i] = (padded[i].code shl 1).toByte()
        }

        var ssidByte = 0x60 or ((address.ssid and 0x0F) shl 1)
        if (address.flag) ssidByte = ssidByte or 0x80
        if (last) ssidByte = ssidByte or 0x01
        result[6] = ssidByte.toByte()
        return result
    }

    private fun decodeAddress(frame: ByteArray, offset: Int): Pair<Ax25Address, Boolean> {
        val callsign = buildString {
            for (i in 0 until 6) {
                append(((frame[offset + i].toInt() and 0xFF) ushr 1).toChar())
            }
        }.trimEnd()
        val ssidByte = frame[offset + 6].toInt() and 0xFF
        val ssid = (ssidByte ushr 1) and 0x0F
        val flag = (ssidByte and 0x80) != 0
        val last = (ssidByte and 0x01) != 0
        return Ax25Address(callsign = callsign, ssid = ssid, flag = flag) to last
    }
}
