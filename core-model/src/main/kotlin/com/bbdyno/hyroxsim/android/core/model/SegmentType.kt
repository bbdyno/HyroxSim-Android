//
//  SegmentType.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable

enum class SegmentType(
    val tracksLocation: Boolean,
    val tracksHeartRate: Boolean = true,
) : Serializable {
    RUN(tracksLocation = true),
    ROX_ZONE(tracksLocation = true),
    STATION(tracksLocation = false),
}
