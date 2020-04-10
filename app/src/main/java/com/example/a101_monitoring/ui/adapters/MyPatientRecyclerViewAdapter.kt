package com.example.a101_monitoring.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Patient


import com.example.a101_monitoring.ui.PatientsListFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_patient.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPatientRecyclerViewAdapter(
    private val mValues: List<Patient>,
    private val mListener: OnListFragmentInteractionListener?
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
        holder.mIdView.text = item.id.toString()
        // TODO fix measurments values to the actual we get for the patient, fetch from viewmodel
        holder.mSaturation.text = "--"
        holder.mHeartRate.text = "--"
        holder.mRespiratoryRate.text = "--"


        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.patient_id
        val mSaturation: TextView = mView.saturation
        val mHeartRate: TextView = mView.heart_rate
        val mRespiratoryRate: TextView = mView.respiratory_rate

//        override fun toString(): String {
//            return super.toString() + " '" + mContentView.text + "'"
//        }
    }
}
