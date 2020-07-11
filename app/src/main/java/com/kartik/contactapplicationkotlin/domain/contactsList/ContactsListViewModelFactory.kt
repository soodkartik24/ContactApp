package com.kartik.contactapplicationkotlin.domain.contactsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.persistence.Repository

@Suppress("UNCHECKED_CAST")
class ContactsListViewModelFactory(
    private val repository: Repository,
    private val application: ContactApplication
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactsListViewModel(repository, application) as T
    }
}