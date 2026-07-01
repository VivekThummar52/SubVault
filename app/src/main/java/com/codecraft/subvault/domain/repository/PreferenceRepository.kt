package com.codecraft.subvault.domain.repository

import com.codecraft.subvault.domain.model.UserPreferences
import com.codecraft.subvault.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveDefaultCurrency(currency: String)
    suspend fun saveAppTheme(theme: AppTheme)
}
