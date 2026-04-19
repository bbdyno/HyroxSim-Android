package com.bbdyno.hyroxsim.core.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bbdyno.hyroxsim.core.persistence.dao.GoalDao
import com.bbdyno.hyroxsim.core.persistence.dao.TemplateDao
import com.bbdyno.hyroxsim.core.persistence.dao.WorkoutDao
import com.bbdyno.hyroxsim.core.persistence.entities.StoredGoalEntity
import com.bbdyno.hyroxsim.core.persistence.entities.StoredSegmentEntity
import com.bbdyno.hyroxsim.core.persistence.entities.StoredTemplateEntity
import com.bbdyno.hyroxsim.core.persistence.entities.StoredWorkoutEntity

@Database(
    entities = [
        StoredWorkoutEntity::class,
        StoredSegmentEntity::class,
        StoredTemplateEntity::class,
        StoredGoalEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class HyroxDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun templateDao(): TemplateDao
    abstract fun goalDao(): GoalDao

    companion object {
        fun create(context: Context): HyroxDatabase = Room.databaseBuilder(
            context.applicationContext,
            HyroxDatabase::class.java,
            "hyroxsim.db",
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
