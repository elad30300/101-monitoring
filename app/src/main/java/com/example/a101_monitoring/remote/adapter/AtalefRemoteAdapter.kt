package com.example.a101_monitoring.remote.adapter

import com.example.a101_monitoring.data.model.Room
import com.example.a101_monitoring.remote.model.DepartmentBody
import com.example.a101_monitoring.remote.model.PatientBody

typealias OnResponseCallback<T> = (responseBody: T) -> Unit
typealias onFailedCallback = (throwable: Throwable) -> Unit
typealias onErrorCallback = (throwable: Throwable) -> Unit

interface AtalefRemoteAdapter {

    fun register(patient: PatientBody, onResponse: OnResponseCallback<PatientBody>, onFailed: onFailedCallback, onError: onErrorCallback)

    fun getDepartments(onResponse: OnResponseCallback<List<DepartmentBody>>, onFailed: onFailedCallback, onError: onErrorCallback)

    fun getAvailableBeds(room: Room, onResponse: OnResponseCallback<List<String>>, onFailed: onFailedCallback, onError: onErrorCallback)

}