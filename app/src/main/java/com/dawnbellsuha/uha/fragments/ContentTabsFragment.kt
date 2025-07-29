package com.dawnbellsuha.uha.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.adapters.ContentPagerAdapter
import com.dawnbellsuha.uha.databinding.FragmentContentTabsBinding

class ContentTabsFragment : Fragment() {

    private var _binding: FragmentContentTabsBinding? = null
    private val binding get() = _binding!!

    private var subjectId: String = ""
    private var subjectName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectId = it.getString("subjectId", "")
            subjectName = it.getString("subjectName", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        binding.toolbar.title = subjectName.ifEmpty { "Content" }
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupViewPager() {
        val adapter = ContentPagerAdapter(this, subjectId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Notes"
                1 -> "Books"
                2 -> "Quizzes"
                3 -> "Videos"
                else -> ""
            }

            // Set icons for tabs
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_notes)
                1 -> tab.setIcon(R.drawable.ic_books)
                2 -> tab.setIcon(R.drawable.ic_quizzes)
                3 -> tab.setIcon(R.drawable.ic_video)
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}