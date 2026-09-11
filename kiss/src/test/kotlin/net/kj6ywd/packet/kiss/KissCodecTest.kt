package net.kj6ywd.packet.kiss

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class KissCodecTest {
    @Test
    fun escapingRoundTripsAcrossChunks() {
        val payload = byteArrayOf(0x01, 0xC0.toByte(), 0x02, 0xDB.toByte(), 0x03)
        val encoded = KissCodec.encode(payload)
        val decoder = KissDecoder()

        val split = encoded.size / 2
        val first = decoder.accept(encoded.copyOfRange(0, split))
        val second = decoder.accept(encoded.copyOfRange(split, encoded.size))

        assertEquals(0, first.size)
        assertEquals(1, second.size)
        assertEquals(0, second.single().port)
        assertEquals(KissFrame.COMMAND_DATA, second.single().command)
        assertArrayEquals(payload, second.single().payload)
    }
}
