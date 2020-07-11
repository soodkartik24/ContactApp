package com.kartik.contactapplicationkotlin.domain.contactDetails

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import com.kartik.contactapplicationkotlin.R
import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.receivers.UpdateContactReceiver
import com.kartik.contactapplicationkotlin.databinding.ActivityContactDetailsBinding
import com.kartik.contactapplicationkotlin.domain.contactsList.ContactsListActivity
import com.kartik.contactapplicationkotlin.persistence.Contact
import com.kartik.contactapplicationkotlin.utils.Constants
import com.squareup.picasso.Picasso
import java.io.IOException

class ContactDetailsActivity : AppCompatActivity() {
    private lateinit var contactDetailsViewModel: ContactDetailsViewModel
    private lateinit var contactDetailsBinding: ActivityContactDetailsBinding
    private lateinit var contactDetails: Contact

    companion object {
        const val ARG_CONTACT_DATA = "contactItem"
        const val ARG_UPDATE_CONTACT = 100
        const val ARG_DELETE_CONTACT = 101

        fun startActivity(activity: AppCompatActivity, contact: Contact) {
            val intent = Intent(activity, ContactDetailsActivity::class.java)
            intent.putExtra(ARG_CONTACT_DATA, contact)
            activity.startActivityForResult(intent, ARG_UPDATE_CONTACT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactDetailsBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_contact_details
        )
        contactDetails = intent.extras?.getSerializable(ARG_CONTACT_DATA) as Contact
        setUpViewModel()
        setProfileImage()
        requestPermission()
    }

    private fun setUpViewModel() {
        contactDetailsViewModel =
            ViewModelProvider(
                this,
                ContactDetailsViewModelFactory(
                    ContactApplication.getApplicationComponent()?.getContactRepositoryInstance()!!,
                    ContactApplication.getApplicationComponent()?.getApplicationInstance()!!,
                    contactDetails
                )
            ).get(
                ContactDetailsViewModel::class.java
            )
        contactDetailsBinding.viewModel = contactDetailsViewModel
        contactDetailsViewModel.showContactInformation()

        contactDetailsViewModel.contactUpdate.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (contactDetailsViewModel.contactUpdate.get()) {
                    Toast.makeText(
                        this@ContactDetailsActivity,
                        "Contact detail updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateAndDeleteContactDetailsAndGetBackToPreviousActivity(ARG_UPDATE_CONTACT)
                } else {
                    Toast.makeText(
                        this@ContactDetailsActivity, "Unable to update contact", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        contactDetailsViewModel.contactDelete.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (contactDetailsViewModel.contactDelete.get()) {
                    Toast.makeText(
                        this@ContactDetailsActivity,
                        "Contact deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateAndDeleteContactDetailsAndGetBackToPreviousActivity(ARG_DELETE_CONTACT)
                } else {
                    Toast.makeText(
                        this@ContactDetailsActivity,
                        "Unable to delete contact",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setProfileImage() {
        contactDetails.profilePictureUrl?.let {
            try {
                val uri = Uri.parse(it)
                Picasso.get().load(uri).into(contactDetailsBinding.contactProfileImage)
            } catch (e: IOException) {
                e.printStackTrace()
                Picasso.get().load(R.drawable.account_circle_black)
                    .into(contactDetailsBinding.contactProfileImage)
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_CONTACTS),
                Constants.Permissions.WRITE_CONTACT_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateAndDeleteContactDetailsAndGetBackToPreviousActivity(resultCode: Int) {
        contactDetails.name = contactDetailsViewModel.name.get()
        contactDetails.number = contactDetailsViewModel.phoneNumber.get()
        contactDetails.email = contactDetailsViewModel.email.get()
        if (resultCode == ARG_UPDATE_CONTACT) {
            sendBroadcast(Intent(this, UpdateContactReceiver::class.java))
        }
        val intent =
            Intent(this@ContactDetailsActivity, ContactsListActivity::class.java)
        intent.putExtra(ARG_CONTACT_DATA, contactDetails)
        setResult(resultCode, intent)
        finish()
    }
}