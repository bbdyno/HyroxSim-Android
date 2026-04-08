//
//  LocationSample.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import java.time.Instant

data class LocationSample(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val horizontalAccuracy: Double,
    val speed: Double? = null,
    val course: Double? = null,
) : Serializable
