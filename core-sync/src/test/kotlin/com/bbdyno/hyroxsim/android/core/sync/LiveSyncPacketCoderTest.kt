//
//  LiveSyncPacketCoderTest.kt
//  core-sync
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.sync

import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LiveSyncPacketCoderTest {
    @Test
    fun `encodes and decodes workout started packet`() {
        val packet: LiveSyncPacket = LiveSyncPacket.WorkoutStarted(
            template = WorkoutTemplate(
                name = "Mirror Test",
                segments = listOf(WorkoutSegment.run()),
            ),
            origin = WorkoutOrigin.WATCH,
        )

        val decoded = LiveSyncPacketCoder.decode(LiveSyncPacketCoder.encode(packet))

        val workoutStarted = assertIs<LiveSyncPacket.WorkoutStarted>(decoded)
        assertEquals("Mirror Test", workoutStarted.template.name)
        assertEquals(WorkoutOrigin.WATCH, workoutStarted.origin)
    }
}
