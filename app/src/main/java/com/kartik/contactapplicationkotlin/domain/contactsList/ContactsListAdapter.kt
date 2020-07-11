package com.kartik.contactapplicationkotlin.domain.contactsList

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kartik.contactapplicationkotlin.R
import com.kartik.contactapplicationkotlin.databinding.ContactsListItemsBinding
import com.kartik.contactapplicationkotlin.interfaces.ItemClickListener
import com.kartik.contactapplicationkotlin.persistence.Contact

class ContactsListAdapter(
    var contactListResponse: ArrayList<Contact>? = null,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<ContactsListAdapter.ContactsViewHolder>(), Filterable {

    var filteredContactsList: ArrayList<Contact>? = null
    var searchByName = false
    var searchByNumber = false

    init {
        filteredContactsList = contactListResponse ?: ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val binding: ContactsListItemsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.contacts_list_items, parent, false
        )
        return ContactsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredContactsList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val name = filteredContactsList?.get(position)?.name ?: ""
        val phoneNumber = filteredContactsList?.get(position)?.number ?: ""
        holder.name.text = name
        holder.phoneNumber.text = phoneNumber
        holder.contactImageTextView.text =
            if (name.isNotEmpty()) name.substring(0, 1) else phoneNumber.substring(0, 1)
        holder.container.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
    }

    class ContactsViewHolder(binding: ContactsListItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val name = binding.name
        val phoneNumber = binding.phoneNumber
        val contactImageTextView = binding.contactImage
        val container = binding.container
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filteredContactsList = if (charString.isEmpty()) {
                    contactListResponse ?: ArrayList()
                } else {
                    val filteredList = ArrayList<Contact>()
                    for (contact in contactListResponse!!) {
                        if (searchByName && contact.name?.toLowerCase()
                                ?.contains(charString.toLowerCase(), true)!!
                        ) {
                            filteredList.add(contact)
                        } else if (searchByNumber && contact.number?.contains(charString)!!) {
                            filteredList.add(contact)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredContactsList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                filteredContactsList = filterResults.values as? ArrayList<Contact>
                notifyDataSetChanged()
            }
        }
    }
}

