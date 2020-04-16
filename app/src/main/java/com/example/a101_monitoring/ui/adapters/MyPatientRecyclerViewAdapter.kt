package com.example.a101_monitoring.ui.adapters

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.data.model.PatientIdentityFieldType


import com.example.a101_monitoring.ui.PatientsListFragment.OnListFragmentInteractionListener
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.viewmodel.PatientItemViewModel
import com.example.a101_monitoring.viewmodel.factory.PatientItemViewModelFactory
import com.google.android.material.button.MaterialButton

import kotlinx.android.synthetic.main.fragment_patient.view.*
import kotlinx.android.synthetic.main.sensor_choose_fragment.view.*


class MyPatientRecyclerViewAdapter(
    private val mValues: List<Patient>,
    private val mListener: OnListFragmentInteractionListener?,
    private val mFragment: Fragment
) : RecyclerView.Adapter<MyPatientRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Patient
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_patient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.apply {
            initializePatientItemViewModel(item.getIdentityField())

            mIdView.text = item.identityId

            setObserverConnectionToSensor()
            setObserverToHeartRate()
            setObserverToSaturation()
            setObserverToRespiratoryRate()

            setChooseSensorButtonActionListener(item.getIdentityField())

            with(mView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.patient_id
        val mSensorIndicator: ImageView = mView.sensor_connection_indicator
        val mSaturation: TextView = mView.saturation
        val mHeartRate: TextView = mView.heart_rate
        val mRespiratoryRate: TextView = mView.respiratory_rate
        val mChooseSensorButton: Button = mView.set_sensor_button

        lateinit var patientItemViewModel: PatientItemViewModel

        val indicatorBackgroundMap = mapOf<Boolean, Int>(
            true to R.drawable.rounded_green_rectangle,
            false to R.drawable.rounded_red_rectangle
        )

        fun initializePatientItemViewModel(patientId: PatientIdentityFieldType) {
            patientItemViewModel = ViewModelProviders.of(
                mFragment,
                PatientItemViewModelFactory(mView.context, patientId))
                .get(PatientItemViewModel::class.java)
        }

        fun setChooseSensorButtonActionListener(patientId: PatientIdentityFieldType) {
            mChooseSensorButton.setOnClickListener {
                val action = PatientsListFragmentDirections.actionPatientFragmentToSensorChooseFragment(patientId)
                it.findNavController().navigate(action)
            }
        }

        fun setObserverConnectionToSensor() {
            patientItemViewModel.isPatientConnectedToSensor.observe(mFragment.viewLifecycleOwner, Observer {
                val resourceId = (if (it == null) indicatorBackgroundMap[false] else indicatorBackgroundMap[it!!])
                mSensorIndicator.background = mView.context.resources.getDrawable(resourceId!!)
            })
        }

        fun setObserverToHeartRate() {
            patientItemViewModel.heartRate.observe(mFragment.viewLifecycleOwner, Observer {
                mHeartRate.text = if (it == null) "--" else it.value.toString()
            })
        }

        fun setObserverToSaturation() {
            patientItemViewModel.saturation.observe(mFragment.viewLifecycleOwner, Observer {
                mSaturation.text = if (it == null) "--" else it.value.toString()
            })
        }

        fun setObserverToRespiratoryRate() {
            patientItemViewModel.respiratoryRate.observe(mFragment.viewLifecycleOwner, Observer {
                mRespiratoryRate.text = it.firstOrNull()?.value?.toString() ?: "--"
            })
        }

//        override fun toString(): String {
//            return super.toString() + " '" + mContentView.text + "'"
//        }
    }
}
