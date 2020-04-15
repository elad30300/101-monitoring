package com.example.a101_monitoring.di.module

import android.app.Application
import androidx.room.Room
import com.example.a101_monitoring.data.database.ApplicationDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(val context: Application) {

    @Singleton
    @Provides
    fun provideApplicationContext() = context

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

}