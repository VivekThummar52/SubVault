package com.codecraft.subvault.data.local

import androidx.room.TypeConverter
import com.codecraft.subvault.ui.theme.AppTheme

class Converters {
    @TypeConverter
    fun fromAppTheme(value: AppTheme): String {
        return value.name
    }

    @TypeConverter
    fun toAppTheme(value: String): AppTheme {
        return try {
            val theme = AppTheme.valueOf(value)
            if (theme == AppTheme.SYSTEM && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                AppTheme.LIGHT
            } else {
                theme
            }
        } catch (e: Exception) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                AppTheme.SYSTEM
            } else {
                AppTheme.LIGHT
            }
        }
    }
}
