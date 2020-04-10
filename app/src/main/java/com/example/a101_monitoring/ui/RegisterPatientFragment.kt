package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.R
import com.example.a101_monitoring.di.component.RegisterPatientComponent
import com.example.a101_monitoring.viewmodel.RegisterPatientViewModel
import kotlinx.android.synthetic.main.register_patient_fragment.*
import javax.inject.Inject


class RegisterPatientFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterPatientFragment()
    }

    private lateinit var registerPatientComponent: RegisterPatientComponent
    @Inject lateinit var registerPatientViewModel: RegisterPatientViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.register_patient_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        register_button.setOnClickListener {
            onRegisterClicked(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeRegisterPatientComponent()
    }

    fun initializeRegisterPatientComponent() {
        registerPatientComponent = (context?.applicationContext as MyApplication).applicationComponent.registerPatientComponent().create()
        registerPatientComponent.inject(this)
    }

    fun onRegisterClicked(view: View) {
        registerPatientViewModel.registerPatient(
            registered_patient_id.text.toString().toInt(),
            1,
            "1",
            "1",
            patient_haiti.toString(),
            doctor.toString(),
            is_civilian_switch.isSelected,
            0,
            true
        )
    }

}
