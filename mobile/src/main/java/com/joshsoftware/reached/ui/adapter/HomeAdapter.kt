package com.joshsoftware.reached.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.FamViewHolder
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.util.ConversionUtil
import com.joshsoftware.reached.ui.viewholder.ViewHolder
import kotlinx.android.synthetic.main.home_view.view.*

class HomeAdapter(val sharedPreferences: AppSharedPreferences,
                  val onMemberClick: (Member, String) -> Unit,
                  val onAddMember: (Group) -> Unit): ListAdapter<Group, ViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            val adapter = MemberAdapter(sharedPreferences) {
                onMemberClick(it, model.id!!)
            }
            holder.itemView.apply {
                nameTextView.text = model.name
                imgAddMember.setOnClickListener {  onAddMember(model) }
                memberRecyclerView.layoutManager  = LinearLayoutManager(context)
                memberRecyclerView.adapter = adapter
                val util = ConversionUtil()
                adapter.submitList(util.getMemberListFromMap(model.members))
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.joshsoftware.reached.R.layout.home_view, parent, false)
        return ViewHolder(view)
    }
}