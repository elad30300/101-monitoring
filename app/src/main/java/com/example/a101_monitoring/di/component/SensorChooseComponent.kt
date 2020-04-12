package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.scope.SensorChooseScope
import com.example.a101_monitoring.ui.SensorChooseFragment
import dagger.Subcomponent

@SensorChooseScope
@Subcomponent
interface SensorChooseComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SensorChooseComponent
    }

    fun inject(sensorChooseFragment: SensorChooseFragment)
}