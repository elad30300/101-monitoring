package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.scope.ReleasePatientScope
import com.example.a101_monitoring.ui.ReleasePatientDialogFragment
import dagger.Subcomponent

@ReleasePatientScope
@Subcomponent
interface ReleasePatientComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ReleasePatientComponent
    }

    fun inject(releasePatientDialogFragment: ReleasePatientDialogFragment)
}