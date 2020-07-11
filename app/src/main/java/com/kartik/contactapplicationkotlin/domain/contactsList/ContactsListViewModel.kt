package com.kartik.contactapplicationkotlin.domain.contactsList

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kartik.contactapplicationkotlin.persistence.Contact
import com.kartik.contactapplicationkotlin.persistence.Repository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class ContactsListViewModel @Inject internal constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    val showNoContactsPlaceHolder = ObservableBoolean()
    val showLoadingContactsPlaceHolder = ObservableBoolean()
    val showSearchView = ObservableBoolean()
    val searchViewText = ObservableField("")

    private val compositeDisposable = CompositeDisposable()
    var contact: MutableLiveData<List<Contact>> = MutableLiveData()
    private val map: HashMap<String, Contact> = HashMap()

    private var startingRow = 0
    private var rowsToLoad = 0
    var allLoaded = false
    private val contentResolver = application.contentResolver

    fun getContactsList(pageSize: Int) {
        compositeDisposable.add(
            Single.fromCallable { fetchContactsList(pageSize) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ o: Any? -> this.onSuccessContactResponse(o) }
                ) { obj: Throwable -> obj.printStackTrace() }
        )
    }

    private fun fetchContactsList(rowsPerLoad: Int): HashMap<String, Contact> {
        val cursor =
            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC"
            )
        if (cursor != null && !allLoaded) {
            val totalRows = cursor.count
            allLoaded = rowsToLoad == totalRows

            if (rowsToLoad < rowsPerLoad) {
                rowsToLoad = rowsPerLoad
            }
            if (totalRows < rowsToLoad) {
                rowsToLoad = totalRows
            }
            if (totalRows > 0) {
                for (i in startingRow until rowsToLoad) {
                    cursor.moveToPosition(i)
                    // get the contact's information
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val hasPhone =
                        cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val image: String? = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI)
                    )
                    var email: String? = null
                    val cursorEmail =
                        getCursorDataFromUri(ContactsContract.CommonDataKinds.Email.CONTENT_URI, id)
                    if (cursorEmail != null && cursorEmail.moveToFirst()) {
                        email =
                            cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                        cursorEmail.close()
                    }

                    // get the user's phone number
                    var phone: String? = null
                    if (hasPhone > 0) {
                        val cursorPhone = getCursorDataFromUri(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            id
                        )
                        if (cursorPhone != null && cursorPhone.moveToFirst()) {
                            phone =
                                cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            cursorPhone.close()
                        }
                    }

                    // if the user has an email or phone then add it to contacts
                    if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
                        val contactModel = Contact(name, phone, email, image, id)
                        if (!map.containsKey(phone)) map[phone] = contactModel
                    }
                }
            }

            startingRow = rowsToLoad
            if (rowsPerLoad > totalRows || rowsToLoad >= totalRows) {
                rowsToLoad = totalRows
            } else {
                if (totalRows - rowsToLoad <= rowsPerLoad) {
                    rowsToLoad = totalRows
                } else {
                    rowsToLoad += rowsPerLoad
                }
            }
            cursor.close()
        }
        return map
    }

    private fun onSuccessContactResponse(o: Any?) {
        if (o != null) {
            val contactHashMap: HashMap<String, Contact> = o as HashMap<String, Contact>
            val list = ArrayList(contactHashMap.values)
            insertContacts(list)
            contact.value =
                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name ?: "" })
        }
    }

    private fun insertContacts(contactList: List<Contact>) =
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertContacts(contactList)
        }

    private fun getCursorDataFromUri(uri: Uri, id: String): Cursor? {
        return contentResolver.query(
            uri,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(id),
            null
        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}