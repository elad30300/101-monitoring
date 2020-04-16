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
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Department
import com.example.a101_monitoring.data.model.DepartmentWithRooms
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
            onDepartmentsUpdate(it)
        })
        department_spinner.doAfterTextChanged {
            val departmentText = it.toString()
            registerPatientViewModel.departments.value?.also {
                val selectedDepartment = it.filter { it.department.name == departmentText }.firstOrNull()
                selectedDepartment?.also {
                    onSetDepartment(it)
                }
            }
        }
    }

    private fun onDepartmentsUpdate(departments: List<DepartmentWithRooms>) {
        val departmentNames = departments.map { it.department.name } as MutableList<String>
        department_spinner.setAdapter(ArrayAdapter(context, R.layout.material_dropdown_menu_popup_item, departmentNames).apply {
            setDropDownViewResource(R.layout.material_dropdown_menu_popup_item)
        })
        if (department_spinner.adapter.count > 0) {
            department_spinner.setText(department_spinner.adapter.getItem(0).toString(), false)
        }
    }

    private fun onSetDepartment(department: DepartmentWithRooms) {
        department.rooms.map { it.name }?.apply {
            room_spinner.setAdapter(ArrayAdapter(context, R.layout.material_dropdown_menu_popup_item, this).apply {
                setDropDownViewResource(R.layout.material_dropdown_menu_popup_item)
            })
            if (room_spinner.adapter.count > 0) {
                room_spinner.setText(room_spinner.adapter.getItem(0).toString(), false)
            }
        }
    }

    private fun initializeBeds() {
        registerPatientViewModel.getAvailableBeds().observe(viewLifecycleOwner, Observer {
            onRoomsUpdate(it)
        })
        room_spinner.doAfterTextChanged {
            val roomText = it.toString()
            registerPatientViewModel.departments.value?.also {
                val department = it.filter { it.department.name == department_spinner.text.toString() }.firstOrNull()
                department?.also {
                    val room = department.rooms.filter { it.name == roomText }.firstOrNull()
                    room?.apply {
                        registerPatientViewModel.updateAvailableBeds(this)
                    }
                }
            }
        }
    }

    private fun onRoomsUpdate(roomsNames: List<String>) {
        bed_spinner.setAdapter(ArrayAdapter(context, R.layout.material_dropdown_menu_popup_item, roomsNames).apply {
            setDropDownViewResource(R.layout.material_dropdown_menu_popup_item)
        })
        if (bed_spinner.adapter.count > 0) {
            bed_spinner.setText(bed_spinner.adapter.getItem(0).toString(), false)
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
        val department = getSelectedDepartment()
        if (isInputValid()) {
            registerPatientViewModel.registerPatient(
                registered_patient_id.text.toString(),
                getSelectedDepartment()!!.id,
                room_spinner.text.toString(),
                bed_spinner.text.toString(),
                patient_haiti.text.toString(),
                doctor.text.toString(),
                is_civilian_switch.isSelected,
                0,
                true
            )
        }
    }

    private fun isInputValid(): Boolean {
        return getSelectedDepartment() != null
    }

    fun getSelectedDepartment(): Department? {
        val departmentText = department_spinner.text.toString()
        return registerPatientViewModel.departments.value?.let {
             it.filter { it.department.name == departmentText }.firstOrNull()?.department
        }
    }

}
