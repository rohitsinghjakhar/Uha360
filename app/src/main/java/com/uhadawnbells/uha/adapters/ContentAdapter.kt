package com.uhadawnbells.uha.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.databinding.ItemContentBinding
import com.uhadawnbells.uha.deskmodels.Content
import com.uhadawnbells.uha.player.PdfViewerActivity
import com.uhadawnbells.uha.player.VideoPlayerActivity

class ContentAdapter(
    private val onContentClick: (Content) -> Unit,
    private val context: Context
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
        return ContentViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(contentList[position])
    }

    override fun getItemCount() = contentList.size

    inner class ContentViewHolder(
        private val binding: ItemContentBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(content: Content) {
            binding.contentTitle.text = content.title
            binding.contentDescription.text = content.description

            // Load thumbnail
            when (content.type) {
                "videos" -> {
                    Glide.with(binding.root.context)
                        .load(content.thumbnailUrl)
                        .placeholder(R.drawable.ic_video_placeholder)
                        .error(R.drawable.ic_video_placeholder)
                        .centerCrop()
                        .into(binding.contentThumbnail)
                }
                else -> {
                    binding.contentThumbnail.setImageResource(getContentTypePlaceholder(content.type))
                }
            }

            // Set type icon
            binding.contentTypeIcon.setImageResource(getContentTypeIcon(content.type))

            // Click handling
            binding.root.setOnClickListener {
                handleContentClick(content)
            }
        }

        private fun handleContentClick(content: Content) {
            when (content.type) {
                "quizzes", "books", "notes" -> {
                    val intent = Intent(context, PdfViewerActivity::class.java).apply {
                        putExtra("pdf_url", content.url)
                        putExtra("title", content.title)
                    }
                    context.startActivity(intent)
                }
                "videos" -> {
                    val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                        putExtra("video_url", content.url)
                        putExtra("title", content.title)
                    }
                    context.startActivity(intent)
                }
                else -> {
                    onContentClick(content)
                }
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