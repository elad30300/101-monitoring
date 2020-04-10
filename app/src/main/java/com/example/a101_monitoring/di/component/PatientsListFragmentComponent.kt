package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.scope.PatientsListFragmentScope
import com.example.a101_monitoring.ui.PatientsListFragment
import dagger.Subcomponent

@PatientsListFragmentScope
@Subcomponent
interface PatientsListFragmentComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): PatientsListFragmentComponent
    }

    fun inject(patientsListFragment: PatientsListFragment)
}