//
//  WorkoutEngineTest.kt
//  core-engine
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.engine

import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkoutEngineTest {
    @Test
    fun `advance finishes final segment`() {
        val template = WorkoutTemplate(
            name = "Test",
            segments = listOf(
                WorkoutSegment.run(),
                WorkoutSegment.roxZone(),
            ),
        )
        val engine = WorkoutEngine(template)
        val start = Instant.parse("2026-04-08T00:00:00Z")

        engine.start(start)
        engine.advance(start.plusSeconds(60))
        engine.advance(start.plusSeconds(90))

        assertTrue(engine.isFinished)
        assertEquals(2, engine.records.size)
    }
}
