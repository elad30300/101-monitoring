package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.SpinnerAdapter
import androidx.lifecycle.Observer
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Department
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

        initializeDepartments()

        initializeBeds()
    }

    private fun initializeDepartments() {
        registerPatientViewModel.departments.observe(viewLifecycleOwner, Observer {
            val departmentNames = it.map { it.department.name } as MutableList<String>
            department_spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, departmentNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        })
        department_spinner.onItemSelectedListener = onDepartmentSelectedItem
    }

    private fun initializeBeds() {
        room_spinner.onItemSelectedListener = onRoomSelectedItem
    }

    private val onDepartmentSelectedItem = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            registerPatientViewModel.departments.value?.also {
                val selectedDepartment = it[position]
                val roomNames = selectedDepartment.rooms.map { it.name }
                room_spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, roomNames).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            room_spinner.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item)
        }

    }

    private val onRoomSelectedItem = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            registerPatientViewModel.departments.value?.also {
                val room = it[department_spinner.selectedItemPosition].rooms[position]
                registerPatientViewModel.getAvailableBeds(room).observe(viewLifecycleOwner, Observer {
                    bed_spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, it).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                })
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            bed_spinner.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item)
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
