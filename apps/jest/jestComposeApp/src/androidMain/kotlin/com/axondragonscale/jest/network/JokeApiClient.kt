package com.axondragonscale.jest.network

import com.axondragonscale.jest.network.response.JokeApiResponse
import com.axondragonscale.jest.network.response.JokeListApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url

/**
 * Created by Ronak Harkhani on 02/04/24
 */
class JokeApiClient(
    private val httpClient: HttpClient,
) {

    companion object {
        private const val BASE_URL = "https://v2.jokeapi.dev"
    }

    // TODO: Handle exceptions and errors properly

    suspend fun getJokes(
        category: String = "Any",
        amount: Int,
        lang: String? = null,
        blacklistFlags: String? = null,
        type: String? = null,
    ): JokeListApiResponse = httpClient.get {
        url("$BASE_URL/joke/$category")

        // Query Parameters
        parameter("amount", amount)
        parameter("lang", lang)
        parameter("blacklistFlags", blacklistFlags)
        parameter("type", type)
    }.body()

    suspend fun getJoke(
        category: String = "Any",
        lang: String? = null,
        blacklistFlags: String? = null,
        type: String? = null,
    ): JokeApiResponse = httpClient.get {
        url("$BASE_URL/joke/$category")

        // Query Parameters
        parameter("lang", lang)
        parameter("blacklistFlags", blacklistFlags)
        parameter("type", type)
    }.body()
}
