//
//  HyroxDivision.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable

enum class HyroxDivision(
    val displayName: String,
    val shortName: String,
) : Serializable {
    MEN_OPEN_SINGLE("Men's Open — Singles", "M Open"),
    MEN_OPEN_DOUBLE("Men's Open — Doubles", "M Open 2x"),
    MEN_PRO_SINGLE("Men's Pro — Singles", "M Pro"),
    MEN_PRO_DOUBLE("Men's Pro — Doubles", "M Pro 2x"),
    WOMEN_OPEN_SINGLE("Women's Open — Singles", "W Open"),
    WOMEN_OPEN_DOUBLE("Women's Open — Doubles", "W Open 2x"),
    WOMEN_PRO_SINGLE("Women's Pro — Singles", "W Pro"),
    WOMEN_PRO_DOUBLE("Women's Pro — Doubles", "W Pro 2x"),
    MIXED_DOUBLE("Mixed — Doubles", "Mixed 2x"),
}
