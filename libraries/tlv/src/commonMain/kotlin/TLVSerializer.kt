import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TLVSerializer : KSerializer<TLV> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TLV", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TLV) {
        encoder.encodeString(value.hex)
    }

    override fun deserialize(decoder: Decoder): TLV =
        TLV.from(decoder.decodeString())
}
