import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TLVSerializerTest {

    @Test
    fun roundTripsTlvsAsHex() {
        val original = listOf(TLV.from("5A021234"), TLV.from("500456495341"))
        val serializer = ListSerializer(TLVSerializer)

        val encoded = Json.encodeToString(serializer, original)
        val decoded = Json.decodeFromString(serializer, encoded)

        assertEquals("[\"5a021234\",\"500456495341\"]", encoded)
        assertEquals(original.map(TLV::hex), decoded.map(TLV::hex))
    }
}
