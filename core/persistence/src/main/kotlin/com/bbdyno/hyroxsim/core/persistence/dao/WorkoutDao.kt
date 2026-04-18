package com.bbdyno.hyroxsim.core.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bbdyno.hyroxsim.core.persistence.entities.StoredSegmentEntity
import com.bbdyno.hyroxsim.core.persistence.entities.StoredWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkout(workout: StoredWorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSegments(segments: List<StoredSegmentEntity>)

    @Transaction
    suspend fun upsertWorkoutWithSegments(
        workout: StoredWorkoutEntity,
        segments: List<StoredSegmentEntity>,
    ) {
        upsertWorkout(workout)
        upsertSegments(segments)
    }

    @Query("SELECT * FROM workout ORDER BY finishedAtEpochMs DESC")
    fun observeAll(): Flow<List<StoredWorkoutEntity>>

    @Query("SELECT * FROM workout WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): StoredWorkoutEntity?

    @Query("SELECT * FROM segment WHERE workoutId = :workoutId ORDER BY `index` ASC")
    suspend fun segmentsFor(workoutId: String): List<StoredSegmentEntity>

    @Query("DELETE FROM workout WHERE id = :id")
    suspend fun deleteById(id: String)
}
