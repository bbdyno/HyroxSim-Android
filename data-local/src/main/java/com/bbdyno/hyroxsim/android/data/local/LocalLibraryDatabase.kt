//
//  LocalLibraryDatabase.kt
//  data-local
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "custom_templates")
data class StoredTemplateEntity(
    @PrimaryKey val id: String,
    val createdAtEpochMillis: Long,
    val payload: ByteArray,
)

@Entity(tableName = "completed_workouts")
data class StoredWorkoutEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val payload: ByteArray,
)

@Dao
interface TemplateDao {
    @Query("SELECT * FROM custom_templates ORDER BY createdAtEpochMillis DESC")
    fun all(): List<StoredTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: StoredTemplateEntity)

    @Query("DELETE FROM custom_templates WHERE id = :id")
    fun deleteById(id: String): Int
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM completed_workouts ORDER BY startedAtEpochMillis DESC")
    fun all(): List<StoredWorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: StoredWorkoutEntity)
}

@Database(
    entities = [
        StoredTemplateEntity::class,
        StoredWorkoutEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class LocalLibraryDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao
    abstract fun workoutDao(): WorkoutDao
}
