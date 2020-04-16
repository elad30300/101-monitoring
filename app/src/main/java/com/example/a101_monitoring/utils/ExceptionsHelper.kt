package com.example.a101_monitoring.utils

import java.lang.Exception

object ExceptionsHelper {
    fun tryBlock(tag: String, description: String, block: () -> Unit) {
        try {
            block()
        } catch (exception: Exception) {
            DefaultCallbacksHelper.onErrorDefault(tag, "had exception in $description", exception)
        }
    }
}