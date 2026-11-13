package com.axondragonscale.wallmatic.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.axondragonscale.wallmatic.model.WallmaticConfig
import com.axondragonscale.wallmatic.proto.Config
import com.axondragonscale.wallmatic.util.toDomain
import com.axondragonscale.wallmatic.util.toProto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration

/**
 * Created by Ronak Harkhani on 19/06/24
 */

object ConfigSerializer : Serializer<Config> {
    override val defaultValue: Config
        get() = Config.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Config = Config.parseFrom(input)
    override suspend fun writeTo(t: Config, output: OutputStream) = t.writeTo(output)
}

private val Context.configStore: DataStore<Config> by dataStore(
    fileName = "wallmatic_config_store_v2",
    serializer = ConfigSerializer,
)

class ConfigRepository(private val context: Context) {

    val configFlow: Flow<WallmaticConfig> = context.configStore.data.map { it.toDomain() }

    /** Read-modify-write against the latest stored document; the only config write path. */
    suspend fun update(transform: (WallmaticConfig) -> WallmaticConfig) {
        context.configStore.updateData { current -> transform(current.toDomain()).toProto() }
    }

    suspend fun setInterval(interval: Duration) = update { it.copy(interval = interval) }
}
