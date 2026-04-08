//
//  DurationFormatterTest.kt
//  core-format
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.format

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationFormatterTest {
    @Test
    fun `formats hms and pace`() {
        assertEquals("1:01:01", DurationFormatter.hms(3661.0))
        assertEquals("5'42\" /km", DurationFormatter.pace(342.0))
    }
}
