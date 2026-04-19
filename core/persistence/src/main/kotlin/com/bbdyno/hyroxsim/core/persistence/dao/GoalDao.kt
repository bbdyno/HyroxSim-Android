package com.bbdyno.hyroxsim.core.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bbdyno.hyroxsim.core.persistence.entities.StoredGoalEntity

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: StoredGoalEntity)

    @Query("SELECT * FROM goal WHERE templateId = :templateId LIMIT 1")
    suspend fun findByTemplate(templateId: String): StoredGoalEntity?

    @Query("DELETE FROM goal WHERE templateId = :templateId")
    suspend fun delete(templateId: String)
}
