package com.joshsoftware.reachedapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.model.OnboardingData
import com.joshsoftware.reachedapp.ui.viewholder.ViewHolder
import kotlinx.android.synthetic.main.onboarding_view.view.*

class OnboardingAdapter: ListAdapter<OnboardingData, ViewHolder>(DIFF_UTIL) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                imgBackground.setImageResource(model.backgroundImageId)
                imgOnboarding.setImageResource(model.imageId)
                txtOnboarding.text = model.text
                if(position == 2) {
                    txtSubOnboarding.visibility = View.VISIBLE
                } else {
                    txtSubOnboarding.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.onboarding_view, parent,
        false)
        return ViewHolder(view)
    }

    companion object {
        val DIFF_UTIL = object: DiffUtil.ItemCallback<OnboardingData>() {
            override fun areItemsTheSame(
                oldItem: OnboardingData,
                newItem: OnboardingData
            ): Boolean {
                return oldItem.imageId == newItem.imageId
            }

            override fun areContentsTheSame(
                oldItem: OnboardingData,
                newItem: OnboardingData
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}