package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.viewmodel.SignInPatientViewModel
import kotlinx.android.synthetic.main.sign_in_patient_fragment.*
import javax.inject.Inject

class SignInPatientFragment : Fragment() {

    companion object {
        fun newInstance() = SignInPatientFragment()
    }

    @Inject lateinit var viewModel: SignInPatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sign_in_patient_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_in_button.setOnClickListener {
            if (isInputValid()) {
                viewModel.signIn(sign_in_patient_id.text.toString())
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeDependencies(context)
    }

    private fun initializeDependencies(context: Context) {
        (context.applicationContext as MyApplication).applicationComponent.signInComponent().create().apply {
            inject(this@SignInPatientFragment)
        }
    }

    private fun isInputValid(): Boolean {
        return true
    }

}
