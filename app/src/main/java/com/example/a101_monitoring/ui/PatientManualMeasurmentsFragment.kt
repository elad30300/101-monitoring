package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.di.component.PatientManualMeasurmentsComponent
import com.example.a101_monitoring.states.*
import com.example.a101_monitoring.utils.TimeHelper
import com.example.a101_monitoring.viewmodel.PatientManualMeasurmentsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.patient_manual_measurments_fragment.*
import javax.inject.Inject

class PatientManualMeasurmentsFragment : Fragment() {

    companion object {
        fun newInstance() = PatientManualMeasurmentsFragment()
    }

    @Inject lateinit var viewModel: PatientManualMeasurmentsViewModel

    val navigationArguments: PatientManualMeasurmentsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.patient_manual_measurments_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSendButtonOnClick()

        observeStates()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context as? AppBarContainer)?.onFragmentSetTitleRequest(navigationArguments.patientId)

        initializePatientManualMeasurmentsComponent(context)
    }

    private fun initializePatientManualMeasurmentsComponent(context: Context) {
        (context.applicationContext as MyApplication).applicationComponent.patientManualMeasurmentsComponent().create().also {
            it.inject(this)
        }
    }

    private fun setSendButtonOnClick() {
        send_manual_measurments_button.setOnClickListener {
            sendBodyTemperature()
            sendBloodPressure()
        }
    }

    private fun sendBodyTemperature() {
        val patientId = navigationArguments.patientId
        val temperatureText = patient_manual_temperature.text.toString().trim()
        if (temperatureText != "") {
            if (isPatientTemperatureInputValid()) {
                viewModel.sendBodyTemperature(temperatureText.toFloat(), patientId)
            } else {
                Toast.makeText(context, R.string.body_temperature_illegal_input_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendBloodPressure() {
        val patientId = navigationArguments.patientId
        val diastolicText = patient_manual_diastolic.text.toString().trim()
        val systolicText = patient_manual_systolic.text.toString().trim()
        if (diastolicText != "" && systolicText != "") {
            if (isPatientBloodPressureInputValid()) {
                viewModel.sendBloodPressure(diastolicText.toInt(), systolicText.toInt(), patientId)
            } else {
                Toast.makeText(context, R.string.blood_pressure_illegal_input_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isPatientTemperatureInputValid(): Boolean {
        val input = patient_manual_temperature.text.toString().trim()
        return input.toFloatOrNull() != null
    }

    private fun isPatientBloodPressureInputValid(): Boolean {
        val diastolicText = patient_manual_diastolic.text.toString().trim()
        val systolicText = patient_manual_systolic.text.toString().trim()

        return diastolicText != "" && systolicText != "" && diastolicText.toIntOrNull() != null && systolicText.toIntOrNull() != null
    }

    private fun observeStates() {
        observeBloodPressureState()
        observeBodyTemperatureState()
    }

    private fun observeBloodPressureState() {
        viewModel.getBloodPressureState().observe(this, Observer {
            when(it.javaClass) {
                BloodPressureDoneState::class.java -> onSubmitBloodPressureSuccessfully()
                BloodPressureFailedState::class.java -> onSubmitBloodPressureFailed()
                BloodPressureWorkingState::class.java -> onSubmitBloodPressureWorking()
            }
        })
    }

    private fun onSubmitBloodPressureSuccessfully() {
        if (viewModel.getBodyTemperatureState().value !is BodyTemperatureWorkingState) {
            manual_measurements_progress_bar.visibility = View.INVISIBLE
        }
    }

    private fun onSubmitBloodPressureFailed() {
        if (viewModel.getBodyTemperatureState().value !is BodyTemperatureWorkingState) {
            manual_measurements_progress_bar.visibility = View.INVISIBLE
        }
    }

    private fun onSubmitBloodPressureWorking() {
        manual_measurements_progress_bar.visibility = View.VISIBLE
    }

    private fun observeBodyTemperatureState() {
        viewModel.getBodyTemperatureState().observe(this, Observer {
            when(it.javaClass) {
                BodyTemperatureDoneState::class.java -> onSubmitBodyTemperatureSuccessfully()
                BodyTemepratureFailedState::class.java -> onSubmitBodyTemperatureFailed()
                BodyTemperatureWorkingState::class.java -> onSubmitBodyTemperatureWorking()
            }
        })
    }

    private fun onSubmitBodyTemperatureSuccessfully() {
        if (viewModel.getBloodPressureState().value !is BloodPressureWorkingState) {
            manual_measurements_progress_bar.visibility = View.INVISIBLE
        }
    }

    private fun onSubmitBodyTemperatureFailed() {
        if (viewModel.getBloodPressureState().value !is BloodPressureWorkingState) {
            manual_measurements_progress_bar.visibility = View.INVISIBLE
        }
    }

    private fun onSubmitBodyTemperatureWorking() {
        manual_measurements_progress_bar.visibility = View.VISIBLE
    }

}
