package com.codecraft.subvault.data.local

import androidx.room.*
import com.codecraft.subvault.domain.model.PriceChangeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceChangeDao {
    @Query("SELECT * FROM price_history WHERE subscriptionId = :subscriptionId ORDER BY date DESC")
    fun getPriceHistory(subscriptionId: Long): Flow<List<PriceChangeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceLog(log: PriceChangeLog)
}
