//
//  HyroxPresets.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

object HyroxPresets {
    val menOpenSingle: WorkoutTemplate = buildTemplate(HyroxDivision.MEN_OPEN_SINGLE)
    val menOpenDouble: WorkoutTemplate = buildTemplate(HyroxDivision.MEN_OPEN_DOUBLE)
    val menProSingle: WorkoutTemplate = buildTemplate(HyroxDivision.MEN_PRO_SINGLE)
    val menProDouble: WorkoutTemplate = buildTemplate(HyroxDivision.MEN_PRO_DOUBLE)
    val womenOpenSingle: WorkoutTemplate = buildTemplate(HyroxDivision.WOMEN_OPEN_SINGLE)
    val womenOpenDouble: WorkoutTemplate = buildTemplate(HyroxDivision.WOMEN_OPEN_DOUBLE)
    val womenProSingle: WorkoutTemplate = buildTemplate(HyroxDivision.WOMEN_PRO_SINGLE)
    val womenProDouble: WorkoutTemplate = buildTemplate(HyroxDivision.WOMEN_PRO_DOUBLE)
    val mixedDouble: WorkoutTemplate = buildTemplate(HyroxDivision.MIXED_DOUBLE)

    val all: List<WorkoutTemplate> = listOf(
        menOpenSingle,
        menOpenDouble,
        menProSingle,
        menProDouble,
        womenOpenSingle,
        womenOpenDouble,
        womenProSingle,
        womenProDouble,
        mixedDouble,
    )

    fun template(division: HyroxDivision): WorkoutTemplate =
        when (division) {
            HyroxDivision.MEN_OPEN_SINGLE -> menOpenSingle
            HyroxDivision.MEN_OPEN_DOUBLE -> menOpenDouble
            HyroxDivision.MEN_PRO_SINGLE -> menProSingle
            HyroxDivision.MEN_PRO_DOUBLE -> menProDouble
            HyroxDivision.WOMEN_OPEN_SINGLE -> womenOpenSingle
            HyroxDivision.WOMEN_OPEN_DOUBLE -> womenOpenDouble
            HyroxDivision.WOMEN_PRO_SINGLE -> womenProSingle
            HyroxDivision.WOMEN_PRO_DOUBLE -> womenProDouble
            HyroxDivision.MIXED_DOUBLE -> mixedDouble
        }

    private fun buildTemplate(division: HyroxDivision): WorkoutTemplate {
        val specs = HyroxDivisionSpec.stations(division)
        val segments = buildList {
            specs.forEachIndexed { index, spec ->
                add(WorkoutSegment.run())
                add(WorkoutSegment.roxZone())
                add(
                    WorkoutSegment.station(
                        spec.kind,
                        target = spec.target,
                        weightKg = spec.weightKg,
                        weightNote = spec.weightNote,
                    ),
                )
                if (index < specs.lastIndex) {
                    add(WorkoutSegment.roxZone())
                }
            }
        }

        return WorkoutTemplate(
            name = division.displayName,
            division = division,
            segments = segments,
            isBuiltIn = true,
        )
    }
}
