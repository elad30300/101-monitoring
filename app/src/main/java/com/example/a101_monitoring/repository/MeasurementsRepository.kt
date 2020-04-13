package com.example.a101_monitoring.repository

import android.util.Log
import com.example.a101_monitoring.data.dao.MeasurementsDao
import com.example.a101_monitoring.data.model.HeartRate
import com.example.a101_monitoring.data.model.RespiratoryRate
import com.example.a101_monitoring.data.model.Saturation
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import java.lang.Exception
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementsRepository @Inject constructor(
    private val measurementsDao: MeasurementsDao
) {

    private val executor = Executors.newSingleThreadExecutor()


    fun getAllHeartRates() = measurementsDao.getAllHeartRates()

    fun getAllSaturations() = measurementsDao.getAllSaturations()

    fun getAllRespiratoryRates() = measurementsDao.getAllRespiratoryRates()

    fun getAllHeartRatesForPatient(patientId: Int) = measurementsDao.getAllHeartRatesForPatient(patientId)

    fun getAllSaturationsForPatient(patientId: Int) = measurementsDao.getAllSaturationsForPatient(patientId)

    fun getAllRespiratoryRatesForPatient(patientId: Int) = measurementsDao.getAllRespiratoryRatesForPatient(patientId)

    fun getLastHeartRateForPatient(patientId: Int) = measurementsDao.getLastHeartRateForPatient(patientId)

    fun getLastSaturationForPatient(patientId: Int) = measurementsDao.getLastSaturationForPatient(patientId)

    fun getLastRespiratoryRateForPatient(patientId: Int) = measurementsDao.getLastRespiratoryRateForPatient(patientId)

    fun insertMeasurements(heartRate: HeartRate?, saturation: Saturation?, respiratoryRate: RespiratoryRate?) {
        heartRate?.apply {
            insertHeartRates(this)
        }
        saturation?.apply {
            insertSaturations(this)
        }
        respiratoryRate?.apply {
            insertRespiratoryRates(this)
        }
    }

    fun insertHeartRates(heartRate: HeartRate) {
        executor.execute {
            Log.d(TAG, "insert $heartRate")
            try {
                measurementsDao.insertHeartRates(heartRate)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "insert heart rate failed", exception)
            }
        }
    }

    fun updateHeartRates(heartRate: HeartRate) {
        executor.execute {
            try {
                measurementsDao.updateHeartRates(heartRate)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "update heart rate failed", exception)
            }
        }
    }

    fun deleteHeartRates(heartRate: HeartRate) {
        executor.execute {
            try {
                measurementsDao.deleteHeartRates(heartRate)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "delete heart rate failed", exception)
            }
        }
    }

    fun insertSaturations(saturation: Saturation) {
        Log.d(TAG, "insert $saturation")
        executor.execute {
            try {
                measurementsDao.insertSaturations(saturation)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "insert saturation failed", exception)
            }
        }
    }

    fun updateSaturations(saturation: Saturation) {
        executor.execute {
            try {
                measurementsDao.updateSaturations(saturation)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "update saturation failed", exception)
            }
        }
    }

    fun deleteSaturations(saturation: Saturation) {
        executor.execute {
            try {
                measurementsDao.deleteSaturations(saturation)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "delete saturation failed", exception)
            }
        }
    }

    fun insertRespiratoryRates(respiratoryRate: RespiratoryRate) {
        Log.d(TAG, "insert $respiratoryRate")
        executor.execute {
            try {
                measurementsDao.insertRespiratoryRates(respiratoryRate)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "insert respiratory rate failed", exception)
            }
        }
    }

    fun updateRespiratoryRates(respiratoryRate: RespiratoryRate) {
        executor.execute {
            try {
                measurementsDao.updateRespiratoryRates(respiratoryRate)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "update respiratory rate failed", exception)
            }
        }
    }

    fun deleteRespiratoryRates(respiratoryRate: RespiratoryRate) {
        executor.execute {
            try {
                measurementsDao.deleteRespiratoryRates(respiratoryRate)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "delete respiratory rate failed", exception)
            }
        }
    }

    companion object {
        val TAG = "${this.javaClass::class.java.name}"
    }

}