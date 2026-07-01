package com.codecraft.subvault.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codecraft.subvault.ui.theme.AppTheme

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 0, // Singleton pattern in DB
    val defaultCurrency: String = "USD",
    val appTheme: AppTheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        AppTheme.SYSTEM
    } else {
        AppTheme.LIGHT
    }
)
