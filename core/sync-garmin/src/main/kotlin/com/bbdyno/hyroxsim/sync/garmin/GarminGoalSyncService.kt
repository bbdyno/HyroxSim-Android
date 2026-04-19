package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.HyroxDivision

/**
 * Pushes a target time to the Garmin watch just before the workout starts.
 * The watch stores it under `GoalStore.KEY` so the delta badge is live
 * from the first tick.
 *
 * All methods return a [SyncResult] so the caller can surface a clear
 * "watch not paired" hint instead of silently dropping the message.
 */
class GarminGoalSyncService(private val bridge: GarminBridge) {

    enum class SyncResult {
        /** Sent successfully. */
        Sent,
        /** No watch is paired — skipped silently. */
        NotPaired,
        /** Sending threw or the SDK rejected delivery. */
        Failed,
    }

    fun sendGoal(
        division: HyroxDivision,
        templateName: String,
        targetTotalMs: Long,
        targetSegmentsMs: List<Long>,
    ): SyncResult {
        if (!bridge.isPaired) return SyncResult.NotPaired
        val ok = bridge.sendEnvelope(
            GarminMessageCodec.encodeGoalSet(
                division = division,
                templateName = templateName,
                targetTotalMs = targetTotalMs,
                targetSegmentsMs = targetSegmentsMs,
            )
        )
        return if (ok) SyncResult.Sent else SyncResult.Failed
    }
}
