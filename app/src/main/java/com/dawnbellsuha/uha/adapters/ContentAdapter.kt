package com.dawnbellsuha.uha.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ItemContentBinding
import com.dawnbellsuha.uha.deskmodels.Content

class ContentAdapter(
    private val onContentClick: (Content) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    private var contentList = listOf<Content>()

    fun submitList(list: List<Content>) {
        contentList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemContentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(contentList[position])
    }

    override fun getItemCount() = contentList.size

    inner class ContentViewHolder(
        private val binding: ItemContentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(content: Content) {
            binding.contentTitle.text = content.title
            binding.contentDescription.text = content.description

            // Load thumbnail
            Glide.with(binding.root.context)
                .load(content.thumbnailUrl)
                .placeholder(getContentTypePlaceholder(content.type))
                .error(getContentTypePlaceholder(content.type))
                .centerCrop()
                .into(binding.contentThumbnail)

            // Set type icon
            binding.contentTypeIcon.setImageResource(getContentTypeIcon(content.type))

            binding.root.setOnClickListener {
                onContentClick(content)
            }
        }

        private fun getContentTypePlaceholder(type: String): Int {
            return when (type) {
                "notes" -> R.drawable.ic_notes_placeholder
                "books" -> R.drawable.ic_books_placeholder
                "quizzes" -> R.drawable.ic_quiz_placeholder
                "videos" -> R.drawable.ic_video_placeholder
                else -> R.drawable.ic_content_placeholder
            }
        }

        private fun getContentTypeIcon(type: String): Int {
            return when (type) {
                "notes" -> R.drawable.ic_notes
                "books" -> R.drawable.ic_books
                "quizzes" -> R.drawable.ic_quizzes
                "videos" -> R.drawable.ic_video
                else -> R.drawable.ic_content_placeholder
            }
        }
    }
}