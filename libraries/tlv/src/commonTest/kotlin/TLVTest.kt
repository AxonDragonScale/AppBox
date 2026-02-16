import kotlin.test.Test

class TLVTest {

    @Test
    fun `tlv from tag and value`() {
        val tlv1 = TLV.from(Tag.from(1L), Value.from(byteArrayOf(1)))
    }
}