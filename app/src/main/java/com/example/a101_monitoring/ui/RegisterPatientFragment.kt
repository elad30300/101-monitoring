package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Department
import com.example.a101_monitoring.data.model.DepartmentWithRooms
import com.example.a101_monitoring.di.component.RegisterPatientComponent
import com.example.a101_monitoring.states.RegisterPatientDoneState
import com.example.a101_monitoring.states.RegisterPatientFailedState
import com.example.a101_monitoring.states.RegisterPatientWorkingState
import com.example.a101_monitoring.viewmodel.RegisterPatientViewModel
import kotlinx.android.synthetic.main.fragment_patient.*
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

        login_button.setOnClickListener {
            navigateToSignInScreen()
        }

        initializeDepartments()

        initializeBeds()

        observerRegisterPatientState()
    }

    private fun observerRegisterPatientState() {
        registerPatientViewModel.getRegisterPatientState().observe(this, Observer {
            when(it.javaClass) {
                RegisterPatientDoneState::class.java -> onPatientRegisteredSuccessfully()
                RegisterPatientWorkingState::class.java -> onPatientRegisterWorking()
                RegisterPatientFailedState::class.java -> onPatientRegisterFailed()
            }
        })
    }

    private fun onPatientRegisteredSuccessfully() {
        register_patient_progress_bar.visibility = View.INVISIBLE
        fetchBedsForRoom()
//        view?.findNavController()?.popBackStack()
    }

    private fun onPatientRegisterWorking() {
        register_patient_progress_bar.visibility = View.VISIBLE
    }

    private fun onPatientRegisterFailed() {
        register_patient_progress_bar.visibility = View.INVISIBLE
    }

    private fun navigateToSignInScreen() {
        val action = RegisterPatientFragmentDirections.actionRegisterPatientFragmentToSignInPatientFragment()
        login_button.findNavController().navigate(action)
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

    private fun fetchBedsForRoom() {
        val roomText = room_spinner.text.toString()
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

    private fun onRegisterClicked(view: View) {
        if (isInputValid()) {
            registerPatientViewModel.registerPatient(
                registered_patient_id.text.toString().trim(),
                getSelectedDepartment()!!.id,
                room_spinner.text.toString().trim(),
                bed_spinner.text.toString().trim(),
                patient_haiti.text.toString().trim(),
                doctor.text.toString().trim(),
                is_civilian_switch.isSelected,
                0,
                true
            )
        } else {
            Toast.makeText(context, R.string.register_invalid_input_message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isInputValid(): Boolean = isIdInputValid()
                                            && isHaitiIdInputValid()
                                            && isDoctorIdInputValid()
                                            && isDepartmentInputValid()
                                            && isRoomInputValid()
                                            && isBedInputValid()

    private fun isIdInputValid(): Boolean = registered_patient_id.text.toString().trim().length == 9

    private fun isHaitiIdInputValid(): Boolean = patient_haiti.text.toString().trim().length == 4

    private fun isDoctorIdInputValid(): Boolean = doctor.text.toString().trim().length == 4

    private fun isDepartmentInputValid(): Boolean = getSelectedDepartment() != null

    private fun isBedInputValid(): Boolean = bed_spinner.text.toString().trim() != ""

    private fun isRoomInputValid(): Boolean = room_spinner.text.toString().trim() != ""

    private fun getSelectedDepartment(): Department? {
        val departmentText = department_spinner.text.toString()
        return registerPatientViewModel.departments.value?.let {
             it.filter { it.department.name == departmentText }.firstOrNull()?.department
        }
    }

}
