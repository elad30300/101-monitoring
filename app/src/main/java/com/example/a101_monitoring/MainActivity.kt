package com.example.a101_monitoring

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.a101_monitoring.bluetooth.BluetoothController
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.ui.PatientsListFragment
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.utils.TimeHelper
import com.example.a101_monitoring.viewmodel.SensorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), PatientsListFragment.OnListFragmentInteractionListener {

    private var networkConnectionDialog: AlertDialog? = null

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

        registerNetworkConnectionCallback()

        if (!hasNetworkConnection()) {
            forceNetworkConnection()
        } else {
            TimeHelper.instance.initializeTimer()
        }
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

    private fun registerNetworkConnectionCallback() {
        val connectivityManager = getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    super.onAvailable(network)
                    Log.i(TAG, "Network is available")
                    networkConnectionDialog?.dismiss()
                    TimeHelper.instance.initializeTimer()
                }

                override fun onLost(network: Network?) {
                    super.onLost(network)
                    Log.i(TAG, "Network is lost")
                    Thread.sleep(500)
                    forceNetworkConnection()
                }
            }
        )
    }

    private fun forceNetworkConnection() {
        if (!hasNetworkConnection()) {
            networkConnectionDialog = AlertDialog.Builder(this)
                .setTitle("Network error")
                .setMessage("אין לך אינטרנט, בבקשה להתחבר לוויפי או להפעיל נתונים סלולרים")
                .setPositiveButton("הגדרות") { _, _ ->
                    startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), REQUEST_ENABLE_NETWORK)
                }.setNegativeButton("ביטול", null).setCancelable(false)
                .show()?.apply {
                    getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                        if (hasNetworkConnection()) {
                            dismiss()
                        } else {
                            Toast.makeText(context, "נא להתחבר לאינטרנט!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

    private fun hasNetworkConnection() = (getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork != null

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
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_NETWORK = 2
    }
}
