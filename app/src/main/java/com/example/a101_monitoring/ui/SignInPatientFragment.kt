package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.states.SignInPatientDoneState
import com.example.a101_monitoring.states.SignInPatientFailedState
import com.example.a101_monitoring.states.SignInPatientWorkingState
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
            } else {
                Toast.makeText(context, R.string.sign_in_invalid_input_message, Toast.LENGTH_LONG).show()
            }
        }

        observeSignInPatientState()
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

    private fun isInputValid() = isIdInputValid()

    private fun isIdInputValid() = sign_in_patient_id.text.toString().trim().length == 9

    private fun observeSignInPatientState() {
        viewModel.getSignInPatientState().observe(this, Observer {
            when(it.javaClass) {
                SignInPatientDoneState::class.java -> onPatientSignInedSuccessfully()
                SignInPatientWorkingState::class.java -> onPatientSignInWorking()
                SignInPatientFailedState::class.java -> onPatientSignInFailed()
            }
        })
    }

    private fun onPatientSignInedSuccessfully() {
        sign_in_patient_progress_bar.visibility = View.INVISIBLE
//        view?.findNavController()?.popBackStack()
    }

    private fun onPatientSignInWorking() {
        sign_in_patient_progress_bar.visibility = View.VISIBLE
    }

    private fun onPatientSignInFailed() {
        sign_in_patient_progress_bar.visibility = View.INVISIBLE
    }

}
