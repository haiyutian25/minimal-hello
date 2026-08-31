package com.example.core.data.datasource

import com.example.core.data.model.RemoteHeroQuote
import com.example.core.network.GreetingApi
import javax.inject.Inject

/**
 * Remote data source for greeting content; the single bridge to [GreetingApi].
 * Maps network DTOs into domain models so the network layer stays encapsulated.
 */
class GreetingRemoteDataSource @Inject constructor(
    private val greetingApi: GreetingApi,
) {
    suspend fun fetchHeroQuotes(): List<RemoteHeroQuote> =
        greetingApi.getHeroQuotes().map { dto ->
            RemoteHeroQuote(part1 = dto.part1, part2 = dto.part2)
        }
}
