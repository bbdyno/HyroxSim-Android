package com.bbdyno.hyroxsim.core.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bbdyno.hyroxsim.core.persistence.entities.StoredTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: StoredTemplateEntity)

    @Query("SELECT * FROM template ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<StoredTemplateEntity>>

    @Query("SELECT * FROM template WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): StoredTemplateEntity?

    @Query("DELETE FROM template WHERE id = :id")
    suspend fun deleteById(id: String)
}
