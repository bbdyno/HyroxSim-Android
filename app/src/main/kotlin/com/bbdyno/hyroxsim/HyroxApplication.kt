package com.bbdyno.hyroxsim

import android.app.Application
import com.bbdyno.hyroxsim.sync.garmin.GarminImportService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HyroxApplication : Application() {

    @Inject lateinit var garminImportService: GarminImportService

    override fun onCreate() {
        super.onCreate()
        garminImportService.start()
    }
}
