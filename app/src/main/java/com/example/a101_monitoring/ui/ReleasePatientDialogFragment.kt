package com.example.a101_monitoring.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.a101_monitoring.MyApplication

import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.viewmodel.ReleasePatientDialogViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.release_patient_dialog_fragment.*
import kotlinx.android.synthetic.main.release_patient_dialog_fragment.view.*
import javax.inject.Inject

class ReleasePatientDialogFragment(
    private val patientId: PatientIdentityFieldType
) : DialogFragment() {

    private lateinit var mReleaseReasonsSpinner: AutoCompleteTextView
    private lateinit var mPasswordEditText: EditText
    private var mActivity: Activity? = null

    @Inject lateinit var viewModel: ReleasePatientDialogViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        initializeDependencies(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return activity?.let {
            mActivity = activity
            val builder = MaterialAlertDialogBuilder(it)
            val inflater = requireActivity().layoutInflater;
            builder.setView(inflater.inflate(R.layout.release_patient_dialog_fragment, null).also {
                initializeChildViews(it)
            })
                .setPositiveButton(R.string.release_dialog_positive_button_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        onReleaseButtonClicked()
                    })
                .setNegativeButton(R.string.release_dialog_negative_button_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            // Create the AlertDialog object and return it
            val dialog = builder.create()



            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initializeDependencies(context: Context) {
        (context.applicationContext as MyApplication).applicationComponent.releasePatientComponent().create().apply {
            inject(this@ReleasePatientDialogFragment)
        }
    }

    private fun initializeChildViews(view: View) {
        mReleaseReasonsSpinner = view.release_reason_spinner
        mPasswordEditText = view.release_password_text

        viewModel.releaseReasons.observe(activity!!, Observer {
            val releaseReasonsDescriptions = it.map { it.description }

            mReleaseReasonsSpinner.setAdapter(ArrayAdapter<String>(mActivity!!, R.layout.material_dropdown_menu_popup_item, releaseReasonsDescriptions).also {
                it.setDropDownViewResource(R.layout.material_dropdown_menu_popup_item)
            })
            if (mReleaseReasonsSpinner.adapter.count > 0) {
                mReleaseReasonsSpinner.setText(mReleaseReasonsSpinner.adapter.getItem(0) as String, false)
            }
        })
    }

    private fun onReleaseButtonClicked() {
        if (isInputValid()) {
            viewModel.releaseReasons.value?.also {
                val releaseReason = it.filter { it.description == mReleaseReasonsSpinner.text.toString() }.firstOrNull()
                releaseReason?.apply {
                    viewModel.releasePatient(patientId, this)
                } ?: Toast.makeText(context, R.string.no_release_reason_alert_message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, R.string.wrong_release_password_message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isInputValid() = viewModel.checkReleaseAccessPassword(mPasswordEditText.text.toString())
}


