package com.bbdyno.hyroxsim.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Mirrors iOS `PacePlannerTests.swift` — same bundled JSON, same assertions,
 * guarantees cross-platform parity.
 */
class PacePlannerDataTest {

    private lateinit var planner: PacePlanner

    @Before
    fun setUp() {
        planner = PacePlanner.loadBundled()
    }

    @Test
    fun loadData() {
        assertEquals(3, planner.data.schemaVersion)
        assertEquals(5, planner.data.bucketSizeMin)
    }

    @Test
    fun allDivisionsPresent() {
        for (division in HyroxDivision.entries) {
            assertNotNull(
                "${division.raw} missing",
                planner.data.divisions[division.raw],
            )
        }
    }

    @Test
    fun bucketsAreSorted() {
        for ((key, div) in planner.data.divisions) {
            for (i in 0 until div.buckets.size - 1) {
                assertTrue(
                    "$key buckets not sorted",
                    div.buckets[i].loMin < div.buckets[i + 1].loMin,
                )
            }
        }
    }

    @Test
    fun bucketsHaveValidPctRange() {
        for ((key, div) in planner.data.divisions) {
            for (b in div.buckets) {
                assertTrue("$key pct_range inverted", b.pctRange[0] <= b.pctRange[1])
                assertTrue("$key negative pct", b.pctRange[0] >= 0)
                assertTrue("$key pct > 100", b.pctRange[1] <= 100.1)
            }
        }
    }

    @Test
    fun runRatioTable() {
        assertEquals(6, planner.data.runRatioTable.size)
        for (row in planner.data.runRatioTable) {
            assertEquals(8, row.r.size)
            assertEquals(1.0, row.r[0], 0.0)
            assertEquals(1.0, row.r[1], 0.0)
            for (i in 2..7) {
                assertTrue("ratio r[$i] < 1.0", row.r[i] >= 1.0)
            }
        }
    }
}

class PacePlannerLogicTest {

    private lateinit var planner: PacePlanner

    @Before
    fun setUp() {
        planner = PacePlanner.loadBundled()
    }

    // MARK: - Interpolation

    @Test
    fun interpolateMenOpenMiddle() {
        val r = planner.interpolate(87.0, HyroxDivision.MenOpenSingle)
        assertNotNull(r)
        assertTrue("pct > 30", r!!.percentile > 30)
        assertTrue("pct < 70", r.percentile < 70)
        assertEquals(8, r.stations.size)
    }

    @Test
    fun interpolateFastTime() {
        val r = planner.interpolate(58.0, HyroxDivision.MenOpenSingle)
        assertNotNull(r)
        assertTrue("pct < 2", r!!.percentile < 2)
    }

    // MARK: - Run Distribution

    @Test
    fun equalModeGivesEqualRuns() {
        val r1 = planner.runTime(0, 300, 5000, PacePlanner.RunMode.Equal)
        val r8 = planner.runTime(7, 300, 5000, PacePlanner.RunMode.Equal)
        assertEquals(r1, r8)
    }

    @Test
    fun adaptiveModeGivesProgressiveRuns() {
        val r1 = planner.runTime(0, 300, 5000, PacePlanner.RunMode.Adaptive)
        val r8 = planner.runTime(7, 300, 5000, PacePlanner.RunMode.Adaptive)
        assertTrue("run1 ($r1) > run8 ($r8)", r1 <= r8)
    }

    @Test
    fun runRatiosRun1EqualsRun2() {
        val ratios = planner.interpolatedRunRatios(5000)
        assertEquals(ratios[0], ratios[1], 0.001)
    }

    // MARK: - Full Plan

    @Test
    fun computePlanMenOpen() {
        val p = planner.computePlan(5040, HyroxDivision.MenOpenSingle, PacePlanner.RunMode.Adaptive)
        assertNotNull(p)
        p!!
        assertEquals(8, p.runTimes.size)
        assertEquals(8, p.stationTimes.size)
        assertEquals(5040, p.computedTotal)
        assertTrue("percentile in range", p.percentile > 0 && p.percentile < 100)
    }

    @Test
    fun computePlanEqualMode() {
        val p = planner.computePlan(5040, HyroxDivision.MenOpenSingle, PacePlanner.RunMode.Equal)
        assertNotNull(p)
        p!!
        assertEquals(5040, p.computedTotal)
        assertEquals(PacePlanner.RunMode.Equal, p.mode)
    }

    @Test
    fun allDivisionsPlannable() {
        for (division in HyroxDivision.entries) {
            assertNotNull(
                division.raw,
                planner.computePlan(5000, division),
            )
        }
    }

    @Test
    fun shorterGoalGivesBetterPercentile() {
        val fast = planner.computePlan(4200, HyroxDivision.MenOpenSingle)!!
        val slow = planner.computePlan(6000, HyroxDivision.MenOpenSingle)!!
        assertTrue("fast (${fast.percentile}) < slow (${slow.percentile})", fast.percentile < slow.percentile)
    }

    // MARK: - Tier

    @Test
    fun tierLabelsBoundary() {
        assertEquals("APEX", PacePlanner.tier(1.0))
        assertEquals("PRO", PacePlanner.tier(3.0))
        assertEquals("EXPERT", PacePlanner.tier(5.0))
        assertEquals("STRONG", PacePlanner.tier(10.0))
        assertEquals("SOLID", PacePlanner.tier(25.0))
        assertEquals("STEADY", PacePlanner.tier(50.0))
        assertEquals("RISING", PacePlanner.tier(75.0))
        assertEquals("STARTER", PacePlanner.tier(75.01))
    }
}
