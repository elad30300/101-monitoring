package com.example.a101_monitoring

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.a101_monitoring.bluetooth.BluetoothController
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.download.DownloadController
import com.example.a101_monitoring.log.logger.Logger
import com.example.a101_monitoring.nfc.NfcController
import com.example.a101_monitoring.states.*
import com.example.a101_monitoring.ui.AppBarContainer
import com.example.a101_monitoring.ui.PatientsListFragment
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.example.a101_monitoring.utils.TimeHelper
import com.example.a101_monitoring.viewmodel.MainViewModel
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
    @Inject lateinit var nfcController: NfcController
    @Inject lateinit var logger: Logger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (applicationContext as MyApplication).applicationComponent.mainActivityComponent().create().also {
            it.inject(this)
        }

        initializeNfc()

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

    private fun initializeNfc() {
        NfcAdapter.getDefaultAdapter(this)?.also {
            val intent = Intent(this, javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try {
                    addDataType("text/plain")    /* Handles all MIME based dispatches.
                                 You should specify only the ones that you need. */
                } catch (e: IntentFilter.MalformedMimeTypeException) {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "addDataType failed", e)
                }
            }
            intentFiltersArray = arrayOf(ndef)
            techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
        } ?: Log.i(TAG, "this device doesn't have NFC / NFC is off")
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
    }

    override fun onResume() {
        super.onResume()
        NfcAdapter.getDefaultAdapter(this)?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.also {
            when (it.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED -> { onNdefTagScanned(it.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) }
                NfcAdapter.ACTION_ADAPTER_STATE_CHANGED -> onNfcAdapterStateChanged(it.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, 0))
            }
        }
    }

    private fun onNdefTagScanned(rawMessages: Array<Parcelable>) {
        try {
            val messages = rawMessages.map { it as NdefMessage }
            nfcController.onNdefTagScanned(messages)
        } catch (exception: Exception) {
            Log.i(TAG, "Tag was scanned but is not NDEF tag, exception: ${exception.message}")
            exception.printStackTrace()
        }

    }

    private fun onNfcAdapterStateChanged(state: Int) {
        TODO("Not implemented")
    }

    override fun onDestroy() {
        super.onDestroy()

        deinitializeBleScanComponents()
    }

    override fun onFragmentSetTitleRequest(title: String) {
        toolbar?.also {
            it.title = title
        }
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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        requestPermissions(
            PERMISSIONS_STORAGE,
            1234)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1234) {
            val approvedResults = grantResults.filter { it == PackageManager.PERMISSION_GRANTED }
            if (approvedResults.size == 5) {
                // save phone number in application settings
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Storing phone number")
                    val telephonyManager = (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                    val phoneId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        telephonyManager.imei
                            ?: Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
                    } else {
                        telephonyManager.deviceId
                            ?: Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
                    }
                    phoneId?.also {
                        ApplicationSettings.sharedInstance.phoneId = it
                    } ?: logger.e(TAG, "could not read phone id")
                } else {
                    Log.i("Permission", "no Permissions")
                }
            }
        }
    }

    private fun registerNetworkConnectionCallback() {
        val connectivityManager = getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    super.onAvailable(network)
                    Log.i(TAG, "Network is available")
                    try {
                        networkConnectionDialog?.dismiss()
                    } catch (ex: java.lang.Exception) {
                        DefaultCallbacksHelper.onErrorDefault(TAG, "can't dismiss dialog due to $ex\n${ex.stackTrace}", ex, logger)
                    }
                    TimeHelper.instance.initializeTimer()
                    runOnUiThread {
                        validateVersion()
                    }
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
            try {
                this?.also {
                    Log.d(TAG, "about to show dialog for network")
                    networkConnectionDialog = AlertDialog.Builder(this)
                        .setTitle("Network error")
                        .setMessage("אין לך אינטרנט, בבקשה להתחבר לוויפי או להפעיל נתונים סלולרים")
                        .setPositiveButton("הגדרות") { _, _ ->
                            startActivityForResult(
                                Intent(android.provider.Settings.ACTION_SETTINGS),
                                REQUEST_ENABLE_NETWORK
                            )
                        }.setNegativeButton("ביטול", null).setCancelable(false)
                        .show()?.apply {
                            getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                                if (hasNetworkConnection()) {
                                    dismiss()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "נא להתחבר לאינטרנט!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                }
            } catch (ex: java.lang.Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "can't display dialog due to $ex\n${ex.stackTrace}", ex, logger)
            }
        }
    }

    private fun hasNetworkConnection() = (getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork != null

    private fun observeStates() {
        observerCheckPatientExistState()
        observerRegisterPatientState()
        observeSignInPatientState()
        observeSubmitSensorToPatientState()
        observeBloodPressureState()
        observeBodyTemperatureState()
        observeReleasePatientState()
    }

    private fun observerCheckPatientExistState() {
        mainViewModel.getCheckPatientExistState().observe(this, Observer {
            when(it.javaClass) {
                CheckPatientExistDoneState::class.java -> onFoundPatientExist()
                CheckPatientExistFailedState::class.java -> onCheckPatientExistFailed()
            }
        })
    }

    private fun onFoundPatientExist() {
        Toast.makeText(this, R.string.check_patient_exist_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onCheckPatientExistFailed() {
        Toast.makeText(this, R.string.check_patient_exist_fail_message, Toast.LENGTH_LONG).show()
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

    private fun onReleasePatientFailed() {
        Toast.makeText(this, R.string.release_patient_fail_message, Toast.LENGTH_LONG).show()
    }

    private fun observeReleasePatientState() {
        mainViewModel.getReleasePatientState().observe(this, Observer {
            when(it.javaClass) {
                ReleasePatientFailedState::class.java -> onReleasePatientFailed()
            }
        })
    }


    private fun onSubmitBodyTemperatureSuccessfully() {
        Toast.makeText(this, R.string.manual_measurements_success_message, Toast.LENGTH_LONG).show()
    }

    private fun onSubmitBodyTemperatureFailed() {
        Toast.makeText(this, R.string.manual_measurements_fail_message, Toast.LENGTH_LONG).show()
    }

    private fun validateVersion() {
        Log.i(TAG, "Called validate version")
        val phoneId: String
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted")
            val tm = (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            phoneId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm.imei
                    ?: Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
            } else {
                tm.deviceId
                    ?: Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
            }

            mainViewModel.getLatestVersion(phoneId, tm.voiceMailNumber, BuildConfig.VERSION_CODE.toString()).observe(this, Observer {
                it?.let { uri ->
                    Log.i(TAG, "Received apk uri, calling download")
                    showProgress(DownloadController(this, this, uri.toString(), logger).enqueueDownload())
                }
            })
        } else {
            Log.i("Permission", "no Permissions")
        }
    }

    private fun showProgress(downloadId: Long) {
        val progressBarDialog = ProgressDialog(this)
        progressBarDialog.setTitle("Download App Data, Please Wait")
        progressBarDialog.setCancelable(false)
        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressBarDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "אישור") { _, _ -> }

        progressBarDialog.progress = 0

        Thread(Runnable {
            var downloading = true
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            while (downloading) {

                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                val cursor = manager.query(q)
                cursor.moveToFirst()
                var bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                var bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }

                val dl_progress: Int = ((bytes_downloaded * 100L) / bytes_total).toInt()

                runOnUiThread {
                    progressBarDialog.progress = dl_progress
                }
                cursor.close()
            }
        }).start()

        progressBarDialog.show()
        progressBarDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            if (progressBarDialog.progress == 100) {
                progressBarDialog.dismiss()
            } else {
                Toast.makeText(this, "נא להמתין לסיום עדכון התוכנה", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_NETWORK = 2
    }
}
