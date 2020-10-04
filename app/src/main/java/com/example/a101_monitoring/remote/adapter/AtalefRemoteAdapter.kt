package com.example.a101_monitoring.remote.adapter

import com.example.a101_monitoring.data.model.Room
import com.example.a101_monitoring.remote.model.*
import okhttp3.ResponseBody

typealias OnResponseCallback<T> = (responseBody: T) -> Unit
typealias OnFailedCallback = (throwable: Throwable) -> Unit
typealias OnErrorCallback = (throwable: Throwable) -> Unit

interface AtalefRemoteAdapter {

    fun register(patient: PatientBody, onResponse: OnResponseCallback<PatientBody>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun signIn(patient: PatientSignInBody, onResponse: OnResponseCallback<PatientBody>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getDepartments(onResponse: OnResponseCallback<List<DepartmentBody>>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getAvailableBeds(room: String, departmentId: Int, onResponse: OnResponseCallback<List<String>>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getReleaseReasons(onResponse: OnResponseCallback<List<ReleaseReasonBody>>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun releasePatient(releasePatientRequestBody: ReleasePatientRequestBody, onResponse: OnResponseCallback<GeneralResponse>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun sendMeasurement(measurementBody: MeasurementBody, onResponse: OnResponseCallback<GeneralResponse>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun sendBloodPressure(bloodPressureBody: BloodPressureBody, onResponse: OnResponseCallback<BooleanResponse>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun sendBodyTemperature(body: BodyTemperatureBody, onResponse: OnResponseCallback<BooleanResponse>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getLatestVersion(version: Version, onResponse: OnResponseCallback<ResponseBody>, onFailed: OnFailedCallback, onError: OnErrorCallback)

}