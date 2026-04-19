package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GarminImportServiceTest {

    @Test
    fun `decodes workout completed envelope and persists via onImport`() = runBlocking {
        val bridge = StubGarminBridge(paired = true)
        var imported: CompletedWorkout? = null
        val service = GarminImportService(
            bridge = bridge,
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            onImport = { imported = it },
        )
        service.start()

        val envelope = mapOf(
            MessageProtocol.Key.VERSION to MessageProtocol.VERSION,
            MessageProtocol.Key.TYPE to MessageProtocol.Type.WORKOUT_COMPLETED,
            MessageProtocol.Key.ID to "wk-1",
            MessageProtocol.Key.PAYLOAD to mapOf(
                "id" to "wk-1",
                "templateName" to "Hyrox Men's Open",
                "division" to "menOpenSingle",
                "startedAtMs" to 1_000L,
                "finishedAtMs" to 5_000L,
                "source" to "garmin",
                "segments" to listOf(
                    mapOf(
                        "index" to 0,
                        "type" to "run",
                        "startedAtMs" to 1_000L,
                        "endedAtMs" to 2_000L,
                        "pausedDurationMs" to 0L,
                    ),
                ),
            ),
        )

        bridge.simulateMessage(envelope)

        assertNotNull("expected imported workout", imported)
        assertEquals("wk-1", imported?.id)
        assertEquals("Hyrox Men's Open", imported?.templateName)
        assertEquals(1, imported?.segments?.size)

        // ACK should be echoed back to the bridge.
        val ack = bridge.capturedEnvelopes.firstOrNull { it[MessageProtocol.Key.TYPE] == MessageProtocol.Type.ACK }
        assertNotNull("expected ack envelope", ack)
        assertEquals("wk-1", ack?.get(MessageProtocol.Key.ID))
    }

    @Test
    fun `ignores non-workout envelopes`() = runBlocking {
        val bridge = StubGarminBridge(paired = true)
        var imported: CompletedWorkout? = null
        val service = GarminImportService(
            bridge = bridge,
            scope = CoroutineScope(Dispatchers.Unconfined),
            onImport = { imported = it },
        )
        service.start()

        bridge.simulateMessage(
            mapOf(
                MessageProtocol.Key.TYPE to MessageProtocol.Type.HELLO,
                MessageProtocol.Key.ID to "x",
            )
        )

        assertNull(imported)
        assertTrue("no ack for hello", bridge.capturedEnvelopes.isEmpty())
    }
}
