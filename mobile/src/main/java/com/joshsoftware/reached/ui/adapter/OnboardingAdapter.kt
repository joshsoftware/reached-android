package com.joshsoftware.reached.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.joshsoftware.reached.R
import com.joshsoftware.reached.model.OnboardingData
import com.joshsoftware.reached.ui.viewholder.ViewHolder
import kotlinx.android.synthetic.main.onboarding_view.view.*
import java.util.*

class OnboardingAdapter: ListAdapter<OnboardingData, ViewHolder>(DIFF_UTIL) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        if(model != null) {
            holder.itemView.apply {
                imgOnboarding.setImageResource(model.imageId)
                txtOnboarding.text = model.text
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
