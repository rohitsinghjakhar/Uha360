package com.uhadawnbells.uha.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uhadawnbells.uha.databinding.ItemClassBinding
import com.uhadawnbells.uha.deskmodels.ClassModel

class ClassesAdapter(
    private val onClassClick: (ClassModel) -> Unit
) : RecyclerView.Adapter<ClassesAdapter.ClassViewHolder>() {

    private var classes = listOf<ClassModel>()

    fun submitList(list: List<ClassModel>) {
        classes = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding = ItemClassBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ClassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.bind(classes[position])
    }

    override fun getItemCount() = classes.size

    inner class ClassViewHolder(
        private val binding: ItemClassBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(classModel: ClassModel) {
            binding.className.text = classModel.name
            binding.classGrade.text = "Grade ${classModel.grade}"

            // Set gradient background based on position
            val gradientColors = listOf(
                intArrayOf(0xFF667eea.toInt(), 0xFF764ba2.toInt()),
                intArrayOf(0xFFf093fb.toInt(), 0xFFf5576c.toInt()),
                intArrayOf(0xFF4facfe.toInt(), 0xFF00f2fe.toInt()),
                intArrayOf(0xFF43e97b.toInt(), 0xFF38f9d7.toInt()),
                intArrayOf(0xFFfa709a.toInt(), 0xFFfee140.toInt()),
                intArrayOf(0xFF30cfd0.toInt(), 0xFF330867.toInt())
            )

            val position = adapterPosition % gradientColors.size
            binding.classGradientBackground.setGradientColors(gradientColors[position])

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
                        onClassClick(classModel)
                    }
                    .start()
            }
        }
    }
}