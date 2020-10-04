package com.example.a101_monitoring.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a101_monitoring.remote.adapter.AtalefRemoteAdapter
import com.example.a101_monitoring.remote.model.Version
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import java.net.URI
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersioningRepository @Inject constructor(
    private val atalefRemoteAdapter: AtalefRemoteAdapter,
    private val executor: Executor
) {

    fun getLatestVersion(phoneId: String, phoneNumber: String, version: String): LiveData<URI> {
        val uri = MutableLiveData<URI>(null)

        executor.execute {
            Log.i(
                TAG,
                "Asking remote server for latest version \nphoneId = $phoneId, number = $phoneNumber, version = $version"
            )
            atalefRemoteAdapter.getLatestVersion(
                Version(phoneId, phoneNumber, version),
                { response ->
                    val result = response.string()
                    Log.i(
                        TAG,
                        "Get latest version, response body = $result"
                    )

                    if (result?.contains("http") == true) {
                        uri.value = URI.create(result)
                    }
                },
                {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "failed to get latest version", it)
                },
                {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "error in get latest version", it)
                }
            )
        }

        return uri
    }

    companion object {
        private const val TAG = "VersionRepo"
    }
}