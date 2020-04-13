package com.example.a101_monitoring.utils

import io.reactivex.disposables.Disposable

object DisposableHelper {
    fun dispose(disposable: Disposable?) {
        disposable?.apply {
            if (!isDisposed) {
                dispose()
            }
        }
    }
}
