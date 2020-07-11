package com.kartik.contactapplicationkotlin.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Contact(
    var name: String?,
    var number: String?,
    var email: String?,
    var profilePictureUrl: String?,
    @PrimaryKey val id: String
) : Serializable