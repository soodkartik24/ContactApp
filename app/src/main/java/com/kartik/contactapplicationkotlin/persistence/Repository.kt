package com.kartik.contactapplicationkotlin.persistence

import androidx.lifecycle.LiveData
import com.kartik.contactapplicationkotlin.persistence.local.ContactDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(private val contactDao: ContactDao) {

    suspend fun insertContacts(contactList: List<Contact>) = contactDao.insertContacts(contactList)

    suspend fun deleteContact(contact: Contact?) = contactDao.deleteContact(contact)

    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)
}