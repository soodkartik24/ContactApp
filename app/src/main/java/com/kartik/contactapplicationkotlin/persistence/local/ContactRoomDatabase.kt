package com.kartik.contactapplicationkotlin.persistence.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kartik.contactapplicationkotlin.persistence.Contact

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactRoomDatabase : RoomDatabase(){
    abstract fun contactDao(): ContactDao
}