package com.example.a101_monitoring

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a101_monitoring.download.DownloadController


class UpdateVersionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_version)

        onPackageInstallation()

        intent?.also {
            handleIntent(it)
        }
    }

    private fun onPackageInstallation() {
        Log.d(TAG, "onPackageInstallation activity")
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            DownloadController.PACKAGE_INSTALLED_ACTION -> onPackageInstalled(intent)
        }
    }


    private fun onPackageInstalled(intent: Intent) {
        val extras = intent.extras
        val status = extras.getInt(PackageInstaller.EXTRA_STATUS)
        val message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // This test app isn't privileged, so the user has to confirm the install.
                Log.d(TAG, "STATUS_PENDING_USER_ACTION")
                val confirmIntent = extras[Intent.EXTRA_INTENT] as Intent
                startActivity(confirmIntent)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d(TAG, "STATUS_SUCCESS")
                Toast.makeText(
                    this,
                    "Install succeeded!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                Log.e(TAG, "Install failed! $status, $message")
                Toast.makeText(
                    this, "Install failed! $status, $message",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Log.d(
                    TAG,
                    "Unrecognized status received from installer: $status"
                )
                Toast.makeText(
                    this, "Unrecognized status received from installer: $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.also {
            handleIntent(it)
        }
    }

    companion object {
        private const val TAG = "UpdateActivity"
    }
}