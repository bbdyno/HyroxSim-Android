package com.bbdyno.hyroxsim.core.domain

import org.junit.Assert.*
import org.junit.Test

class WorkoutEngineTest {

    private fun tinyTemplate(): WorkoutTemplate = WorkoutTemplate(
        name = "Tiny",
        division = HyroxDivision.MenOpenSingle,
        segments = listOf(
            WorkoutSegment.run(1000.0),
            WorkoutSegment.station(StationKind.SkiErg),
            WorkoutSegment.run(1000.0),
        ),
        usesRoxZone = false,
    )

    @Test
    fun startAdvanceFinish_records3segments_andReportsTotalElapsed() {
        val e = WorkoutEngine(tinyTemplate())
        e.start(1_000)
        assertEquals("running", e.state.label)

        e.advance(6_000)
        assertEquals(1, e.records.size)
        assertEquals(1, e.currentSegmentIndex)

        e.advance(11_000)
        e.advance(16_000)
        assertTrue(e.isFinished)
        assertEquals(3, e.records.size)
        assertEquals(15_000L, e.totalElapsedMs(99_999))
    }

    @Test
    fun pauseResume_excludesPausedDuration() {
        val e = WorkoutEngine(tinyTemplate())
        e.start(0)
        e.pause(3_000)
        e.resume(8_000)    // 5s paused
        assertEquals(5_000L, e.segmentElapsedMs(10_000))
        assertEquals(5_000L, e.totalElapsedMs(10_000))
    }

    @Test(expected = EngineError.InvalidTransition::class)
    fun invalidTransition_advanceFromIdle_throws() {
        WorkoutEngine(tinyTemplate()).advance(0)
    }

    @Test
    fun undo_restoresPreviousIndex_andDropsLastRecord() {
        val e = WorkoutEngine(tinyTemplate())
        e.start(0)
        e.advance(1_000)
        assertEquals(1, e.currentSegmentIndex)
        e.undo(2_000)
        assertEquals(0, e.currentSegmentIndex)
        assertEquals(0, e.records.size)
    }

    @Test
    fun finishFromPaused_usesActiveTime() {
        val e = WorkoutEngine(tinyTemplate())
        e.start(0)
        e.pause(5_000)
        e.finish(7_000)
        assertTrue(e.isFinished)
        assertEquals(5_000L, e.records[0].activeDurationMs)
    }

    @Test
    fun hyroxPreset_produces31SegmentsWithCorrectBreakdown() {
        val t = WorkoutTemplate.hyroxPreset(HyroxDivision.MenOpenSingle)
        assertEquals(31, t.segments.size)
        assertEquals(8, t.segments.count { it.type == SegmentType.Run })
        assertEquals(8, t.segments.count { it.type == SegmentType.Station })
        assertEquals(15, t.segments.count { it.type == SegmentType.RoxZone })
        assertEquals(SegmentType.Run, t.segments.first().type)
        assertEquals(SegmentType.Station, t.segments.last().type)
    }

    @Test
    fun divisionSpec_hasExpectedWeights() {
        val mensPro = HyroxDivisionSpec.stationsFor(HyroxDivision.MenProSingle)
        assertEquals(202.0, mensPro[1].weightKg!!, 0.01)
        val womensOpen = HyroxDivisionSpec.stationsFor(HyroxDivision.WomenOpenSingle)
        val wb = womensOpen[7]
        assertEquals(4.0, wb.weightKg!!, 0.01)
        assertEquals(StationTarget.Reps(75), wb.target)
    }
}
