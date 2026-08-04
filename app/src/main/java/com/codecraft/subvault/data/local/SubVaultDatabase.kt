package com.codecraft.subvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codecraft.subvault.domain.model.ExchangeRate
import com.codecraft.subvault.domain.model.PriceChangeLog
import com.codecraft.subvault.domain.model.SentNotification
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.model.UserPreferences

@Database(
    entities = [Subscription::class, PriceChangeLog::class, UserPreferences::class, ExchangeRate::class, SentNotification::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SubVaultDatabase : RoomDatabase() {
    abstract val subscriptionDao: SubscriptionDao
    abstract val userPreferencesDao: UserPreferencesDao
    abstract val priceChangeDao: PriceChangeDao
    abstract val exchangeRateDao: ExchangeRateDao
    abstract val sentNotificationDao: SentNotificationDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sent_notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        subscriptionId INTEGER NOT NULL,
                        renewalDate INTEGER NOT NULL,
                        intervalDays INTEGER NOT NULL,
                        sentAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exchange_rates (
                        code TEXT PRIMARY KEY NOT NULL,
                        rate REAL NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE subscriptions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        cycle TEXT NOT NULL,
                        renewalDate INTEGER NOT NULL,
                        endDate INTEGER,
                        paymentMethod TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        iconUrl TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO subscriptions_new (id, name, category, amount, currency, cycle, renewalDate, paymentMethod, isActive, iconUrl, createdAt, updatedAt)
                    SELECT id, name, category, price, currency, cycle, renewalDate, 'Other', 1, '', 0, 0 FROM subscriptions
                """.trimIndent())

                db.execSQL("DROP TABLE subscriptions")
                db.execSQL("ALTER TABLE subscriptions_new RENAME TO subscriptions")

                db.execSQL("""
                    CREATE TABLE user_preferences (
                        id INTEGER PRIMARY KEY NOT NULL,
                        defaultCurrency TEXT NOT NULL DEFAULT 'USD'
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val defaultTheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    "SYSTEM"
                } else {
                    "LIGHT"
                }
                db.execSQL("ALTER TABLE user_preferences ADD COLUMN appTheme TEXT NOT NULL DEFAULT '$defaultTheme'")
            }
        }
    }
}
