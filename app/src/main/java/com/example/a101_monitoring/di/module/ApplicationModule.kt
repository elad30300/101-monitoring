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
    fun provideApplicationDatabase(): ApplicationDatabase = Room.databaseBuilder(
                                        context,
                                        ApplicationDatabase::class.java,
                                        "application-database"
                                    )
                                        .build()

    @Singleton
    @Provides
    fun providePatientDao(database: ApplicationDatabase) = database.patientDao()

}