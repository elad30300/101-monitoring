package com.example.a101_monitoring.viewmodel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a101_monitoring.viewmodel.PatientItemViewModel

class PatientItemViewModelFactory(
    private val context: Context,
    private val patientId: Int) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PatientItemViewModel(context, patientId) as T
    }

}