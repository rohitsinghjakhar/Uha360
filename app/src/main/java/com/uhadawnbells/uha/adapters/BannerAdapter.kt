package com.uhadawnbells.uha.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.models.BannerItem
import com.google.android.material.card.MaterialCardView

class BannerAdapter(
    private val banners: List<BannerItem>,
    private val onBannerClick: (BannerItem) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    companion object {
        private const val TAG = "BannerAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    override fun getItemCount(): Int = banners.size

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView = itemView.findViewById<MaterialCardView>(R.id.cv_banner)
        private val imageView = itemView.findViewById<ImageView>(R.id.iv_banner)
        private val textView = itemView.findViewById<TextView>(R.id.tv_banner_text)

        fun bind(banner: BannerItem) {
            Log.d(TAG, "🖼️ Loading banner: ${banner.text} with image: ${banner.imageUrl}")

            // Load image with better error handling
            Glide.with(itemView.context)
                .load(banner.imageUrl)
                .placeholder(R.drawable.banner_rounded_background)
                .error(R.drawable.banner_rounded_background)
                .centerCrop()
                .timeout(10000) // 10 second timeout
                .into(imageView)

            // Set banner text
            textView.text = banner.text

            // Set click listener
            cardView.setOnClickListener {
                Log.d(TAG, "🖱️ Banner clicked: ${banner.text}")
                onBannerClick(banner)
            }
        }
    }
}