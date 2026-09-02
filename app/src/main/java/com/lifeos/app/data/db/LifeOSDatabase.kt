package com.lifeos.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.lifeos.app.data.dao.*
import com.lifeos.app.data.entity.*

class Converters {
    @TypeConverter fun fromTaskStatus(v: TaskStatus) = v.name
    @TypeConverter fun toTaskStatus(v: String) = TaskStatus.valueOf(v)

    @TypeConverter fun fromPriority(v: Priority) = v.name
    @TypeConverter fun toPriority(v: String) = Priority.valueOf(v)

    @TypeConverter fun fromFrequency(v: Frequency) = v.name
    @TypeConverter fun toFrequency(v: String) = Frequency.valueOf(v)

    @TypeConverter fun fromInstanceStatus(v: InstanceStatus) = v.name
    @TypeConverter fun toInstanceStatus(v: String) = InstanceStatus.valueOf(v)

    @TypeConverter fun fromReviewPeriod(v: ReviewPeriod) = v.name
    @TypeConverter fun toReviewPeriod(v: String) = ReviewPeriod.valueOf(v)

    @TypeConverter fun fromNotificationType(v: NotificationType) = v.name
    @TypeConverter fun toNotificationType(v: String) = NotificationType.valueOf(v)
}

@Database(
    entities = [
        Task::class,
        Objective::class,
        ObjectiveInstance::class,
        JournalEntry::class,
        DailyReview::class,
        PeriodicReview::class,
        NotificationSchedule::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeOSDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun objectiveDao(): ObjectiveDao
    abstract fun objectiveInstanceDao(): ObjectiveInstanceDao
    abstract fun journalDao(): JournalDao
    abstract fun dailyReviewDao(): DailyReviewDao
    abstract fun periodicReviewDao(): PeriodicReviewDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var instance: LifeOSDatabase? = null

        fun get(context: Context): LifeOSDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LifeOSDatabase::class.java,
                    "lifeos.db"
                )
                    // Pre-release app, no shipped migrations yet — reset on schema bump
                    // rather than maintaining Migration objects for an unreleased schema.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
