package com.codecraft.subvault.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class CurrencyResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

interface CurrencyApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") from: String = "USD"
    ): CurrencyResponse

    @GET("{date}")
    suspend fun getHistoricalRates(
        @Path("date") date: String,
        @Query("from") from: String = "USD"
    ): CurrencyResponse

    companion object {
        const val BASE_URL = "https://api.frankfurter.app/"
    }
}
