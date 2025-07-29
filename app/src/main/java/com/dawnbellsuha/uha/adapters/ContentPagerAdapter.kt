package com.dawnbellsuha.uha.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dawnbellsuha.uha.fragments.ContentListFragment

class ContentPagerAdapter(
    fragment: Fragment,
    private val subjectId: String
) : FragmentStateAdapter(fragment) {

    private val contentTypes = listOf("notes", "books", "quizzes", "videos")

    override fun getItemCount(): Int = contentTypes.size

    override fun createFragment(position: Int): Fragment {
        return ContentListFragment.newInstance(subjectId, contentTypes[position])
    }
}