package com.bbdyno.hyroxsim.sync.garmin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioural contract for the hello/hello.ack/sync.request flow that
 * mirrors iOS `GarminBridge`. Real SDK delivery is exercised on-device.
 */
class StubGarminBridgeHelloAckTest {

    @Test
    fun `sendHello emits hello envelope with phone_os and app_version`() {
        val bridge = StubGarminBridge(paired = true)

        bridge.sendHello(appVersion = "1.2.3")

        assertEquals(1, bridge.capturedEnvelopes.size)
        val env = bridge.capturedEnvelopes.first()
        assertEquals(MessageProtocol.Type.HELLO, env[MessageProtocol.Key.TYPE])
        val payload = env[MessageProtocol.Key.PAYLOAD] as Map<*, *>
        assertEquals("android", payload["phone_os"])
        assertEquals("1.2.3", payload["app_version"])
    }

    @Test
    fun `hello_ack triggers onHelloAck handler`() {
        val bridge = StubGarminBridge(paired = true)
        var fired = 0
        bridge.setOnHelloAck { fired += 1 }

        bridge.simulateMessage(
            mapOf(
                MessageProtocol.Key.TYPE to MessageProtocol.Type.HELLO_ACK,
                MessageProtocol.Key.ID to "x",
            )
        )

        assertEquals(1, fired)
    }

    @Test
    fun `sync_request also triggers onHelloAck handler`() {
        val bridge = StubGarminBridge(paired = true)
        var fired = 0
        bridge.setOnHelloAck { fired += 1 }

        bridge.simulateMessage(
            mapOf(
                MessageProtocol.Key.TYPE to MessageProtocol.Type.SYNC_REQUEST,
                MessageProtocol.Key.ID to "boot",
            )
        )

        assertEquals(1, fired)
    }

    @Test
    fun `unrelated message does not trigger onHelloAck`() {
        val bridge = StubGarminBridge(paired = true)
        var fired = 0
        bridge.setOnHelloAck { fired += 1 }

        bridge.simulateMessage(
            mapOf(
                MessageProtocol.Key.TYPE to MessageProtocol.Type.WORKOUT_COMPLETED,
                MessageProtocol.Key.ID to "wk",
            )
        )

        assertEquals(0, fired)
    }

    @Test
    fun `sendHello is dropped when not paired`() {
        val bridge = StubGarminBridge(paired = false)

        bridge.sendHello(appVersion = "1.2.3")

        assertTrue("envelope must not be captured when unpaired",
            bridge.capturedEnvelopes.isEmpty())
    }
}
