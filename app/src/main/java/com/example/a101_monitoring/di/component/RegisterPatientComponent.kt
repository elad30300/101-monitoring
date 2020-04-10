package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.scope.RegisterPatientScope
import com.example.a101_monitoring.ui.RegisterPatientFragment
import dagger.Subcomponent

@RegisterPatientScope
@Subcomponent
interface RegisterPatientComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): RegisterPatientComponent
    }

    fun inject(registerPatientFragment: RegisterPatientFragment)
}