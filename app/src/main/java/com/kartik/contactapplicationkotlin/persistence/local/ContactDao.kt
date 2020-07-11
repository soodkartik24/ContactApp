package com.kartik.contactapplicationkotlin.persistence.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kartik.contactapplicationkotlin.persistence.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact ORDER BY name ASC")
    fun getAllContacts(): LiveData<List<Contact>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contact: List<Contact>)

    @Delete
    suspend fun deleteContact(contact: Contact?)

    @Update
    suspend fun updateContact(contact: Contact)

    @Query("SELECT * from contact WHERE name LIKE '%' || :query || '%'")
    fun findContactByName(query: String): LiveData<List<Contact>>

    @Query("SELECT * FROM contact WHERE number = :number")
    fun getContactByPhoneNumber(number: String): LiveData<Contact?>
}