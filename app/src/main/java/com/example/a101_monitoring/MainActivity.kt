package com.example.a101_monitoring

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.nfc.tech.NfcF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.a101_monitoring.bluetooth.BluetoothController
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.states.*
import com.example.a101_monitoring.ui.AppBarContainer
import com.example.a101_monitoring.ui.PatientsListFragment
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.ui.SensorChooseFragment
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.example.a101_monitoring.utils.TimeHelper
import com.example.a101_monitoring.viewmodel.MainViewModel
import com.example.a101_monitoring.viewmodel.SensorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), PatientsListFragment.OnListFragmentInteractionListener,
    AppBarContainer {

    private var networkConnectionDialog: AlertDialog? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null

    @Inject lateinit var mainViewModel: MainViewModel
    @Inject lateinit var bluetoothController: BluetoothController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (applicationContext as MyApplication).applicationComponent.mainActivityComponent().create().also {
            it.inject(this)
        }

        // nfc code
//        NfcAdapter.getDefaultAdapter(this)?.also {
//            val intent = Intent(this, javaClass).apply {
//                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            }
//            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
//                try {
//                    addDataType("plain/text")    /* Handles all MIME based dispatches.
//                                 You should specify only the ones that you need. */
//                } catch (e: IntentFilter.MalformedMimeTypeException) {
//                    DefaultCallbacksHelper.onErrorDefault(TAG, "addDataType failed", e)
//                }
//            }
//            intentFiltersArray = arrayOf(ndef)
//            techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
//        } ?: Log.i(TAG, "this device doesn't have NFC / NFC is off")
        //

        initializeAppBar()

        initializePermissions()

        checkIfBluetoothOn()

        initializeBleScanComponents()

        registerNetworkConnectionCallback()

        if (!hasNetworkConnection()) {
            forceNetworkConnection()
        } else {
            TimeHelper.instance.initializeTimer()
        }

        observeStates()
    }

//    override fun onPause() {
//        super.onPause()
//        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        NfcAdapter.getDefaultAdapter(this)?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
//    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        intent?.also {
//            when (it.action) {
//                NfcAdapter.ACTION_NDEF_DISCOVERED -> onNdefTagScanned(it.getParcelableExtra(NfcAdapter.EXTRA_TAG))
//                NfcAdapter.ACTION_ADAPTER_STATE_CHANGED -> onNfcAdapterStateChanged(it.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, 0))
//            }
//        }
//    }
//
//    private fun onNdefTagScanned(tag: Tag) {
//
//    }
//
//    private fun onNfcAdapterStateChanged(state: Int) {
//        NfcAdapter.ReaderCallback
//    }

    override fun onDestroy() {
        super.onDestroy()

        deinitializeBleScanComponents()
    }

    override fun onFragmentSetTitleRequest(title: String) {
        toolbar.title = title
    }

    private fun initializeAppBar() {
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)
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

    private fun observeStates() {
        observerRegisterPatientState()
        observeSignInPatientState()
        observeSubmitSensorToPatientState()
        observeBloodPressureState()
        observeBodyTemperatureState()
    }

    private fun observerRegisterPatientState() {
        mainViewModel.getRegisterPatientState().observe(this, Observer {
            when(it.javaClass) {
                RegisterPatientDoneState::class.java -> onPatientRegisteredSuccessfully()
                RegisterPatientFailedState::class.java -> onPatientRegisterFailed()
            }
        })
    }

    private fun onPatientRegisteredSuccessfully() {
        Toast.makeText(this, R.string.register_patient_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onPatientRegisterFailed() {
        Toast.makeText(this, R.string.register_patient_fail_message, Toast.LENGTH_LONG).show()
    }

    private fun observeSignInPatientState() {
        mainViewModel.getSignInPatientState().observe(this, Observer {
            when(it.javaClass) {
                SignInPatientDoneState::class.java -> onPatientSignedInSuccessfully()
                SignInPatientFailedState::class.java -> onPatientSignInFailed()
            }
        })
    }

    private fun onPatientSignedInSuccessfully() {
        Toast.makeText(this, R.string.sign_in_patient_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onPatientSignInFailed() {
        Toast.makeText(this, R.string.sign_in_patient_fail_message, Toast.LENGTH_LONG).show()
    }

    private fun observeSubmitSensorToPatientState() {
        mainViewModel.getSubmitSensorToPatientState().observe(this, Observer {
            when(it.javaClass) {
                SubmitSensorToPatientDoneState::class.java -> onSubmitSensorToPatientSuccessfully()
                SubmitSensorToPatientFailedState::class.java -> onSubmitSensorToPatientFailed()
            }
        })
    }

    private fun onSubmitSensorToPatientSuccessfully() {
        Toast.makeText(this, R.string.submit_sensor_to_patient_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onSubmitSensorToPatientFailed() {
        Toast.makeText(this, R.string.submit_sensor_to_patient_fail_message, Toast.LENGTH_LONG).show()
    }

    private fun observeBloodPressureState() {
        mainViewModel.getBloodPressureState().observe(this, Observer {
            when(it.javaClass) {
                BloodPressureDoneState::class.java -> onSubmitBloodPressureSuccessfully()
                BloodPressureFailedState::class.java -> onSubmitBloodPressureFailed()
            }
        })
    }

    private fun onSubmitBloodPressureSuccessfully() {
        Toast.makeText(this, R.string.manual_measurements_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onSubmitBloodPressureFailed() {
        Toast.makeText(this, R.string.manual_measurements_fail_message, Toast.LENGTH_LONG).show()
    }

    private fun observeBodyTemperatureState() {
        mainViewModel.getBodyTemperatureState().observe(this, Observer {
            when(it.javaClass) {
                BodyTemperatureDoneState::class.java -> onSubmitBodyTemperatureSuccessfully()
                BodyTemepratureFailedState::class.java -> onSubmitBodyTemperatureFailed()
            }
        })
    }

    private fun onSubmitBodyTemperatureSuccessfully() {
        Toast.makeText(this, R.string.manual_measurements_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onSubmitBodyTemperatureFailed() {
        Toast.makeText(this, R.string.manual_measurements_fail_message, Toast.LENGTH_LONG).show()
    }


    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_NETWORK = 2
    }
}
