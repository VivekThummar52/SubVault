package com.codecraft.subvault.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder for Firebase Remote Config integration.
 * Will be used in Phase 3 to fetch dynamic configuration like icon URLs or feature flags.
 */
@Singleton
class RemoteConfigProvider @Inject constructor() {
    
    /**
     * Placeholder for fetching a remote icon URL for a given subscription name.
     */
    fun getIconUrlForSubscription(name: String): String {
        // In the future, this would fetch from Firebase Remote Config or a CDN mapping
        return ""
    }

    /**
     * Example feature flag for enabling/disabling cloud sync.
     */
    fun isCloudSyncEnabled(): Boolean {
        return false // Default to false until cloud integration is complete
    }
}
