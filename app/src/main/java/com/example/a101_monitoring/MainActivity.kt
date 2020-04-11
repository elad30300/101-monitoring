package com.example.a101_monitoring

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.a101_monitoring.viewmodel.StatesViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

//    @Inject lateinit var statesViewModel: StatesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        (applicationContext as MyApplication).applicationComponent.inject(this)


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
