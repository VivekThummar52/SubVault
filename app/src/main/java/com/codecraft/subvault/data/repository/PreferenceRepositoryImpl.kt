package com.codecraft.subvault.data.repository

import com.codecraft.subvault.data.local.UserPreferencesDao
import com.codecraft.subvault.domain.model.UserPreferences
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val dao: UserPreferencesDao
) : PreferenceRepository {
    override fun getUserPreferences(): Flow<UserPreferences> {
        return dao.getUserPreferences().map { it ?: UserPreferences() }
    }

    override suspend fun saveDefaultCurrency(currency: String) {
        val currentPrefs = dao.getUserPreferences().first() ?: UserPreferences()
        dao.saveUserPreferences(currentPrefs.copy(defaultCurrency = currency))
    }

    override suspend fun saveAppTheme(theme: AppTheme) {
        val currentPrefs = dao.getUserPreferences().first() ?: UserPreferences()
        dao.saveUserPreferences(currentPrefs.copy(appTheme = theme))
    }
}
