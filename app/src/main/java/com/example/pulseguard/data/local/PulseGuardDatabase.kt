// com.example.pulseguard.data.local.PulseGuardDatabase
package com.example.pulseguard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pulseguard.data.local.dao.BloodPressureDao
import com.example.pulseguard.data.local.dao.ReminderDao
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.Reminder

/**
 * Central Room database for Pulse Guard.
 *
 * **Version history**
 * - `v1`: Initial schema — `blood_pressure_entries` table.
 * - `v2`: Added `reminders` table for notification scheduling.
 *
 * `exportSchema = true` writes a JSON schema file to `app/schemas/` on every build,
 * enabling diff-based migration tracking (see `ksp { arg("room.schemaLocation", …) }`
 * in `app/build.gradle.kts`).
 */
@Database(
    entities = [BloodPressureEntry::class, Reminder::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PulseGuardDatabase : RoomDatabase() {

    /** Returns the DAO for blood pressure measurement entries. */
    abstract fun bloodPressureDao(): BloodPressureDao

    /** Returns the DAO for reminder entities. */
    abstract fun reminderDao(): ReminderDao
}

/**
 * Migration from v1 to v2: creates the `reminders` table.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `label` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `hour` INTEGER NOT NULL,
                `minute` INTEGER NOT NULL,
                `days` TEXT NOT NULL,
                `is_enabled` INTEGER NOT NULL DEFAULT 1,
                `created_at` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}
