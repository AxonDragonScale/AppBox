package com.axondragonscale.jest.model.serialization

import com.axondragonscale.jest.model.IJoke
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.TwoPartJoke
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Created by Ronak Harkhani on 03/04/24
 */
object JokeSerializer : JsonContentPolymorphicSerializer<IJoke>(IJoke::class) {

    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<IJoke> =
        when (val type = element.jsonObject.get("type")?.jsonPrimitive?.content) {
            "single" -> OnePartJoke.serializer()
            "twopart" -> TwoPartJoke.serializer()
            else -> throw IllegalArgumentException("Type $type is not supported.")
        }

}
