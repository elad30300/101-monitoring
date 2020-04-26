package com.example.a101_monitoring.ui

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.receiver.BluetoothAddressBroadcastReceiver
import com.example.a101_monitoring.states.SubmitSensorToPatientDoneState
import com.example.a101_monitoring.states.SubmitSensorToPatientFailedState
import com.example.a101_monitoring.states.SubmitSensorToPatientWorkingState
import com.example.a101_monitoring.utils.IndicationHelper
import com.example.a101_monitoring.viewmodel.SensorChooseViewModel
import kotlinx.android.synthetic.main.sensor_choose_fragment.*
import javax.inject.Inject

class SensorChooseFragment : Fragment(), BluetoothAddressBroadcastReceiver.BluetoothAddressReceiverListener {

    @Inject lateinit var viewModel: SensorChooseViewModel
    @Inject lateinit var localBroadcastManager: LocalBroadcastManager

    private val navigationArguments: SensorChooseFragmentArgs by navArgs()
    private lateinit var bluetoothAddressBroadcastReceiver: BluetoothAddressBroadcastReceiver

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

        initializeBluetoothAddressReceiver()
    }

    override fun onStart() {
        super.onStart()

        registerBluetoothAddressReceiver()
    }

    override fun onStop() {
        super.onStop()

        unregisterBluetoothAddressReceiver()
    }

    private fun registerBluetoothAddressReceiver() {
        context?.also {
            val filter = IntentFilter(BluetoothAddressBroadcastReceiver.ACTION_BLUETOOTH_ADDRESS_SUBMITTED).apply {
                addAction(BluetoothAddressBroadcastReceiver.ACTION_BLUETOOTH_ADDRESS_SUBMITTED)
            }
            localBroadcastManager.registerReceiver(bluetoothAddressBroadcastReceiver, filter)
        } ?: Log.i(TAG, "Couldn't register receiver because context is null")
    }

    private fun unregisterBluetoothAddressReceiver() {
        context?.also {
            localBroadcastManager.unregisterReceiver(bluetoothAddressBroadcastReceiver)
        } ?: Log.i(TAG, "Couldn't un-register receiver because context is null")
    }

    private fun initializeBluetoothAddressReceiver() {
        bluetoothAddressBroadcastReceiver = BluetoothAddressBroadcastReceiver().also {
            it.listener = this
        }
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
            if (isInputValid()) {
                viewModel.setSensor(patientId, sensor_address.text.toString().trim().toUpperCase())
            } else {
                Toast.makeText(context, R.string.sensor_address_invalid_input_message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeSubmitSensorToPatientState() {
        viewModel.getSubmitSensorToPatientState().observe(viewLifecycleOwner, Observer {
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

    private fun isInputValid() = BluetoothAdapter.checkBluetoothAddress(sensor_address.text.toString().trim().toUpperCase())

    override fun onBtAddressSubmitted(address: String) {
        sensor_address.setText(address)
        context?.also {
            IndicationHelper.vibrate(it)
        }
    }

    companion object {
        fun newInstance() = SensorChooseFragment()
        private const val TAG = "SensorChooseFragment"
    }

}
