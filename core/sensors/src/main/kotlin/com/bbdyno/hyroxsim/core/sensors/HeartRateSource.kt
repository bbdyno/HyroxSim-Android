package com.bbdyno.hyroxsim.core.sensors

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.bbdyno.hyroxsim.core.domain.HeartRateSample
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Reads recent heart-rate samples from Health Connect. Intended to be
 * polled every few seconds during an active workout — HC doesn't provide
 * streaming callbacks, so callers pull on a timer.
 *
 * On devices without Health Connect installed, [client] is null and
 * [latestSample] returns null.
 */
class HeartRateSource(private val context: Context) {

    private val client: HealthConnectClient? by lazy {
        runCatching {
            if (HealthConnectClient.getSdkStatus(context) ==
                HealthConnectClient.SDK_AVAILABLE
            ) {
                HealthConnectClient.getOrCreate(context)
            } else null
        }.getOrNull()
    }

    val permissions = setOf(HealthPermission.getReadPermission(HeartRateRecord::class))

    suspend fun hasPermissions(): Boolean {
        val c = client ?: return false
        return c.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    /**
     * Returns the most recent HR sample within the last [windowSeconds]
     * seconds, or null if none/unavailable.
     */
    suspend fun latestSample(windowSeconds: Long = 30): HeartRateSample? {
        val c = client ?: return null
        if (!hasPermissions()) return null
        val end = Instant.now()
        val start = end.minus(windowSeconds, ChronoUnit.SECONDS)
        val resp = runCatching {
            c.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            )
        }.getOrNull() ?: return null

        val last = resp.records.lastOrNull() ?: return null
        val lastSample = last.samples.lastOrNull() ?: return null
        return HeartRateSample(
            timestampEpochMs = lastSample.time.toEpochMilli(),
            bpm = lastSample.beatsPerMinute.toInt(),
        )
    }
}
