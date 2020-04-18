package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.states.SubmitSensorToPatientDoneState
import com.example.a101_monitoring.states.SubmitSensorToPatientFailedState
import com.example.a101_monitoring.states.SubmitSensorToPatientWorkingState
import com.example.a101_monitoring.viewmodel.SensorChooseViewModel
import kotlinx.android.synthetic.main.sensor_choose_fragment.*
import javax.inject.Inject

class SensorChooseFragment : Fragment() {

    companion object {
        fun newInstance() = SensorChooseFragment()
    }

    @Inject lateinit var viewModel: SensorChooseViewModel

    private val navigationArguments: SensorChooseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sensor_choose_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigationArguments.patientId.let {
            observePatientSensorAddress(it)
            setSaveAddressButtonOnClickListener(it)
        }

        observeSubmitSensorToPatientState()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        initializeDaggerComponent(context)
    }

    private fun initializeDaggerComponent(context: Context) {
        (context.applicationContext as MyApplication).applicationComponent.sensorChooseComponent().create().also {
            it.inject(this)
        }
    }

    private fun observePatientSensorAddress(patientId: PatientIdentityFieldType) {
        viewModel.getSensor(patientId).observe(viewLifecycleOwner, Observer {
            sensor_address.setText(it ?: "")
        })
    }

    private fun setSaveAddressButtonOnClickListener(patientId: PatientIdentityFieldType) {
        sensor_address_save_button.setOnClickListener {
            viewModel.setSensor(patientId, sensor_address.text.toString())
        }
    }

    private fun observeSubmitSensorToPatientState() {
        viewModel.getSubmitSensorToPatientState().observe(this, Observer {
            when(it.javaClass) {
                SubmitSensorToPatientDoneState::class.java -> onSubmitSensorToPatientSuccessfully()
                SubmitSensorToPatientFailedState::class.java -> onSubmitSensorToPatientFailed()
                SubmitSensorToPatientWorkingState::class.java -> onSubmitSensorToPatientWorking()
            }
        })
    }

    private fun onSubmitSensorToPatientSuccessfully() {
        choose_sensor_progress_bar.visibility = View.INVISIBLE
    }

    private fun onSubmitSensorToPatientFailed() {
        choose_sensor_progress_bar.visibility = View.INVISIBLE
    }

    private fun onSubmitSensorToPatientWorking() {
        choose_sensor_progress_bar.visibility = View.VISIBLE
    }

}
