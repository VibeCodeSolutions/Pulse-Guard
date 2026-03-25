// com.example.pulseguard.data.local.entity.Reminder
package com.example.pulseguard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pulseguard.domain.model.ReminderType
import java.time.DayOfWeek

/**
 * Room entity representing a recurring reminder for medication intake
 * or blood pressure measurement.
 *
 * [days] is stored as a comma-separated string of [DayOfWeek] ordinal
 * values (1 = MONDAY … 7 = SUNDAY). Use the [daysSet] extension to get
 * a typed `Set<DayOfWeek>` and [encodeDays] to convert back.
 *
 * @property id        Auto-generated primary key.
 * @property label     User-visible label, e.g. "Blutdruckmessung morgens".
 * @property type      Whether this is a [ReminderType.MEDICATION] or
 *                     [ReminderType.MEASUREMENT] reminder.
 * @property hour      Hour of day (0–23) when the reminder should fire.
 * @property minute    Minute of hour (0–59) when the reminder should fire.
 * @property days      Comma-separated [DayOfWeek] ordinal values.
 * @property isEnabled Whether this reminder is currently active.
 * @property createdAt Epoch millis when this reminder was first created.
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "type")
    val type: ReminderType,

    @ColumnInfo(name = "hour")
    val hour: Int,

    @ColumnInfo(name = "minute")
    val minute: Int,

    @ColumnInfo(name = "days")
    val days: String,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

/** Returns the stored days as a typed [Set] of [DayOfWeek]. */
val Reminder.daysSet: Set<DayOfWeek>
    get() {
        if (days.isBlank()) return emptySet()
        return days.split(",").map { DayOfWeek.of(it.trim().toInt()) }.toSet()
    }

/** Encodes a [Set] of [DayOfWeek] into the comma-separated storage format. */
fun encodeDays(daySet: Set<DayOfWeek>): String =
    daySet.joinToString(",") { it.value.toString() }
