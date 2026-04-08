//
//  HyroxPresetsTest.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HyroxPresetsTest {
    @Test
    fun `built in presets contain all divisions`() {
        assertEquals(9, HyroxPresets.all.size)
    }

    @Test
    fun `men open single template has expected segment count`() {
        assertEquals(31, HyroxPresets.menOpenSingle.segments.size)
        assertTrue(HyroxPresets.menOpenSingle.isBuiltIn)
    }
}
