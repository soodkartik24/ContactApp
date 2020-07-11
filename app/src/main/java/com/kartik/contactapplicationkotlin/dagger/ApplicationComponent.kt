package com.kartik.contactapplicationkotlin.dagger

import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.persistence.Repository
import dagger.Component

@Component(modules = [ApplicationModule::class])
@ApplicationScope
interface ApplicationComponent {
    fun getApplicationInstance(): ContactApplication
    fun getContactRepositoryInstance(): Repository
}