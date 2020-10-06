package com.example.a101_monitoring.log.logger

import com.example.a101_monitoring.log.Environments
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlobContainer
import java.text.SimpleDateFormat
import java.util.*


class AzureLoggingTool(environment: Environments) {
    private val storageConnectionString = when (environment) {
        Environments.DEV -> "DefaultEndpointsProtocol=https;AccountName=101logsdevelop;AccountKey=mW2yF/9T0h4e2DOMT3tmMN99RhkbaPZyvO3tNNx5DA0rzetzW7fot2bM7Azk5z5fmoApTc4668s33PFDTgcgLg==;EndpointSuffix=core.windows.net"
        Environments.TEST -> "DefaultEndpointsProtocol=https;AccountName=101logsdevelop;AccountKey=mW2yF/9T0h4e2DOMT3tmMN99RhkbaPZyvO3tNNx5DA0rzetzW7fot2bM7Azk5z5fmoApTc4668s33PFDTgcgLg==;EndpointSuffix=core.windows.net"
        Environments.PROD -> "DefaultEndpointsProtocol=https;AccountName=101logsprod;AccountKey=pd6AYUvLwEbXVubMAUBpKRK1+izv8jdU/ETQF0deWOeKCxuaoWLP4Vz7IEGsGXOZncEs5esJJtfi/ukWadFiJA==;EndpointSuffix=core.windows.net"
    }

    @Throws(Exception::class)
    private fun getContainer(): CloudBlobContainer? {
        // Retrieve storage account from connection-string.
        val storageAccount = CloudStorageAccount
            .parse(storageConnectionString)

        // Create the blob client.
        val blobClient = storageAccount.createCloudBlobClient()

        // Get a reference to a container.
        // The container name must be lower case
        return blobClient.getContainerReference("logs")
    }

    private fun getBlobName(): String {
        val pattern = "yyyy-MM-dd"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    @Throws(java.lang.Exception::class)
    fun logMessage(message: String, logType: LogTypes) {
        val container = getContainer()
        container!!.createIfNotExists()

        val logBlob = container.getAppendBlobReference("$logType-${getBlobName()}")

        if (!logBlob.exists()) {
            logBlob.createOrReplace()
        }

        logBlob.appendText(message)
    }

    companion object {
        private var instance: AzureLoggingTool? = null;

        fun getInstance(environment: Environments): AzureLoggingTool {
            if (instance == null) {
                instance = AzureLoggingTool(environment)
            }

            return instance!!
        }
    }
}