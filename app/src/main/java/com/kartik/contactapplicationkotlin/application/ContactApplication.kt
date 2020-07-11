package com.kartik.contactapplicationkotlin.application

import android.app.Application
import com.kartik.contactapplicationkotlin.dagger.ApplicationComponent
import com.kartik.contactapplicationkotlin.dagger.ApplicationModule
import com.kartik.contactapplicationkotlin.dagger.DaggerApplicationComponent

class ContactApplication : Application() {
    companion object {
        private var applicationComponent: ApplicationComponent? = null

        fun getApplicationComponent(): ApplicationComponent? {
            return applicationComponent
        }

        fun setApplicationComponent(applicationComponent: ApplicationComponent?) {
            this.applicationComponent = applicationComponent
        }
    }

    override fun onCreate() {
        super.onCreate()

        setApplicationComponent(
            DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
        )
    }
}