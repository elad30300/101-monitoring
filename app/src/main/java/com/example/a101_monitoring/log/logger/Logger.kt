package com.example.a101_monitoring.log.logger

import android.util.Log
import com.example.a101_monitoring.log.Configurations
import java.lang.Exception

class Logger : LoggerAdapter() {
    private val azureLoggingTool = AzureLoggingTool.getInstance(Configurations.CURRENT_ENVIRONMENT)

    override fun logD(tag: String, msg: String) {
        try {
            azureLoggingTool.logMessage("${tag}:${msg}", LogTypes.DEBUG)
        } catch (e: Exception) {
            Log.e("azure-logging", "could not log message with error $e")
        }
    }

    override fun logE(tag: String, msg: String) {
        try {
            azureLoggingTool.logMessage("${tag}:${msg}", LogTypes.ERROR)
        } catch (e: Exception) {
            Log.e("azure-logging", "could not log message")
        }
    }

    override fun logI(tag: String, msg: String) {
        try {
            azureLoggingTool.logMessage("${tag}:${msg}", LogTypes.INFO)
        } catch (e: Exception) {
            Log.e("azure-logging", "could not log message")
        }
    }

    override fun logV(tag: String, msg: String) {
        try {
            azureLoggingTool.logMessage("${tag}:${msg}", LogTypes.VERBOSE)
        } catch (e: Exception) {
            Log.e("azure-logging", "could not log message")
        }
    }

    override fun logW(tag: String, msg: String) {
        try {
            azureLoggingTool.logMessage("${tag}:${msg}", LogTypes.WARNING)
        } catch (e: Exception) {
            Log.e("azure-logging", "could not log message")
        }
    }

    companion object {
        private var instance: Logger? = null

        fun getInstance(): Logger {
            if (instance == null) {
                instance = Logger()
            }

            return instance!!
        }
    }
}