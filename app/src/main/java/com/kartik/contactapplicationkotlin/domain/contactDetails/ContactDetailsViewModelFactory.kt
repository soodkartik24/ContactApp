package com.kartik.contactapplicationkotlin.domain.contactDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.persistence.Contact
import com.kartik.contactapplicationkotlin.persistence.Repository

@Suppress("UNCHECKED_CAST")
class ContactDetailsViewModelFactory(
    private val repository: Repository,
    private val application: ContactApplication,
    private val contact: Contact
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactDetailsViewModel(repository,application, contact) as T
    }
}