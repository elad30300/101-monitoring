package com.example.a101_monitoring

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.ui.PatientsListFragment
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.viewmodel.StatesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), PatientsListFragment.OnListFragmentInteractionListener {

//    @Inject lateinit var statesViewModel: StatesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        (applicationContext as MyApplication).applicationComponent.inject(this)


    }

    override fun onListFragmentInteraction(item: Patient?) {
        item?.apply {
            val action = PatientsListFragmentDirections.actionPatientFragmentToPatientManualMeasurmentsFragment(id)
            nav_host_fragment.findNavController().navigate(action) // TODO check if this is correct implementation
        }
    }

//    private fun setStatesObservers() {
//        statesViewModel.registerPatientState.observe(this, Observer {
//            if (!it) {
//                runOnUiThread {
//                    Toast.makeText(this, "רישום מטופל נכשל", Toast.LENGTH_SHORT).show()
//                }
//            }
//        })
//    }

}
