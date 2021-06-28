package com.joshsoftware.reached.ui.adapter

import android.view.LayoutInflater
import android.view.View
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
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.viewholder.ViewHolder
import kotlinx.android.synthetic.main.group_menu_layout.view.*
import kotlinx.android.synthetic.main.home_view.*
import kotlinx.android.synthetic.main.home_view.view.*

class HomeAdapter(val sharedPreferences: AppSharedPreferences,
                  val onMemberClick: (Member, String) -> Unit,
                  val onMemberProfileClick: (Member, Group) -> Unit,
                  val onGroupEdit: (Group) -> Unit,
                  val onGroupDelete: (Group, Int) -> Unit,
                  val onAddMember: (Group) -> Unit): ListAdapter<Group, ViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            val adapter = MemberAdapter(sharedPreferences, {
                onMemberProfileClick(it, model)
            }) {
                onMemberClick(it, model.id!!)
            }
            holder.itemView.apply {

                viewTransparent.visibility = View.GONE
                groupMenuLayout.visibility = View.GONE
                nameTextView.text = model.name
                imgAddMember.setOnClickListener {  onAddMember(model) }
                memberRecyclerView.layoutManager  = LinearLayoutManager(context)
                memberRecyclerView.adapter = adapter
                val util = ConversionUtil()
                adapter.submitList(util.getMemberListFromMap(model.members))

                if(sharedPreferences.userId == model.created_by) {
                    txtDelete.text = context.getString(R.string.delete_group)
                } else {
                    txtDelete.text = context.getString(R.string.leave_group)
                }

                imgGroupMenu.setOnClickListener {
                    if(viewTransparent.visibility == View.GONE) {
                        viewTransparent.visibility = View.VISIBLE
                        groupMenuLayout.visibility = View.VISIBLE
                    } else {
                        viewTransparent.visibility = View.GONE
                        groupMenuLayout.visibility = View.GONE
                    }
                }

                txtDelete.setOnClickListener {
                    onGroupDelete(model, position)
                    viewTransparent.visibility = View.GONE
                    groupMenuLayout.visibility = View.GONE
                }

                txtEdit.setOnClickListener {
                    onGroupEdit(model)
                    viewTransparent.visibility = View.GONE
                    groupMenuLayout.visibility = View.GONE
                }
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