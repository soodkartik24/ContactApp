package com.kartik.contactapplicationkotlin.domain.contactDetails

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.kartik.contactapplicationkotlin.R
import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.persistence.Contact
import com.kartik.contactapplicationkotlin.persistence.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class ContactDetailsViewModel @Inject internal constructor(
    private val repository: Repository,
    private val application: ContactApplication,
    private val contact: Contact
) : AndroidViewModel(application) {

    val isEdit = ObservableBoolean()
    val name = ObservableField("")
    val phoneNumber = ObservableField("")
    val email = ObservableField("")
    val contactUpdate = ObservableBoolean()
    val contactDelete = ObservableBoolean()

    private val DATA_COLS = arrayOf(
        ContactsContract.Data.MIMETYPE,
        ContactsContract.Data.DATA1,  //phone number
        ContactsContract.Data.CONTACT_ID
    )

    fun showContactInformation() {
        name.set(contact.name)
        phoneNumber.set(contact.number)
        email.set(contact.email ?: application.getString(R.string.not_available))
    }

    fun editContact(editContact: Boolean) {
        isEdit.set(editContact)
    }

    fun updateContactDetails() {
        //selection for name
        var where = String.format(
            "%s = '%s' AND %s = ?",
            DATA_COLS[0],  //mimetype
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            DATA_COLS[2] /*contactId*/
        )

        val args = arrayOf(contact.id)

        val operations: ArrayList<ContentProviderOperation> = ArrayList()

        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, args)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name.get())
                .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, "")
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, "")
                .build()
        )

        //change selection for number
        where = String.format(
            "%s = '%s' AND %s = ?",
            DATA_COLS[0],//mimetype
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            DATA_COLS[1]/*number*/
        )

        //change args for number
        args[0] = contact.number ?: ""

        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, args)
                .withValue(DATA_COLS[1]/*number*/, phoneNumber.get())
                .build()
        )

        try {
            val results: Array<ContentProviderResult> =
                application.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            if (results.isNotEmpty() && results[0].count == 0) {
                contactUpdate.set(true)
                contact.name = name.get()
                contact.number = phoneNumber.get()
                updateContact(contact)
            } else {
                contactUpdate.set(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            contactUpdate.set(false)
        }
    }

    @SuppressLint("Recycle")
    fun deleteContact() {
        val contactUri: Uri =
            Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact.number))
        val cur: Cursor = application.contentResolver.query(contactUri, null, null, null, null)!!
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME)) == name.get()) {
                        val lookupKey: String =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                        val uri: Uri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                            lookupKey
                        )
                        application.contentResolver.delete(uri, null, null)
                    }
                } while (cur.moveToNext())
            }
            contactDelete.set(true)
        } catch (e: java.lang.Exception) {
            contactDelete.set(false)
        }
        cur.close()
        deleteContact(contact)
    }

    private fun deleteContact(contact: Contact) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteContact(contact)
        }
    }

    private fun updateContact(contact: Contact) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateContact(contact)
        }
    }
}