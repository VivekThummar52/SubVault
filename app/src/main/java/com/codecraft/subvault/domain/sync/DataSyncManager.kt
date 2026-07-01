package com.codecraft.subvault.domain.sync

import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing data synchronization with cloud services.
 * Currently a placeholder for Phase 3: Future Online Readiness.
 */
interface DataSyncManager {
    /**
     * Triggers a manual synchronization of local data to the cloud.
     */
    suspend fun syncData()

    /**
     * Returns a flow indicating the current sync status.
     */
    fun getSyncStatus(): Flow<SyncStatus>
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
