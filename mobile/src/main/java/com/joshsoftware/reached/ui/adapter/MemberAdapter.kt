package com.joshsoftware.reached.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.FamViewHolder
import com.joshsoftware.core.model.Member
import com.joshsoftware.reached.R
import kotlinx.android.synthetic.main.member_view.view.*

class MemberAdapter(
    var sharedPreferences: AppSharedPreferences,
    var onClick: (Member) -> Unit,
    var onMemberClick: (Member) -> Unit
) : ListAdapter<Member, FamViewHolder>(
    DIFF_CALLBACK
) {

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FamViewHolder, position: Int) {
        val model = getItem(position)
        if (model != null) {
            holder.itemView.apply {
                if (sharedPreferences.userId != null) {
                    if (model.id == sharedPreferences.userId) {
                        nameTextView.text = context.getString(R.string.me)
                    } else {
                        nameTextView.text = model.name
                    }
                } else {
                    nameTextView.text = model.name
                }
                if (model.sosState) {
                    containerCardView.strokeWidth = 4
                    containerCardView.strokeColor =
                        ContextCompat.getColor(context, R.color.colorAlert)
                } else {
                    containerCardView.strokeWidth = 0
                }
                if (model.lastKnownAddress.isNullOrEmpty()) {
                    placeTextView.text = "Enroute"
                } else {
                    placeTextView.text = model.lastKnownAddress
                }
                model.lastUpdated?.let { updatedText ->
//                    lastUpdatedTextView.text = dateTimeUtils.getLastUpdatedFormatted(updatedText)
                }
                model.profileUrl?.let {
                    if (it.isNotEmpty()) {
                        Glide.with(this).load(it).into(profileImageView);
                    }
                }
                profileImageView.setOnClickListener {
                    onMemberClick(model)
                }
            }
            holder.itemView.setOnClickListener {
                onClick(model)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Member>() {
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