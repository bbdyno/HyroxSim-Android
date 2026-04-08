//
//  LocalWorkoutLibrary.kt
//  data-local
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.local

import android.content.Context
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HyroxPresets
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.Instant
import java.util.UUID

class LocalWorkoutLibrary(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val snapshotFile = File(appContext.filesDir, "hyrox-workout-library.bin")
    private val lock = Any()

    fun allTemplates(): List<WorkoutTemplate> =
        synchronized(lock) {
            val customTemplates = readSnapshot().customTemplates
                .sortedByDescending { it.createdAt }
            HyroxPresets.all + customTemplates
        }

    fun customTemplates(): List<WorkoutTemplate> =
        synchronized(lock) {
            readSnapshot().customTemplates
                .sortedByDescending { it.createdAt }
        }

    fun saveTemplate(template: WorkoutTemplate): WorkoutTemplate =
        synchronized(lock) {
            val snapshot = readSnapshot()
            val nextTemplates = snapshot.customTemplates
                .filterNot { it.id == template.id }
                .plus(template.copy(isBuiltIn = false))
                .sortedByDescending { it.createdAt }
            writeSnapshot(snapshot.copy(customTemplates = nextTemplates))
            template
        }

    fun deleteTemplate(id: UUID): Boolean =
        synchronized(lock) {
            val snapshot = readSnapshot()
            val nextTemplates = snapshot.customTemplates.filterNot { it.id == id }
            if (nextTemplates.size == snapshot.customTemplates.size) {
                return false
            }
            writeSnapshot(snapshot.copy(customTemplates = nextTemplates))
            true
        }

    fun completedWorkouts(): List<CompletedWorkout> =
        synchronized(lock) {
            readSnapshot().completedWorkouts
                .sortedByDescending { it.startedAt }
        }

    fun saveCompletedWorkout(workout: CompletedWorkout): CompletedWorkout =
        synchronized(lock) {
            val snapshot = readSnapshot()
            val nextHistory = snapshot.completedWorkouts
                .filterNot { it.id == workout.id }
                .plus(workout)
                .sortedByDescending { it.startedAt }
            writeSnapshot(snapshot.copy(completedWorkouts = nextHistory))
            workout
        }

    fun seedIfEmpty() {
        synchronized(lock) {
            if (snapshotFile.exists()) return
            writeSnapshot(LibrarySnapshot())
        }
    }

    private fun readSnapshot(): LibrarySnapshot {
        if (!snapshotFile.exists()) {
            return LibrarySnapshot()
        }
        return runCatching {
            ObjectInputStream(snapshotFile.inputStream().buffered()).use { stream ->
                stream.readObject() as? LibrarySnapshot
            }
        }.getOrNull() ?: LibrarySnapshot()
    }

    private fun writeSnapshot(snapshot: LibrarySnapshot) {
        snapshotFile.parentFile?.mkdirs()
        ObjectOutputStream(snapshotFile.outputStream().buffered()).use { stream ->
            stream.writeObject(snapshot)
        }
    }
}

private data class LibrarySnapshot(
    val customTemplates: List<WorkoutTemplate> = emptyList(),
    val completedWorkouts: List<CompletedWorkout> = emptyList(),
    val savedAt: Instant = Instant.now(),
) : Serializable
