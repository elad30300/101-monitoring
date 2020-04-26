package com.example.a101_monitoring.di.module

import android.app.Application
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.example.a101_monitoring.data.database.ApplicationDatabase
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class ApplicationModule(val context: Application) {

    @Singleton
    @Provides
    fun provideApplicationContext() = context

    @Singleton
    @Provides
    fun provideLocalBroadcastManager(context: Application) = LocalBroadcastManager.getInstance(context)

    @Singleton
    @Provides
    fun provideExecutor(): Executor = Executors.newSingleThreadExecutor()

    @Singleton
    @Provides
    fun provideApplicationDatabase(): ApplicationDatabase = Room.databaseBuilder(
                                        context,
                                        ApplicationDatabase::class.java,
                                        "application-database"
                                    )
//                                        .addMigrations(ApplicationDatabase.migration1to2)
                                        .build()

    @Singleton
    @Provides
    fun providePatientDao(database: ApplicationDatabase) = database.patientDao()

    @Singleton
    @Provides
    fun provideMeasurementsDao(database: ApplicationDatabase) = database.measurementsDao()

    @Singleton
    @Provides
    fun provideDepartmentDao(database: ApplicationDatabase) = database.departmentDao()

    @Singleton
    @Provides
    fun provideRoomDao(database: ApplicationDatabase) = database.roomDao()

    @Singleton
    @Provides
    fun provideReleaseReasonsDao(database: ApplicationDatabase) = database.releaseReasonsDao()

}