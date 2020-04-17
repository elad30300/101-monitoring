package com.example.a101_monitoring.remote.service

import com.example.a101_monitoring.data.model.ReleaseReason
import com.example.a101_monitoring.remote.model.*
import retrofit2.Call
import retrofit2.http.*

interface AtalefService {

    @POST("patients/register")
    fun register(@Body body: PatientBody): Call<PatientBody>

    @POST("patients/signIn")
    fun signIn(@Body patientId: PatientSignInBody): Call<PatientBody>

    @GET("departments")
    fun getDepartments(): Call<List<DepartmentBody>>

    @GET("patients/beds")
    fun getAvailableBeds(@Query("roomNumber") room: String, @Query("DepartmentId") departmentId: Int): Call<List<String>>

    @GET("releaseReasons")
    fun getReleaseReasons(): Call<List<ReleaseReasonBody>>

    @PUT("patients/release")
    fun releasePatient(@Body releasePatientRequestBody: ReleasePatientRequestBody): Call<GeneralResponse>

}