package com.example.a101_monitoring.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.R
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.di.component.PatientsListFragmentComponent
import com.example.a101_monitoring.ui.adapters.MyPatientRecyclerViewAdapter

import com.example.a101_monitoring.viewmodel.PatientsListViewModel
import kotlinx.android.synthetic.main.fragment_patient_list.*
import javax.inject.Inject

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [PatientsListFragment.OnListFragmentInteractionListener] interface.
 */
class PatientsListFragment : Fragment() {

    private lateinit var patientsListFragmentComponent: PatientsListFragmentComponent
    @Inject lateinit var patientsListViewModel: PatientsListViewModel

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_patient_list, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter
        list.apply {
            adapter = MyPatientRecyclerViewAdapter(
                patientsListViewModel.getPatients(),
                listener
            )
        }

        add_patient_button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.registerPatientFragment, null))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        }

        initializePatientsListComponent(context)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun initializePatientsListComponent(context: Context) {
        patientsListFragmentComponent = (context.applicationContext as MyApplication).applicationComponent
            .patientsListComponent()
            .create()
//            .also {
//                it.inject(this)
//            }
        patientsListFragmentComponent.inject(this)
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: Patient?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PatientsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
