package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

// MARK: - JSON Schema (pace_planner.json 과 1:1 매칭)

@Serializable
data class PacePlannerData(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("bucket_size_min") val bucketSizeMin: Int,
    @SerialName("run_ratio_table") val runRatioTable: List<RunRatioRow>,
    val divisions: Map<String, PlannerDivision>,
)

@Serializable
data class RunRatioRow(
    val t: Int,
    val r: List<Double>,
)

@Serializable
data class PlannerDivision(
    @SerialName("total_athletes") val totalAthletes: Int,
    val buckets: List<TimeBucket>,
)

@Serializable
data class TimeBucket(
    @SerialName("lo_min") val loMin: Int,
    @SerialName("hi_min") val hiMin: Int,
    val count: Int,
    @SerialName("pct_range") val pctRange: List<Double>,
    @SerialName("avg_overall") val avgOverall: Int,
    @SerialName("avg_run") val avgRun: Int,
    @SerialName("avg_rox") val avgRox: Int,
    @SerialName("avg_run_rox") val avgRunRox: Int,
    @SerialName("avg_pace_8_7") val avgPace87: Int,
    @SerialName("avg_station_total") val avgStationTotal: Int,
    val stations: Map<String, Int>,
)

// MARK: - Interpolated Result

data class InterpolatedBucket(
    val pctRangeLo: Double,
    val pctRangeHi: Double,
    val avgOverall: Int,
    val avgRun: Int,
    val avgRox: Int,
    val avgRunRox: Int,
    val avgPace87: Int,
    val avgStationTotal: Int,
    val stations: Map<String, Int>,
) {
    val percentile: Double get() = (pctRangeLo + pctRangeHi) / 2
    val roxFraction: Double get() = if (avgRunRox > 0) avgRox.toDouble() / avgRunRox.toDouble() else 0.0
}

// MARK: - Pace Plan Result

data class PacePlan(
    val goalTotalS: Int,
    /** Per-run times (8 values). */
    val runTimes: List<Int>,
    /** Per-station times keyed by StationKind raw string. */
    val stationTimes: Map<String, Int>,
    /** Seconds per 8.7km-equivalent lap. */
    val paceSeconds87: Int,
    /** Midpoint percentile (0–100). */
    val percentile: Double,
    /** Athletes in the source dataset. */
    val totalAthletes: Int,
    /** Computed total (should equal goalTotalS). */
    val computedTotal: Int,
    val mode: PacePlanner.RunMode,
    /** Rox fraction of run+rox, from bucket data. */
    val roxFraction: Double,
) {
    val runTotal: Int get() = runTimes.sum()
    val stationTotal: Int get() = stationTimes.values.sum()

    /** Split combined run+rox into (run, rox) using data-driven fraction. */
    fun splitRunRox(combinedS: Int): Pair<Int, Int> {
        val rox = (combinedS.toDouble() * roxFraction).roundToInt()
        return (combinedS - rox) to rox
    }
}

// MARK: - Pace Planner Engine

class PacePlanner(val data: PacePlannerData) {

    enum class RunMode { Equal, Adaptive }

    // MARK: - Bucket Interpolation (lerp)

    fun interpolate(targetMinutes: Double, division: HyroxDivision): InterpolatedBucket? {
        val div = data.divisions[division.raw] ?: return null
        return lerp(div.buckets, targetMinutes)
    }

    private fun lerp(buckets: List<TimeBucket>, targetMinutes: Double): InterpolatedBucket? {
        fun mid(b: TimeBucket) = (b.loMin + b.hiMin) / 2.0

        var lo: TimeBucket? = null
        var hi: TimeBucket? = null
        for (b in buckets) {
            if (mid(b) <= targetMinutes) lo = b
            if (mid(b) >= targetMinutes && hi == null) hi = b
        }

        val loB = lo ?: hi ?: return null
        val hiB = hi ?: lo ?: return null

        if (loB.loMin == hiB.loMin && loB.hiMin == hiB.hiMin) {
            return InterpolatedBucket(
                pctRangeLo = loB.pctRange[0],
                pctRangeHi = loB.pctRange[1],
                avgOverall = loB.avgOverall,
                avgRun = loB.avgRun,
                avgRox = loB.avgRox,
                avgRunRox = loB.avgRunRox,
                avgPace87 = loB.avgPace87,
                avgStationTotal = loB.avgStationTotal,
                stations = loB.stations,
            )
        }

        val t = (targetMinutes - mid(loB)) / (mid(hiB) - mid(loB))
        fun l(a: Int, b: Int): Int = (a.toDouble() + (b - a).toDouble() * t).roundToInt()
        fun ld(a: Double, b: Double): Double = a + (b - a) * t

        val stations = mutableMapOf<String, Int>()
        for ((key, loVal) in loB.stations) {
            val hiVal = hiB.stations[key] ?: continue
            stations[key] = l(loVal, hiVal)
        }

        return InterpolatedBucket(
            pctRangeLo = (ld(loB.pctRange[0], hiB.pctRange[0]) * 10).roundToInt() / 10.0,
            pctRangeHi = (ld(loB.pctRange[1], hiB.pctRange[1]) * 10).roundToInt() / 10.0,
            avgOverall = l(loB.avgOverall, hiB.avgOverall),
            avgRun = l(loB.avgRun, hiB.avgRun),
            avgRox = l(loB.avgRox, hiB.avgRox),
            avgRunRox = l(loB.avgRunRox, hiB.avgRunRox),
            avgPace87 = l(loB.avgPace87, hiB.avgPace87),
            avgStationTotal = l(loB.avgStationTotal, hiB.avgStationTotal),
            stations = stations,
        )
    }

    // MARK: - Run Distribution

    fun runTime(index: Int, paceSeconds87: Int, totalSeconds: Int, mode: RunMode): Int {
        val totalRun = paceSeconds87.toDouble() * 8.7
        return when (mode) {
            RunMode.Equal -> (totalRun / 8.0).roundToInt()
            RunMode.Adaptive -> {
                val ratios = interpolatedRunRatios(totalSeconds)
                val sum = ratios.sum()
                (totalRun * ratios[index] / sum).roundToInt()
            }
        }
    }

    fun interpolatedRunRatios(targetSeconds: Int): List<Double> {
        val table = data.runRatioTable
        val target = targetSeconds.toDouble()
        val first = table.firstOrNull() ?: return List(8) { 1.0 }
        val last = table.last()

        if (target <= first.t.toDouble()) return first.r
        if (target >= last.t.toDouble()) return last.r

        for (i in 0 until table.size - 1) {
            val lo = table[i]
            val hi = table[i + 1]
            if (target in lo.t.toDouble()..hi.t.toDouble()) {
                val f = (target - lo.t) / (hi.t - lo.t).toDouble()
                return lo.r.zip(hi.r) { a, b -> a + (b - a) * f }
            }
        }
        return last.r
    }

    // MARK: - Full Plan Computation

    fun computePlan(goalTotalS: Int, division: HyroxDivision, mode: RunMode = RunMode.Adaptive): PacePlan? {
        val targetMin = goalTotalS / 60.0
        val bucket = interpolate(targetMin, division) ?: return null
        val div = data.divisions[division.raw] ?: return null

        val stationTimes = bucket.stations.toMutableMap()
        var stnTotal = stationTimes.values.sum()

        val targetRun = goalTotalS - stnTotal
        val basePace = max(1, (targetRun.toDouble() / 8.7).roundToInt())
        var bestPace = basePace
        var bestDiff = Int.MAX_VALUE
        for (p in max(1, basePace - 3)..basePace + 3) {
            val runT = 8 * ((p.toDouble() * 8.7 / 8.0).roundToInt())
            val diff = abs(targetRun - runT)
            if (diff < bestDiff) {
                bestDiff = diff
                bestPace = p
            }
        }

        val runTimes = List(8) { i -> runTime(i, bestPace, goalTotalS, mode) }
        val runTotal = runTimes.sum()
        var residual = goalTotalS - (runTotal + stnTotal)

        val stationOrder = listOf(
            "skiErg", "sledPush", "sledPull", "burpeeBroadJumps",
            "rowing", "farmersCarry", "sandbagLunges", "wallBalls",
        )

        if (residual != 0) {
            val sign = if (residual > 0) 1 else -1
            var remaining = abs(residual)
            var idx = 0
            while (remaining > 0 && idx < 400) {
                val key = stationOrder[idx % 8]
                val v = stationTimes[key]
                if (v != null && v + sign >= 1) {
                    stationTimes[key] = v + sign
                    remaining--
                }
                idx++
            }
            stnTotal = stationTimes.values.sum()
        }

        val total = runTotal + stnTotal

        return PacePlan(
            goalTotalS = goalTotalS,
            runTimes = runTimes,
            stationTimes = stationTimes,
            paceSeconds87 = bestPace,
            percentile = bucket.percentile,
            totalAthletes = div.totalAthletes,
            computedTotal = total,
            mode = mode,
            roxFraction = bucket.roxFraction,
        )
    }

    companion object {
        private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

        /** Load bundled pace_planner.json (resources/pace/pace_planner.json). */
        fun loadBundled(): PacePlanner {
            val stream = PacePlanner::class.java.classLoader
                ?.getResourceAsStream("pace/pace_planner.json")
                ?: error("pace/pace_planner.json missing from resources")
            val text = stream.bufferedReader().use { it.readText() }
            val data = lenientJson.decodeFromString(PacePlannerData.serializer(), text)
            return PacePlanner(data)
        }

        /** Percentile tier label (matches iOS PacePlanner.tier). */
        fun tier(percentile: Double): String = when {
            percentile <= 1 -> "APEX"
            percentile <= 3 -> "PRO"
            percentile <= 5 -> "EXPERT"
            percentile <= 10 -> "STRONG"
            percentile <= 25 -> "SOLID"
            percentile <= 50 -> "STEADY"
            percentile <= 75 -> "RISING"
            else -> "STARTER"
        }
    }
}
