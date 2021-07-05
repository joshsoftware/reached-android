package com.joshsoftware.reached.ui

import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.FamViewHolder
import com.joshsoftware.core.R
import com.joshsoftware.core.model.Member
import kotlinx.android.synthetic.main.member_view.view.*

class MemberAdapter(var sharedPreferences: AppSharedPreferences, var onClick: (Member) -> Unit): ListAdapter<Member, FamViewHolder>(DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: FamViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                if(!TextUtils.isEmpty(model.id)) {
                    if(sharedPreferences.userId != null) {
                        if(model.id == sharedPreferences.userId) {
                            nameTextView.text = context.getString(R.string.me)
                        } else {
                            nameTextView.text = model.name
                        }
                    } else {
                        nameTextView.text = model.name
                    }
                    if(model.sosState) {
                        imgSos.visibility  =View.VISIBLE
                    } else {
                        imgSos.visibility  =View.GONE
                    }
                    if(model.lastKnownAddress.isNullOrEmpty()) {
                        placeTextView.text = "Enroute"
                    } else {
                        placeTextView.text = model.lastKnownAddress
                    }
                    model.profileUrl?.let {
                        Glide.with(this).load(it).into(imgProfile);
                    }
                } else {
                    nameTextView.text = "Show on map"
                    nameTextView.setTextColor(ContextCompat.getColor(context, R.color.colorOnPrimary))
                    imgProfile.visibility = View.GONE
                    nameTextView.gravity = Gravity.CENTER
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