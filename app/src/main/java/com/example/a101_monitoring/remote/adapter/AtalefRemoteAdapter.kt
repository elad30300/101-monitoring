package com.example.a101_monitoring.remote.adapter

import com.example.a101_monitoring.data.model.Room
import com.example.a101_monitoring.remote.model.*

typealias OnResponseCallback<T> = (responseBody: T) -> Unit
typealias OnFailedCallback = (throwable: Throwable) -> Unit
typealias OnErrorCallback = (throwable: Throwable) -> Unit

interface AtalefRemoteAdapter {

    fun register(patient: PatientBody, onResponse: OnResponseCallback<PatientBody>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getDepartments(onResponse: OnResponseCallback<List<DepartmentBody>>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getAvailableBeds(room: Room, onResponse: OnResponseCallback<List<String>>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun getReleaseReasons(onResponse: OnResponseCallback<List<ReleaseReasonBody>>, onFailed: OnFailedCallback, onError: OnErrorCallback)

    fun releasePatient(releasePatientRequestBody: ReleasePatientRequestBody, onResponse: OnResponseCallback<GeneralResponse>, onFailed: OnFailedCallback, onError: OnErrorCallback)

}