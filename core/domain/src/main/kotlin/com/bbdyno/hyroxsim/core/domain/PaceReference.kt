package com.bbdyno.hyroxsim.core.domain

/**
 * Division-specific reference totals (seconds). Used as fallback when the
 * user hasn't set an explicit target. Values mirror the iOS
 * `PaceReference.swift` median benchmarks.
 */
object PaceReference {

    fun referenceTotalSeconds(division: HyroxDivision): Int = when (division) {
        HyroxDivision.MenOpenSingle -> 75 * 60
        HyroxDivision.MenOpenDouble -> 65 * 60
        HyroxDivision.MenProSingle -> 70 * 60
        HyroxDivision.MenProDouble -> 60 * 60
        HyroxDivision.WomenOpenSingle -> 90 * 60
        HyroxDivision.WomenOpenDouble -> 78 * 60
        HyroxDivision.WomenProSingle -> 82 * 60
        HyroxDivision.WomenProDouble -> 70 * 60
        HyroxDivision.MixedDouble -> 72 * 60
    }

    /**
     * Proportional split across the 31 segments. Rough but deterministic —
     * matches the Monkey C watch-side `PaceReference.defaultSegmentSeconds`
     * so deltas render consistently if phone doesn't explicitly push
     * per-segment targets.
     *
     * Returns the per-segment target in seconds, aligned with `template.segments`.
     */
    fun defaultTargetSegmentsSeconds(
        template: WorkoutTemplate,
        totalSecondsOverride: Int? = null,
    ): List<Int> {
        val total = totalSecondsOverride
            ?: template.division?.let(::referenceTotalSeconds)
            ?: (80 * 60)
        return template.segments.map { seg ->
            defaultSegmentSeconds(seg.type, total)
        }
    }

    private fun defaultSegmentSeconds(type: SegmentType, totalSeconds: Int): Int = when (type) {
        SegmentType.Run -> (totalSeconds * 47) / 100 / 8
        SegmentType.Station -> (totalSeconds * 46) / 100 / 8
        SegmentType.RoxZone -> (totalSeconds * 7) / 100 / 15
    }
}
