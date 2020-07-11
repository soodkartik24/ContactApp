package com.kartik.contactapplicationkotlin.dagger

import androidx.room.Room
import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.persistence.Repository
import com.kartik.contactapplicationkotlin.persistence.local.ContactDao
import com.kartik.contactapplicationkotlin.persistence.local.ContactRoomDatabase
import com.kartik.contactapplicationkotlin.utils.Constants
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val application: ContactApplication) {
    @Provides
    @ApplicationScope
    fun providesApplication(): ContactApplication {
        return application
    }

    @Provides
    @ApplicationScope
    fun provideContactDao(contactRoomDatabase: ContactRoomDatabase): ContactDao {
        return contactRoomDatabase.contactDao()
    }

    @Provides
    @ApplicationScope
    fun provideContactDatabase(): ContactRoomDatabase {
        return Room.databaseBuilder(
            application,
            ContactRoomDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    @ApplicationScope
    fun provideContactRepository(contactDao: ContactDao): Repository {
        return Repository(contactDao)
    }
}