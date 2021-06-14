package com.joshsoftware.reached.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.joshsoftware.core.model.Address
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.viewholder.ViewHolder
import kotlinx.android.synthetic.main.location_view.view.*

class AddressAdapter: ListAdapter<Address, ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Address>() {
            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                txtLocationName.text = model.name
                txtRadius.text = "${(model.radius.toFloat() / 1000.0)} KM"
                txtAddress.text = model.address
            }
        }
    }
}