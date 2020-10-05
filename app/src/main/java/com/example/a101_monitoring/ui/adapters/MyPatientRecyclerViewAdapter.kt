package com.example.a101_monitoring.ui.adapters

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.data.model.PatientIdentityFieldType


import com.example.a101_monitoring.ui.PatientsListFragment.OnListFragmentInteractionListener
import com.example.a101_monitoring.ui.PatientsListFragmentDirections
import com.example.a101_monitoring.ui.ReleasePatientDialogFragment
import com.example.a101_monitoring.viewmodel.PatientItemViewModel
import com.example.a101_monitoring.viewmodel.factory.PatientItemViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

import kotlinx.android.synthetic.main.fragment_patient.view.*
import kotlinx.android.synthetic.main.sensor_choose_fragment.*
import kotlinx.android.synthetic.main.sensor_choose_fragment.view.*


class MyPatientRecyclerViewAdapter(
    private val mValues: List<Patient>,
    private val mListener: OnListFragmentInteractionListener?,
    private val mFragment: Fragment
) : RecyclerView.Adapter<MyPatientRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private var editMode = MutableLiveData<Boolean>(false)

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Patient
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }


    }

    fun dismissEditMode() {
        editMode.value = false
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

            setReleaseButtonActionListener()

            observeEditMode()

            mView.setOnLongClickListener {
                editMode.value = true
                true
            }

            with(mView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }
    }

    fun getEditMode(): LiveData<Boolean> = editMode

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.patient_id
        val mSensorIndicator: ImageView = mView.sensor_connection_indicator
        val mSensorProgressBar: ProgressBar = mView.sensor_proccess_progress_bar
        val mSensorStatus: TextView = mView.sensor_procces_status_text_view
        val mSaturation: TextView = mView.saturation
        val mHeartRate: TextView = mView.heart_rate
        val mRespiratoryRate: TextView = mView.respiratory_rate
        val mChooseSensorButton: Button = mView.set_sensor_button
        val mReleaseButton: FloatingActionButton = mView.patient_item_release_button

        lateinit var patientItemViewModel: PatientItemViewModel

        val indicatorBackgroundMap = mapOf<Boolean, Int>(
            true to R.drawable.rounded_green_rectangle,
            false to R.drawable.rounded_red_rectangle
        )

        fun initializePatientItemViewModel(patientId: PatientIdentityFieldType) {
            patientItemViewModel = PatientItemViewModel(mFragment.context!!, patientId)
            Log.d(PatientListItemViewHolderTag, "view model - $patientItemViewModel")
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
                mSensorProgressBar.visibility = View.GONE
                mSensorStatus.visibility = View.GONE
            })
            patientItemViewModel.isPatientSensorScanning.observe(mFragment.viewLifecycleOwner, Observer {
                it?.also { scanning ->
                    val connecting = patientItemViewModel.isPatientSensorConnecting.value
                    val visibilityBool = if (connecting == null) scanning else (connecting!! || scanning)
                    mSensorProgressBar.visibility = if (visibilityBool) View.VISIBLE else View.GONE
                    mSensorStatus.visibility = if (visibilityBool) View.VISIBLE else View.GONE
                    if (scanning) {
                        mSensorStatus.text = "סורק"
                    }
                }
            })
            patientItemViewModel.isPatientSensorConnecting.observe(mFragment.viewLifecycleOwner, Observer {
                it?.also { connecting ->
                    val scanning = patientItemViewModel.isPatientSensorScanning.value
                    val visibilityBool = if (scanning == null) connecting else (scanning!! || connecting)
                    mSensorProgressBar.visibility = if (visibilityBool) View.VISIBLE else View.GONE
                    mSensorStatus.visibility = if (visibilityBool) View.VISIBLE else View.GONE
                    if (connecting) {
                        mSensorStatus.text = "מתחבר"
                    }
                }
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

        fun setReleaseButtonActionListener() {
            mReleaseButton.setOnClickListener {
                if (mView.tag is Patient && mFragment.activity != null) {
                    val patientId = (mView.tag as Patient).identityId
                    ReleasePatientDialogFragment(patientId).show(mFragment.activity!!.supportFragmentManager, "release")
                }
            }
        }

        fun observeEditMode() {
            editMode.observe(mFragment.viewLifecycleOwner, Observer {
                if (it) {
                    mView.setOnClickListener(null)
                    mReleaseButton.show()
                } else {
                    mView.setOnClickListener(mOnClickListener)
                    mReleaseButton.hide()
                }
            })
        }



//        override fun toString(): String {
//            return super.toString() + " '" + mContentView.text + "'"
//        }
    }

    companion object {
        private const val PatientListItemViewHolderTag = "PatientItemViewHolder"
    }

}
