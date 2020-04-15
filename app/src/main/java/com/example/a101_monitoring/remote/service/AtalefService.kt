package com.example.a101_monitoring.remote.service

import com.example.a101_monitoring.remote.model.DepartmentBody
import com.example.a101_monitoring.remote.model.PatientBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AtalefService {

    @POST("patients/register")
    fun register(@Body body: PatientBody): Call<PatientBody>

    @GET("departments")
    fun getDepartments(): Call<List<DepartmentBody>>

    @GET("patients/beds")
    fun getAvailableBeds(@Query("roomNumber") room: String, @Query("DepartmentId") departmentId: Int): Call<List<String>>

}