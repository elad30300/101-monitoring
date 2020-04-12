package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.di.component.PatientManualMeasurmentsComponent
import com.example.a101_monitoring.viewmodel.PatientManualMeasurmentsViewModel
import kotlinx.android.synthetic.main.patient_manual_measurments_fragment.*
import javax.inject.Inject

class PatientManualMeasurmentsFragment : Fragment() {

    companion object {
        fun newInstance() = PatientManualMeasurmentsFragment()
    }

    private lateinit var patientManualMeasurmentsComponent: PatientManualMeasurmentsComponent
    @Inject lateinit var viewModel: PatientManualMeasurmentsViewModel

    val navigationArguments: PatientManualMeasurmentsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.patient_manual_measurments_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        patient_manual_measurments_title.text = navigationArguments.patientId.toString()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        initializePatientManualMeasurmentsComponent(context)

        initializeViewModel()
    }

    private fun initializePatientManualMeasurmentsComponent(context: Context) {
        (context.applicationContext as MyApplication).applicationComponent.patientManualMeasurmentsComponent().create().also {
            it.inject(this)
        }
    }

    private fun initializeViewModel() {
        viewModel.apply {
            patientId = navigationArguments.patientId
        }
    }


//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
////        viewModel = ViewModelProviders.of(this).get(PatientManualMeasurmentsViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

}
