package com.dawnbellsuha.uha.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ItemBoardBinding
import com.dawnbellsuha.uha.deskmodels.Board

class BoardsAdapter(
    private val onBoardClick: (Board) -> Unit
) : RecyclerView.Adapter<BoardsAdapter.BoardViewHolder>() {

    private var boards = listOf<Board>()

    fun submitList(list: List<Board>) {
        boards = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding = ItemBoardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(boards[position])
    }

    override fun getItemCount() = boards.size

    inner class BoardViewHolder(
        private val binding: ItemBoardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(board: Board) {
            binding.boardName.text = board.name

            // Load board image with Glide
            Glide.with(binding.root.context)
                .load(board.thumbnail)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(binding.boardImage)

            // Add click animation
            binding.root.setOnClickListener {
                binding.cardView.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        binding.cardView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                        onBoardClick(board)
                    }
                    .start()
            }
        }
    }
}