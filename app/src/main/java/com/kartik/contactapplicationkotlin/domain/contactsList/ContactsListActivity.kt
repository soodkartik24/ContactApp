package com.kartik.contactapplicationkotlin.domain.contactsList

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartik.contactapplicationkotlin.R
import com.kartik.contactapplicationkotlin.application.ContactApplication
import com.kartik.contactapplicationkotlin.databinding.ActivityContactsListBinding
import com.kartik.contactapplicationkotlin.domain.contactDetails.ContactDetailsActivity
import com.kartik.contactapplicationkotlin.interfaces.ItemClickListener
import com.kartik.contactapplicationkotlin.persistence.Contact
import com.kartik.contactapplicationkotlin.utils.Constants


class ContactsListActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var contactListViewModel: ContactsListViewModel
    private lateinit var contactListBinding: ActivityContactsListBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: ContactsListAdapter
    private var arrayList: ArrayList<Contact> = ArrayList()
    private val contactItemsPageSize: Int = 10
    private var showOverflowMenu: Boolean = true
    private var loadContactList: Boolean = false

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, ContactsListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactListBinding = DataBindingUtil.setContentView(this, R.layout.activity_contacts_list)
        setUpViewModel()
        setSupportActionBar(contactListBinding.toolbar)
        supportActionBar?.title = getString(R.string.contacts)
        initContactsRecyclerView()
    }

    private fun setUpViewModel() {
        contactListViewModel = ViewModelProvider(
            this,
            ContactsListViewModelFactory(
                ContactApplication.getApplicationComponent()?.getContactRepositoryInstance()!!,
                ContactApplication.getApplicationComponent()?.getApplicationInstance()!!
            )
        ).get(ContactsListViewModel::class.java)
        contactListBinding.contactViewModel = contactListViewModel
    }

    private fun initContactsRecyclerView() {
        linearLayoutManager = LinearLayoutManager(this)
        contactListBinding.contactsRecyclerView.layoutManager = linearLayoutManager
        adapter = ContactsListAdapter(arrayList, this)
        contactListBinding.contactsRecyclerView.adapter = adapter
        contactListBinding.contactsRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastPosition = linearLayoutManager.findLastVisibleItemPosition()
                val arraySize = arrayList.size - 1
                if (!loadContactList && !contactListViewModel.allLoaded) {
                    loadContactList = true
                    contactListViewModel.getContactsList(contactItemsPageSize)
                }
            }
        })
        if (hasContactReadPermission()) {
            loadContactsList()
        } else {
            requestPermission()
        }
    }

    private fun loadContactsList() {
        contactListViewModel.showLoadingContactsPlaceHolder.set(true)
        contactListViewModel.getContactsList(contactItemsPageSize)
        contactListViewModel.contact.observe(
            this,
            Observer<List<Contact>> { contactModels ->
                run {
                    contactListViewModel.showLoadingContactsPlaceHolder.set(false)
                    arrayList.clear()
                    arrayList.addAll(contactModels)
                    adapter.notifyDataSetChanged()
                    loadContactList = false
                    contactListViewModel.showNoContactsPlaceHolder.set(arrayList.size == 0)
                }
            })
    }

    private fun hasContactReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS),
                Constants.Permissions.READ_CONTACT_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.Permissions.READ_CONTACT_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContactsList()
            } else {
                requestPermission()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.setGroupVisible(0, showOverflowMenu)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        adapter.searchByName = false
        adapter.searchByNumber = false
        when (item.itemId) {
            R.id.asc -> {
                sortContactsList(true)
            }
            R.id.desc -> {
                sortContactsList(false)
            }
            R.id.searchbyname -> {
                showSearchView(getString(R.string.search_by_name))
                adapter.searchByName = true
            }
            R.id.searchbynumber -> {
                showSearchView(getString(R.string.search_by_number))
                adapter.searchByNumber = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sortContactsList(sortListInAscOrder: Boolean) {
        val list = if (sortListInAscOrder) {
            arrayList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name ?: "" })
        } else {
            arrayList.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) {
                it.name ?: ""
            })
        }
        arrayList.clear()
        arrayList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    private fun showSearchView(name: String) {
        hideOverFlowMenu()
        contactListViewModel.showSearchView.set(true)
        contactListViewModel.searchViewText.set(name)
        contactListBinding.searchview.isFocusable = true
        contactListBinding.searchview.isIconified = false
        contactListBinding.searchview.requestFocusFromTouch()
        contactListBinding.searchview.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(text: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                adapter.filter.filter(text)
                return true
            }

        })
        contactListBinding.searchview.setOnCloseListener {
            showOverFLowMenu()
            contactListViewModel.showSearchView.set(false)
            true
        }
    }

    private fun showOverFLowMenu() {
        showOverflowMenu = true
        invalidateOptionsMenu()
    }

    private fun hideOverFlowMenu() {
        showOverflowMenu = false
        invalidateOptionsMenu()
    }

    override fun onItemClick(position: Int) {
        ContactDetailsActivity.startActivity(this, arrayList[position])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ContactDetailsActivity.ARG_UPDATE_CONTACT) {
            val contact =
                data?.extras?.getSerializable(ContactDetailsActivity.ARG_CONTACT_DATA) as? Contact
            for (index in 0 until arrayList.size - 1) {
                if (arrayList[index].id == contact?.id) {
                    arrayList[index].name = contact.name
                    arrayList[index].number = contact.number
                    arrayList[index].email = contact.email
                    adapter.notifyDataSetChanged()
                    break
                }
            }
        } else if (resultCode == ContactDetailsActivity.ARG_DELETE_CONTACT) {
            val contact =
                data?.extras?.getSerializable(ContactDetailsActivity.ARG_CONTACT_DATA) as? Contact
            for (index in 0 until arrayList.size - 1) {
                if (arrayList[index].id == contact?.id) {
                    arrayList.removeAt(index)
                    adapter.notifyDataSetChanged()
                    break
                }
            }
        }
    }
}