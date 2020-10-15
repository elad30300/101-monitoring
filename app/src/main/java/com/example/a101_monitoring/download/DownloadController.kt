package com.example.a101_monitoring.download

import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.example.a101_monitoring.BuildConfig
import com.example.a101_monitoring.MainActivity
import com.example.a101_monitoring.UpdateVersionActivity
import com.example.a101_monitoring.log.logger.Logger
import java.io.File


class DownloadController(
    private val context: Context,
    private val activity: Activity?,
    private val url: String,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "DownloadCtrl"
        private const val FILE_NAME = "atalefnitur.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".fileprovider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
        const val REQUEST_INSTALL = 1456
        const val PACKAGE_INSTALLED_ACTION =
            "com.example.a101_monitoring.SESSION_API_PACKAGE_INSTALLED"
    }

    fun enqueueDownload(): Long {
        var destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)
        if (file.exists()) file.delete()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        request.setMimeType(MIME_TYPE)
        request.setTitle("atalefnitur")
        request.setDescription("Downloading new version")
        // set destination
        request.setDestinationUri(uri)
        showInstallOption(destination, uri)
        // Enqueue a new download and same the referenceId
        return downloadManager.enqueue(request)
    }

    private fun showInstallOption(
        destination: String,
        uri: Uri
    ) {
        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val contentUri = FileProvider.getUriForFile(
                            context,
                            BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                            File(destination)
                        )

//                    val install = Intent(Intent.ACTION_VIEW)
////                    install.setData(contentUri)
////                    install.setType(MIME_TYPE)
////                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//////                    install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
////                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
////                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//////                    install.data = contentUri
////                    activity?.startActivity(install) ?: Log.e(TAG, "activity is null")

                        context.apply {
                            contentResolver.openInputStream(contentUri)?.use { apkStream ->
                                val installer =
                                    context.applicationContext.packageManager.packageInstaller.apply {
                                        registerSessionCallback(object :
                                            PackageInstaller.SessionCallback() {
                                            override fun onCreated(sessionId: Int) {
                                                Log.d(TAG, "new session $sessionId was created")
                                            }

                                            override fun onActiveChanged(
                                                sessionId: Int,
                                                active: Boolean
                                            ) {
                                                Log.d(
                                                    TAG,
                                                    "session $sessionId active changed to $active"
                                                )
                                            }

                                            override fun onBadgingChanged(sessionId: Int) {
                                                Log.d(TAG, "session $sessionId badging changed")
                                            }

                                            override fun onFinished(
                                                sessionId: Int,
                                                success: Boolean
                                            ) {
                                                if (success) {
                                                    Log.i(
                                                        TAG,
                                                        "session $sessionId finished with sucessfully"
                                                    )
                                                } else {
                                                    Log.e(
                                                        TAG,
                                                        "session $sessionId finished with failure"
                                                    )
                                                }
                                            }

                                            override fun onProgressChanged(
                                                sessionId: Int,
                                                progress: Float
                                            ) {
                                                Log.d(
                                                    TAG,
                                                    "session $sessionId progress changed to $progress"
                                                )
                                            }
                                        })
                                    }
                                val length =
                                    DocumentFile.fromSingleUri(
                                        context.applicationContext,
                                        contentUri
                                    )
                                        ?.length() ?: -1
                                val params =
                                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                                val sessionId = installer.createSession(params)
                                val session = installer.openSession(sessionId)

                                try {
                                    session.openWrite("package", 0, length).use { sessionStream ->
                                        apkStream.copyTo(sessionStream)
                                        session.fsync(sessionStream)
                                    }


                                    activity?.also {
                                        val intent = Intent(
                                            context,
                                            UpdateVersionActivity::class.java
                                        ).apply {
                                            action = PACKAGE_INSTALLED_ACTION
                                        }
                                        val pendingIntent =
                                            PendingIntent.getActivity(context, 0, intent, 0)

                                        val intentSender = pendingIntent.intentSender
                                        logger.i(TAG, "about to start version update")
                                        session.commit(intentSender)
//                                session.close()
                                    }
                                } catch (ex: Exception) {
                                    logger.e(TAG, "failed to use session, ex $ex\n${ex.stackTrace}")
                                    session?.apply { abandon() }
                                }
                            }
                        }

                        context.unregisterReceiver(this)
                        // finish()
                    } else {
                        val install = Intent(Intent.ACTION_VIEW)
                        install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        install.setDataAndType(
                            uri,
                            APP_INSTALL_PATH
                        )
                        context.startActivity(install)
                        context.unregisterReceiver(this)
                        // finish()
                    }
                } catch (ex: java.lang.Exception) {
                    logger.e(TAG, "failed in onComplete download, ex $ex\n${ex.stackTrace}")
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
//
//    class InstallService(private val context: Context) :
//        IntentService(InstallService::class.simpleName) {
//        override fun onHandleIntent(intent: Intent?) {
//            val packageInstaller = context.packageManager.packageInstaller
//            val params =
//                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
//            val sessionId = packageInstaller.createSession(params)
//            val session = packageInstaller.openSession(sessionId)
//            addApkToInstallSession(contentUri, session)
//            val intent = Intent(context, MainActivity::class.java)
//            intent.action = PACKAGE_INSTALLED_ACTION
//            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
//            val statusReceiver = pendingIntent.intentSender
//            session.commit(statusReceiver)
//        }
//
//
//    }
//
//    private fun addApkToInstallSession(uri: Uri, session: PackageInstaller.Session) {
//        try {
//            val packageInSession = session.openWrite(context.packageName, 0, -1)
//            val inputStream = context.contentResolver.openInputStream(uri)
//            val buffer = ByteArray(16384)
//            var n = inputStream.read(buffer)
//            while (n >= 0) {
//                packageInSession.write(buffer, 0, n)
//                n = inputStream.read(buffer)
//            }
//        } catch (ex: Exception) {
//            logger.e(TAG, "addApkToInstallSession exception $ex\n${ex.stackTrace}")
//        }
//    }

}