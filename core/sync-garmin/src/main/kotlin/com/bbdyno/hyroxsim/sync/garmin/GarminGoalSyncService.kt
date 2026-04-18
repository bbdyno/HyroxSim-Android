package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.HyroxDivision

/**
 * Pushes a target time to the Garmin watch just before the workout starts.
 * The watch stores it under `GoalStore.KEY` so the delta badge is live
 * from the first tick.
 */
class GarminGoalSyncService(private val bridge: GarminBridge) {
    fun sendGoal(
        division: HyroxDivision,
        templateName: String,
        targetTotalMs: Long,
        targetSegmentsMs: List<Long>,
    ) {
        bridge.sendEnvelope(
            GarminMessageCodec.encodeGoalSet(
                division = division,
                templateName = templateName,
                targetTotalMs = targetTotalMs,
                targetSegmentsMs = targetSegmentsMs,
            )
        )
    }
}
