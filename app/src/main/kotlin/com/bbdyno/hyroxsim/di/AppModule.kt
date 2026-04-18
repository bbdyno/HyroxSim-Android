package com.bbdyno.hyroxsim.di

import android.content.Context
import com.bbdyno.hyroxsim.core.persistence.HyroxDatabase
import com.bbdyno.hyroxsim.core.persistence.dao.TemplateDao
import com.bbdyno.hyroxsim.core.persistence.dao.WorkoutDao
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutRepository
import com.bbdyno.hyroxsim.sync.garmin.GarminBridge
import com.bbdyno.hyroxsim.sync.garmin.GarminTemplateSyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HyroxDatabase =
        HyroxDatabase.create(context)

    @Provides
    fun provideWorkoutDao(db: HyroxDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideTemplateDao(db: HyroxDatabase): TemplateDao = db.templateDao()

    @Provides
    @Singleton
    fun provideWorkoutRepository(dao: WorkoutDao): WorkoutRepository = WorkoutRepository(dao)

    @Provides
    @Singleton
    fun provideTemplateRepository(dao: TemplateDao): TemplateRepository = TemplateRepository(dao)

    @Provides
    @Singleton
    fun provideGarminBridge(@ApplicationContext context: Context): GarminBridge =
        GarminBridge.provide(context)

    @Provides
    @Singleton
    fun provideGarminTemplateSyncService(bridge: GarminBridge): GarminTemplateSyncService =
        GarminTemplateSyncService(bridge)
}
