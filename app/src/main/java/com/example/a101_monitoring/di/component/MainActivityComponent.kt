package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.MainActivity
import com.example.a101_monitoring.di.scope.MainActivityScope
import dagger.Subcomponent

@MainActivityScope
@Subcomponent
interface MainActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivityComponent
    }

    fun inject(mainActivity: MainActivity)
}