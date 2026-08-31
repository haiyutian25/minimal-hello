package com.example.core.network

import com.example.core.network.model.HeroQuoteDto
import retrofit2.http.GET

/**
 * Retrofit service contract for remote greeting content. The full
 * Retrofit/OkHttp stack is wired via Hilt in [com.example.core.network.di.NetworkModule].
 */
interface GreetingApi {

    /** Fetches the curated hero quotes. */
    @GET("greetings")
    suspend fun getHeroQuotes(): List<HeroQuoteDto>
}
