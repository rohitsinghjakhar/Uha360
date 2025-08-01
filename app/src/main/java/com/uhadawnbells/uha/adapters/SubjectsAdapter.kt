package com.uhadawnbells.uha.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.databinding.ItemSubjectBinding
import com.uhadawnbells.uha.deskmodels.Subject

class SubjectsAdapter(
    private val onSubjectClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    private var subjects = listOf<Subject>()

    fun submitList(list: List<Subject>) {
        subjects = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    override fun getItemCount() = subjects.size

    inner class SubjectViewHolder(
        private val binding: ItemSubjectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.subjectName.text = subject.name

            // Load subject icon
            Glide.with(binding.root.context)
                .load(subject.icon)
                .placeholder(getSubjectIcon(subject.name))
                .error(getSubjectIcon(subject.name))
                .into(binding.subjectIcon)

            // Set background color based on subject
            val backgroundResource = getSubjectBackground(subject.name)
            binding.subjectBackground.setBackgroundResource(backgroundResource)

            binding.root.setOnClickListener {
                Log.d("SubjectClick", "Clicked subject: ${subject.id}")
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
                        onSubjectClick(subject)
                    }
                    .start()
            }
        }

        // In your SubjectsAdapter
        private fun getSubjectIcon(subjectName: String): Int {
            return when (subjectName.lowercase()) {
                "mathematics", "maths", "math" -> R.drawable.ic_maths
                "science", "general science" -> R.drawable.ic_science
                "english", "english language", "english literature" -> R.drawable.ic_english
                "history", "social studies" -> R.drawable.ic_history
                "geography" -> R.drawable.ic_geography
                "physics" -> R.drawable.ic_physics
                "chemistry" -> R.drawable.ic_chemistry
                "biology", "life science" -> R.drawable.ic_biology
                "computer science", "computers", "it" -> R.drawable.ic_computer_science
                "economics" -> R.drawable.ic_economics
                "hindi", "sanskrit", "language" -> R.drawable.ic_language
                else -> R.drawable.ic_subject_placeholder
            }
        }


        private fun getSubjectBackground(subjectName: String): Int {
            return when (subjectName.lowercase()) {
                "mathematics", "maths" -> R.drawable.gradient_maths
                "science" -> R.drawable.gradient_science
                "english" -> R.drawable.gradient_english
                "history" -> R.drawable.gradient_history
                "geography" -> R.drawable.gradient_geography
                "physics" -> R.drawable.gradient_physics
                "chemistry" -> R.drawable.gradient_chemistry
                "biology" -> R.drawable.gradient_biology
                else -> R.drawable.gradient_default_subject
            }
        }
    }
}