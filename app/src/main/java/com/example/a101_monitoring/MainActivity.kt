package com.example.a101_monitoring

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.a101_monitoring.bluetooth.BluetoothController
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.ui.PatientsListFragment
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.viewmodel.SensorViewModel
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), PatientsListFragment.OnListFragmentInteractionListener {

//    @Inject lateinit var statesViewModel: StatesViewModel
    @Inject lateinit var sensorViewModel: SensorViewModel
    @Inject lateinit var bluetoothController: BluetoothController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (applicationContext as MyApplication).applicationComponent.mainActivityComponent().create().also {
            it.inject(this)
        }

        initializePermissions()

        checkIfBluetoothOn()

        initializeBleScanComponents()
    }

    override fun onDestroy() {
        super.onDestroy()

        deinitializeBleScanComponents()
    }

    private fun initializeBleScanComponents() {
        bluetoothController.start()
    }

    private fun deinitializeBleScanComponents() {
        bluetoothController.close()
    }

    override fun onListFragmentInteraction(item: Patient?) {
        item?.apply {
            val action = PatientsListFragmentDirections.actionPatientFragmentToPatientManualMeasurmentsFragment(getIdentityField())
            nav_host_fragment.findNavController().navigate(action) // TODO check if this is correct implementation
        }
    }

    /* check if BT is on/off
     * on - do nothing
      off - popup to open BT*/
    private fun checkIfBluetoothOn() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Phone doesn't support BT
            Toast.makeText(this, "BT not supported exit app", Toast.LENGTH_SHORT).show()
            this.finishAndRemoveTask()
        } else if (!bluetoothAdapter.isEnabled) {
            // BT is disabled and need to be open
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun initializePermissions() {
        val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        requestPermissions(
            PERMISSIONS_STORAGE,
            1234)
    }

//    private fun setStatesObservers() {
//        statesViewModel.registerPatientState.observe(this, Observer {
//            if (!it) {
//                runOnUiThread {
//                    Toast.makeText(this, "רישום מטופל נכשל", Toast.LENGTH_SHORT).show()
//                }
//            }
//        })
//    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}
