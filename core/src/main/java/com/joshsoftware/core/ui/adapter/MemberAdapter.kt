package com.joshsoftware.core.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.joshsoftware.core.FamViewHolder
import com.joshsoftware.core.R
import com.joshsoftware.core.model.Member
import kotlinx.android.synthetic.main.member_view.view.*

class MemberAdapter(var onClick: (Member) -> Unit): ListAdapter<Member, FamViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: FamViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                nameTextView.text = model.name
                model.profileUrl?.let {
                    Glide.with(this).load(it).into(profileImageView);
                }
            }
            holder.itemView.setOnClickListener {
                onClick(model)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Member> () {
            override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.member_view, parent, false)
        return FamViewHolder(view)
    }
}