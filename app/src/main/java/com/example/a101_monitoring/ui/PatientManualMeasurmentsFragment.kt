package com.example.a101_monitoring.ui

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.di.component.PatientManualMeasurmentsComponent
import javax.inject.Inject

class PatientManualMeasurmentsFragment : Fragment() {

    companion object {
        fun newInstance() = PatientManualMeasurmentsFragment()
    }

    private lateinit var patientManualMeasurmentsComponent: PatientManualMeasurmentsComponent
    @Inject lateinit var viewModel: PatientManualMeasurmentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.patient_manual_measurments_fragment, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        initializePatientManualMeasurmentsComponent(context)
    }

    private fun initializePatientManualMeasurmentsComponent(context: Context) {
        (context.applicationContext as MyApplication).applicationComponent.patientManualMeasurmentsComponent().create().also {
            it.inject(this)
        }
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
////        viewModel = ViewModelProviders.of(this).get(PatientManualMeasurmentsViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

}
