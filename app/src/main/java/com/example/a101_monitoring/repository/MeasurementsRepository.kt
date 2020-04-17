package com.example.a101_monitoring.repository

import android.util.Log
import com.example.a101_monitoring.data.dao.MeasurementsDao
import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.model.HeartRate
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.data.model.RespiratoryRate
import com.example.a101_monitoring.data.model.Saturation
import com.example.a101_monitoring.remote.adapter.AtalefRemoteAdapter
import com.example.a101_monitoring.remote.model.MeasurementBody
import com.example.a101_monitoring.utils.DataRemoteHelper
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementsRepository @Inject constructor(
    private val measurementsDao: MeasurementsDao,
    private val patientDao: PatientDao,
    private val atalefRemoteAdapter: AtalefRemoteAdapter,
    private val executor: Executor
) {

    fun getAllHeartRates() = measurementsDao.getAllHeartRates()

    fun getAllSaturations() = measurementsDao.getAllSaturations()

    fun getAllRespiratoryRates() = measurementsDao.getAllRespiratoryRates()

    fun getAllHeartRatesForPatient(patientId: PatientIdentityFieldType) = measurementsDao.getAllHeartRatesForPatient(patientId)

    fun getAllSaturationsForPatient(patientId: PatientIdentityFieldType) = measurementsDao.getAllSaturationsForPatient(patientId)

    fun getAllRespiratoryRatesForPatient(patientId: PatientIdentityFieldType) = measurementsDao.getAllRespiratoryRatesForPatient(patientId)

    fun getLastHeartRateForPatient(patientId: PatientIdentityFieldType) = measurementsDao.getLastHeartRateForPatient(patientId)

    fun getLastSaturationForPatient(patientId: PatientIdentityFieldType) = measurementsDao.getLastSaturationForPatient(patientId)

    fun getLastRespiratoryRateForPatient(patientId: PatientIdentityFieldType) = measurementsDao.getLastRespiratoryRateForPatient(patientId)

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
        sendMeasurements(heartRate, saturation, respiratoryRate)
    }

    private fun sendMeasurements(heartRate: HeartRate?, saturation: Saturation?, respiratoryRate: RespiratoryRate?) {
        if (heartRate == null && saturation == null && respiratoryRate == null) {
            Log.e(TAG, "try to send to remote all missing measurements")
            return
        }
        val patientId = heartRate?.patientId ?: saturation?.patientId ?: respiratoryRate?.patientId
        val patient = patientDao.getPatient(patientId!!)
        val measurementBody = DataRemoteHelper.fromDataToRemoteMeasurements(heartRate, saturation, respiratoryRate, patient)
        sendMeasurementsToRemote(measurementBody)
    }

    private fun sendMeasurementsToRemote(measurements: MeasurementBody) {
        executor.execute {
            atalefRemoteAdapter.sendMeasurement(measurements,
                {
                    onMeasurementSentToRemote(measurements)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "failure: send measurements to remote $measurements")
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "error: send measurements to remote $measurements")
                }
            )
        }
    }

    private fun onMeasurementSentToRemote(sentMeasurements: MeasurementBody) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "sent measurements $sentMeasurements successfully to remote")
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
        val TAG = "MeasurementsRepository"
    }

}