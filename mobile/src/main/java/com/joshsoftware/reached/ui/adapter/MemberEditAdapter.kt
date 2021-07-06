package com.joshsoftware.reached.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.joshsoftware.core.model.Member
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.viewholder.ViewHolder
import kotlinx.android.synthetic.main.edit_member_view.view.*

class MemberEditAdapter(val onClick: (Member, Int) -> Unit, val groupId: String) :
    ListAdapter<Member, ViewHolder>(MemberAdapter.DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.edit_member_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                if(groupId == model.id)
                    imgDelete.visibility = View.GONE

                imgDelete.setOnClickListener {
                    onClick(model, position)
                }
                txtMember.text = model.name
            }
        }
    }
}