package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable

/**
 * HYROX competition divisions. Raw values match iOS Swift auto-derived
 * enum raw values (camelCase), keeping persistence and Garmin messages
 * cross-platform interoperable.
 */
@Serializable
enum class HyroxDivision(val raw: String) {
    MenOpenSingle("menOpenSingle"),
    MenOpenDouble("menOpenDouble"),
    MenProSingle("menProSingle"),
    MenProDouble("menProDouble"),
    WomenOpenSingle("womenOpenSingle"),
    WomenOpenDouble("womenOpenDouble"),
    WomenProSingle("womenProSingle"),
    WomenProDouble("womenProDouble"),
    MixedDouble("mixedDouble");

    val displayName: String
        get() = when (this) {
            MenOpenSingle -> "Men's Open — Singles"
            MenOpenDouble -> "Men's Open — Doubles"
            MenProSingle -> "Men's Pro — Singles"
            MenProDouble -> "Men's Pro — Doubles"
            WomenOpenSingle -> "Women's Open — Singles"
            WomenOpenDouble -> "Women's Open — Doubles"
            WomenProSingle -> "Women's Pro — Singles"
            WomenProDouble -> "Women's Pro — Doubles"
            MixedDouble -> "Mixed — Doubles"
        }

    val shortName: String
        get() = when (this) {
            MenOpenSingle -> "M Open"
            MenOpenDouble -> "M Open 2x"
            MenProSingle -> "M Pro"
            MenProDouble -> "M Pro 2x"
            WomenOpenSingle -> "W Open"
            WomenOpenDouble -> "W Open 2x"
            WomenProSingle -> "W Pro"
            WomenProDouble -> "W Pro 2x"
            MixedDouble -> "Mixed 2x"
        }

    companion object {
        fun fromRaw(raw: String): HyroxDivision? = entries.firstOrNull { it.raw == raw }
    }
}
