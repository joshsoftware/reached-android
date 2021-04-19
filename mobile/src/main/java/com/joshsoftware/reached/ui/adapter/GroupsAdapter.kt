package com.joshsoftware.reached.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.joshsoftware.core.FamViewHolder
import com.joshsoftware.core.model.Group
import kotlinx.android.synthetic.main.group_view.view.*

class GroupsAdapter(var onClick: (Group) -> Unit): ListAdapter<Group, FamViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: FamViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                nameTextView.text = model.name
            }
            holder.itemView.setOnClickListener {
                onClick(model)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Group> () {
            override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.joshsoftware.reached.R.layout.group_view, parent, false)
        return FamViewHolder(view)
    }
}