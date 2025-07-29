package com.dawnbellsuha.uha.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dawnbellsuha.uha.databinding.ItemOnboardingBinding

class OnboardingAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(private val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            binding.imageView.setImageResource(item.imageResId)
            binding.titleText.text = item.title
            binding.descriptionText.text = item.description
            binding.root.setBackgroundResource(item.backgroundColorResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
