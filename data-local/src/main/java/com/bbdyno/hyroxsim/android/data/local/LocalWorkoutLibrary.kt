//
//  LocalWorkoutLibrary.kt
//  data-local
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HyroxDivision
import com.bbdyno.hyroxsim.android.core.model.HyroxPresets
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LocalWorkoutLibrary(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val database = Room.databaseBuilder(
        appContext,
        LocalLibraryDatabase::class.java,
        "hyrox-local-library.db",
    )
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()
    private val preferences = PreferenceDataStoreFactory.create(
        corruptionHandler = null,
        migrations = emptyList(),
        produceFile = { appContext.preferencesDataStoreFile("hyrox-local-preferences.preferences_pb") },
    )
    private val legacySnapshotFile = File(appContext.filesDir, "hyrox-workout-library.bin")
    private val lock = Any()

    fun allTemplates(): List<WorkoutTemplate> =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            HyroxPresets.all + customTemplatesInternal()
        }

    fun customTemplates(): List<WorkoutTemplate> =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            customTemplatesInternal()
        }

    fun saveTemplate(template: WorkoutTemplate): WorkoutTemplate =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            val normalized = template.copy(isBuiltIn = false)
            database.templateDao().upsert(
                StoredTemplateEntity(
                    id = normalized.id.toString(),
                    createdAtEpochMillis = normalized.createdAt.toEpochMilli(),
                    payload = BinaryArchive.encode(normalized),
                ),
            )
            normalized.division?.let(::setLastSelectedDivision)
            normalized
        }

    fun deleteTemplate(id: UUID): Boolean =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            database.templateDao().deleteById(id.toString()) > 0
        }

    fun completedWorkouts(): List<CompletedWorkout> =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            completedWorkoutsInternal()
        }

    fun saveCompletedWorkout(workout: CompletedWorkout): CompletedWorkout =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            database.workoutDao().upsert(
                StoredWorkoutEntity(
                    id = workout.id.toString(),
                    startedAtEpochMillis = workout.startedAt.toEpochMilli(),
                    finishedAtEpochMillis = workout.finishedAt.toEpochMilli(),
                    payload = BinaryArchive.encode(workout),
                ),
            )
            workout
        }

    fun seedIfEmpty() {
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
        }
    }

    fun lastSelectedDivision(): HyroxDivision? =
        synchronized(lock) {
            migrateLegacySnapshotIfNeeded()
            runBlocking {
                preferences.data.first()[PreferenceKeys.lastSelectedDivision]
                    ?.let { name -> HyroxDivision.entries.firstOrNull { it.name == name } }
            }
        }

    fun setLastSelectedDivision(division: HyroxDivision?) {
        synchronized(lock) {
            runBlocking {
                preferences.edit { values ->
                    if (division == null) {
                        values.remove(PreferenceKeys.lastSelectedDivision)
                    } else {
                        values[PreferenceKeys.lastSelectedDivision] = division.name
                    }
                }
            }
        }
    }

    private fun customTemplatesInternal(): List<WorkoutTemplate> =
        database.templateDao()
            .all()
            .mapNotNull { BinaryArchive.decode<WorkoutTemplate>(it.payload) }
            .sortedByDescending { it.createdAt }

    private fun completedWorkoutsInternal(): List<CompletedWorkout> =
        database.workoutDao()
            .all()
            .mapNotNull { BinaryArchive.decode<CompletedWorkout>(it.payload) }
            .sortedByDescending { it.startedAt }

    private fun migrateLegacySnapshotIfNeeded() {
        if (isMigrationComplete()) return

        val legacySnapshot = readLegacySnapshot()
        if (legacySnapshot != null) {
            database.runInTransaction {
                legacySnapshot.customTemplates
                    .sortedByDescending { it.createdAt }
                    .forEach { template ->
                        database.templateDao().upsert(
                            StoredTemplateEntity(
                                id = template.id.toString(),
                                createdAtEpochMillis = template.createdAt.toEpochMilli(),
                                payload = BinaryArchive.encode(template.copy(isBuiltIn = false)),
                            ),
                        )
                    }
                legacySnapshot.completedWorkouts
                    .sortedByDescending { it.startedAt }
                    .forEach { workout ->
                        database.workoutDao().upsert(
                            StoredWorkoutEntity(
                                id = workout.id.toString(),
                                startedAtEpochMillis = workout.startedAt.toEpochMilli(),
                                finishedAtEpochMillis = workout.finishedAt.toEpochMilli(),
                                payload = BinaryArchive.encode(workout),
                            ),
                        )
                    }
            }
            legacySnapshotFile.delete()
        }

        runBlocking {
            preferences.edit { values ->
                values[PreferenceKeys.legacyMigrationComplete] = true
            }
        }
    }

    private fun isMigrationComplete(): Boolean =
        runBlocking {
            preferences.safeData().first()[PreferenceKeys.legacyMigrationComplete] == true
        }

    private fun readLegacySnapshot(): LibrarySnapshot? {
        if (!legacySnapshotFile.exists()) {
            return null
        }
        return runCatching {
            ObjectInputStream(legacySnapshotFile.inputStream().buffered()).use { stream ->
                stream.readObject() as? LibrarySnapshot
            }
        }.getOrNull()
    }
}

private object PreferenceKeys {
    val legacyMigrationComplete: Preferences.Key<Boolean> =
        booleanPreferencesKey("legacy_migration_complete")
    val lastSelectedDivision: Preferences.Key<String> =
        stringPreferencesKey("last_selected_division")
}

private fun DataStore<Preferences>.safeData() =
    data.catch { emit(emptyPreferences()) }

private object BinaryArchive {
    fun encode(value: Serializable): ByteArray =
        ByteArrayOutputStream().use { output ->
            ObjectOutputStream(output).use { stream ->
                stream.writeObject(value)
            }
            output.toByteArray()
        }

    inline fun <reified T> decode(payload: ByteArray): T? =
        runCatching {
            ObjectInputStream(ByteArrayInputStream(payload)).use { stream ->
                stream.readObject() as? T
            }
        }.getOrNull()
}

private data class LibrarySnapshot(
    val customTemplates: List<WorkoutTemplate> = emptyList(),
    val completedWorkouts: List<CompletedWorkout> = emptyList(),
    val savedAt: Instant = Instant.now(),
) : Serializable
