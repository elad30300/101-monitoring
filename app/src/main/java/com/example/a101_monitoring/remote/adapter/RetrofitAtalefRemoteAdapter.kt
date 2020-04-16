package com.example.a101_monitoring.remote.adapter

import com.example.a101_monitoring.data.model.Room
import com.example.a101_monitoring.remote.model.DepartmentBody
import com.example.a101_monitoring.remote.model.PatientBody
import com.example.a101_monitoring.remote.service.AtalefService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import javax.inject.Singleton

@Singleton
class RetrofitAtalefRemoteAdapter(
    private val atalefService: AtalefService
)  : AtalefRemoteAdapter {


    override fun register(patient: PatientBody, onResponse: OnResponseCallback<PatientBody>, onFailed: OnFailedCallback,
                          onError: OnErrorCallback) {
        request(atalefService.register(patient), onResponse, onFailed, onError)
    }

    override fun getDepartments(onResponse: OnResponseCallback<List<DepartmentBody>>, onFailed: OnFailedCallback,
                                onError: OnErrorCallback) {
        request(atalefService.getDepartments(), onResponse, onFailed, onError)
    }

    override fun getAvailableBeds(room: Room, onResponse: OnResponseCallback<List<String>>,
                                  onFailed: OnFailedCallback, onError: OnErrorCallback) {
        request(atalefService.getAvailableBeds(room.name, room.departmentId), onResponse, onFailed, onError)
    }

    private fun <T: Any>request(call: Call<T>, onResponse: OnResponseCallback<T>, onFailed: OnFailedCallback, onError: OnErrorCallback) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful && response.body() != null) {
                    onResponse(response.body()!!)
                } else {
                    val failMessage = buildFailMessageForResponse(response, "Got failed response")
                    onFailed(Exception(failMessage))
                }
            }
            override fun onFailure(call: Call<T>, t: Throwable) {
                onError(t)
            }
        })
    }

    private fun <T: Any>buildFailMessageForResponse(response: Response<T>, baseMessage: String): String = baseMessage

    companion object {
        const val TAG = "RetrofitAtalefAdapter"
    }

}