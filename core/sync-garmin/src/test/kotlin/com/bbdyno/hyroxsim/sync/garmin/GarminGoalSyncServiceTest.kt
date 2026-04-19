package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GarminGoalSyncServiceTest {

    @Test
    fun `returns NotPaired when no watch connected`() {
        val bridge = StubGarminBridge(paired = false)
        val svc = GarminGoalSyncService(bridge)

        val result = svc.sendGoal(
            division = HyroxDivision.MenOpenSingle,
            templateName = "Test",
            targetTotalMs = 75L * 60 * 1000,
            targetSegmentsMs = List(31) { 150_000L },
        )

        assertEquals(GarminGoalSyncService.SyncResult.NotPaired, result)
        assertTrue("no envelopes captured when unpaired", bridge.capturedEnvelopes.isEmpty())
    }

    @Test
    fun `sends envelope when paired and reports Sent`() {
        val bridge = StubGarminBridge(paired = true)
        val svc = GarminGoalSyncService(bridge)

        val result = svc.sendGoal(
            division = HyroxDivision.MenOpenSingle,
            templateName = "Preset 75m",
            targetTotalMs = 75L * 60 * 1000,
            targetSegmentsMs = List(31) { 150_000L },
        )

        assertEquals(GarminGoalSyncService.SyncResult.Sent, result)
        assertEquals(1, bridge.capturedEnvelopes.size)
        val envelope = bridge.capturedEnvelopes.first()
        assertEquals(MessageProtocol.Type.GOAL_SET, envelope[MessageProtocol.Key.TYPE])
        val payload = envelope[MessageProtocol.Key.PAYLOAD] as Map<*, *>
        assertEquals("menOpenSingle", payload["division"])
        assertEquals("Preset 75m", payload["templateName"])
        assertEquals(75L * 60 * 1000, payload["targetTotalMs"])
    }
}
