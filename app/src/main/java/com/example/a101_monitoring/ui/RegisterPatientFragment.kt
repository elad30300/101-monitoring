package com.example.a101_monitoring.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a101_monitoring.R
import com.example.a101_monitoring.viewmodel.RegisterPatientViewModel


class RegisterPatientFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterPatientFragment()
    }

    private lateinit var registerPatientViewModel: RegisterPatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.register_patient_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerPatientViewModel = ViewModelProviders.of(this).get(RegisterPatientViewModel::class.java)
        // TODO: Use the ViewModel
    }



}
