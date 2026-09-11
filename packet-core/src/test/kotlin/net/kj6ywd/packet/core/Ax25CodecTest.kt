package net.kj6ywd.packet.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Ax25CodecTest {
    @Test
    fun uiFrameRoundTrips() {
        val payload = "YWDXR KJ6YWD-11 - TEST".toByteArray()
        val encoded = Ax25Codec.encodeUi(
            destination = Ax25Address("BEACON"),
            source = Ax25Address("KJ6YWD", 11),
            digipeaters = listOf(Ax25Address("YWDNOD", flag = true)),
            payload = payload,
        )

        val decoded = Ax25Codec.decode(encoded)
        assertEquals("BEACON", decoded.destination.toString())
        assertEquals("KJ6YWD-11", decoded.source.toString())
        assertEquals("YWDNOD", decoded.digipeaters.single().toString())
        assertEquals(true, decoded.digipeaters.single().flag)
        assertEquals("UI", decoded.controlName)
        assertEquals(0xF0, decoded.pid)
        assertArrayEquals(payload, decoded.information)
    }

    @Test
    fun knownControlNamesAreDecoded() {
        assertEquals("I", Ax25Codec.controlName(0x00))
        assertEquals("RR", Ax25Codec.controlName(0x01))
        assertEquals("SABM", Ax25Codec.controlName(0x2F))
        assertEquals("UA", Ax25Codec.controlName(0x63))
        assertEquals("DISC", Ax25Codec.controlName(0x43))
    }
}
