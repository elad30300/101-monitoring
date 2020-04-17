package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.scope.SignInPatientScope
import com.example.a101_monitoring.ui.SignInPatientFragment
import dagger.Subcomponent

@SignInPatientScope
@Subcomponent
interface SignInComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SignInComponent
    }

    fun inject(signInPatientFragment: SignInPatientFragment)
}