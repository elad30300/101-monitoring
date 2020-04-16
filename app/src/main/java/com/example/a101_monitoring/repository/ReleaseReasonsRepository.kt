package com.example.a101_monitoring.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a101_monitoring.data.dao.ReleaseReasonsDao
import com.example.a101_monitoring.data.model.ReleaseReason
import com.example.a101_monitoring.remote.adapter.AtalefRemoteAdapter
import com.example.a101_monitoring.remote.model.ReleaseReasonBody
import com.example.a101_monitoring.utils.DataRemoteHelper
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.example.a101_monitoring.utils.ExceptionsHelper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleaseReasonsRepository @Inject constructor(
    private val releaseReasonsDao: ReleaseReasonsDao,
    private val atalefRemoteAdapter: AtalefRemoteAdapter,
    private val executor: Executor
) {

    private val password = "2" // TODO: should be hashed so decompiler won't have access to password
    private val releaseReasons = releaseReasonsDao.getReleaseReasons()

    fun getReleaseReasons(): LiveData<List<ReleaseReason>> {
        refreshReleaseReasonsFromRemote()
        return releaseReasons
    }

    fun checkReleaseAccessPassword(password: String): Boolean { // TODO: should implement it with hash
        return this.password == password
    }

    private fun refreshReleaseReasonsFromRemote() {
        executor.execute {
            atalefRemoteAdapter.getReleaseReasons(
                {
                    onGetReleaseReasonsFromRemote(it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "failure in fetch release reasons from remote", it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "error in fetch release reasons from remote", it)
                }
            )
        }
    }

    private fun onGetReleaseReasonsFromRemote(releaseReasons: List<ReleaseReasonBody>) {
        Log.d(TAG, "got ${releaseReasons.size} from remote")
        insertReleaseReasons(DataRemoteHelper.fromRemoteToDataReleaseReasonList(releaseReasons))
    }

    private fun insertReleaseReasons(releaseReasons: List<ReleaseReason>) {
        executor.execute {
            ExceptionsHelper.tryBlock(TAG, "insert release reasons to dao") {
                releaseReasonsDao.insertList(releaseReasons)
            }
        }
    }

    companion object {
        const val TAG = "ReleaseReasonsRepo"
    }

}