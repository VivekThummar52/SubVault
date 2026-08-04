package com.codecraft.subvault.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codecraft.subvault.data.local.ExchangeRateDao
import com.codecraft.subvault.data.remote.CurrencyApi
import com.codecraft.subvault.domain.model.ExchangeRate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CurrencySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val currencyApi: CurrencyApi,
    private val exchangeRateDao: ExchangeRateDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CurrencySyncWorker", "Starting currency sync...")
        return try {
            val response = currencyApi.getLatestRates()
            Log.d("CurrencySyncWorker", "Received rates: ${response.rates} items")
            val exchangeRates = response.rates.map { (code, rate) ->
                ExchangeRate(code = code, rate = rate)
            } + ExchangeRate("USD", 1.0) // Add base USD as well

            exchangeRateDao.insertRates(exchangeRates)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
