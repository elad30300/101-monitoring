package com.example.a101_monitoring.di.module

import com.example.a101_monitoring.remote.AtalefServiceConstants
import com.example.a101_monitoring.remote.adapter.AtalefRemoteAdapter
import com.example.a101_monitoring.remote.adapter.RetrofitAtalefRemoteAdapter
import com.example.a101_monitoring.remote.service.AtalefService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class RemoteServiceModule {

    @Singleton
    @Provides
    fun provideGson() = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson) = Retrofit.Builder()
                                        .baseUrl(AtalefServiceConstants.baseUrl)
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build()

    @Singleton
    @Provides
    fun provideAtalefService(retrofit: Retrofit) = retrofit.create(AtalefService::class.java)

    @Singleton
    @Provides
    fun provideAtalefRemoteAdapter(atalefService: AtalefService): AtalefRemoteAdapter = RetrofitAtalefRemoteAdapter(atalefService)

}