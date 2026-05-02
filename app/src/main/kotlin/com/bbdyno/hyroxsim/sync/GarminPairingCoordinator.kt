package com.bbdyno.hyroxsim.sync

import com.bbdyno.hyroxsim.core.persistence.repository.GoalRepository
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import com.bbdyno.hyroxsim.sync.garmin.GarminBridge
import com.bbdyno.hyroxsim.sync.garmin.GarminGoalSyncService
import com.bbdyno.hyroxsim.sync.garmin.GarminTemplateSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Mirrors iOS `AppServices.wireGarminPostPairingResync`.
 *
 * `hello.ack` (or the watch's unsolicited `sync.request` boot ping) is the
 * only confirmation that the watch app is open and has accepted pairing.
 * CIQ does not buffer messages for an offline watch app, so any
 * `template.upsert` / `goal.set` sent before the watch app was opened was
 * silently dropped. We re-push everything here so state created
 * pre-pairing finally lands.
 *
 * Built-in HYROX presets deliberately skip `template.upsert` — the watch
 * generates the preset structure itself, and re-uploading would clutter
 * its MY WORKOUTS list. We push only the goal for those.
 */
class GarminPairingCoordinator(
    private val bridge: GarminBridge,
    private val templateRepo: TemplateRepository,
    private val goalRepo: GoalRepository,
    private val templateSync: GarminTemplateSyncService,
    private val goalSync: GarminGoalSyncService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {
    fun start() {
        bridge.setOnHelloAck {
            scope.launch { resync() }
        }
    }

    private suspend fun resync() {
        val templates = templateRepo.observeAll().first()
        templateSync.pushAll(templates.filter { !it.isBuiltIn })
        for (template in templates) {
            val goal = goalRepo.find(template.id) ?: continue
            val division = template.division ?: continue
            goalSync.sendGoal(
                division = division,
                templateName = template.name,
                targetTotalMs = goal.targetTotalMs,
                targetSegmentsMs = goal.targetSegmentsMs ?: emptyList(),
            )
        }
    }
}
