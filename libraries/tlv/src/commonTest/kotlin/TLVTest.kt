import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TLVTest {

    @Test
    fun `tlv from tag and value`() {
        val tlv1 = TLV.from(Tag.from(1L), Value.from(byteArrayOf(1)))
    }

    @Test
    fun `tlv from bytes - single byte tag and short form length`() {
        // Tag 6F, Length 07, Value = 7 bytes
        val bytes = byteArrayOf(
            0x6F.toByte(), 0x07.toByte(), // Tag=6F, Length=07
            0x84.toByte(), 0x02.toByte(), 0xAB.toByte(), 0xCD.toByte(), // Nested: Tag=84, Len=02, Val=ABCD
            0x50.toByte(), 0x01.toByte(), 0x41.toByte() // Nested: Tag=50, Len=01, Val=41 ('A')
        )
        val tlv = TLV.from(bytes)
        assertEquals("6F", tlv.tag.asHex.uppercase())
        assertEquals(7, tlv.length.lengthAsInt)
        assertEquals(7, tlv.value.size)
        assertEquals(9, tlv.size) // tag(1) + length(1) + value(7)
        assertTrue(tlv.isNested)
    }

    @Test
    fun `tlv from bytes - two byte tag`() {
        // Tag 9F38, Length 03, Value = 3 bytes
        val bytes = byteArrayOf(
            0x9F.toByte(), 0x38.toByte(), 0x03.toByte(), // Tag=9F38, Length=03
            0x01.toByte(), 0x02.toByte(), 0x03.toByte() // Value
        )
        val tlv = TLV.from(bytes)
        assertEquals("9F38", tlv.tag.asHex.uppercase())
        assertEquals(3, tlv.length.lengthAsInt)
        assertEquals(3, tlv.value.size)
        assertEquals(6, tlv.size) // tag(2) + length(1) + value(3)
    }

    @Test
    fun `tlv from bytes - long form length`() {
        // Tag 70, Length 81 80 (=128 bytes), Value = 128 bytes
        val valueBytes = ByteArray(128) { it.toByte() }
        val bytes = byteArrayOf(0x70.toByte(), 0x81.toByte(), 0x80.toByte()) + valueBytes
        val tlv = TLV.from(bytes)
        assertEquals("70", tlv.tag.asHex.uppercase())
        assertEquals(128, tlv.length.lengthAsInt)
        assertEquals(128, tlv.value.size)
        assertEquals(131, tlv.size) // tag(1) + length(2) + value(128)
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun `tlv from PPSE-like response`() {
        // Simulated PPSE response: 6F with nested A5 containing 61 with AID (4F)
        val ppseHex = "6F1A840E325041592E5359532E4444463031A5088801015F2D02656E"
        val bytes = ppseHex.hexToByteArray()
        val tlv = TLV.from(bytes)
        assertEquals("6F", tlv.tag.asHex.uppercase())
        assertTrue(tlv.isNested)
        assertTrue(tlv.size <= bytes.size)
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun `tlvlist from bytes parses multiple TLVs`() {
        // Three consecutive TLVs: [Tag=50 Len=04 Val="VISA"] [Tag=57 Len=02 Val=0102] [Tag=5A Len=04 Val=12345678]
        val hex = "500456495341570201025A0412345678"
        val bytes = hex.hexToByteArray()
        val list = TLVList.from(bytes)
        assertEquals(3, list.tlvList.size)
        assertEquals("50", list.tlvList[0].tag.asHex.uppercase())
        assertEquals(4, list.tlvList[0].value.size)
        assertEquals("57", list.tlvList[1].tag.asHex.uppercase())
        assertEquals(2, list.tlvList[1].value.size)
        assertEquals("5A", list.tlvList[2].tag.asHex.uppercase())
        assertEquals(4, list.tlvList[2].value.size)
    }

    @Test
    fun `tlvlist from empty bytes`() {
        val list = TLVList.from(byteArrayOf())
        assertEquals(0, list.tlvList.size)
    }

    @Test
    fun `tlv with zero-length value`() {
        // Tag=88 Len=00 (no value bytes)
        val bytes = byteArrayOf(0x88.toByte(), 0x00.toByte())
        val tlv = TLV.from(bytes)
        assertEquals("88", tlv.tag.asHex.uppercase())
        assertEquals(0, tlv.length.lengthAsInt)
        assertEquals(0, tlv.value.size)
        assertEquals(2, tlv.size)
    }
}
