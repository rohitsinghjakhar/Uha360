package com.uhadawnbells.uha.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.adapters.ContentPagerAdapter
import com.uhadawnbells.uha.databinding.FragmentContentTabsBinding

class ContentTabsFragment : Fragment() {
    private var _binding: FragmentContentTabsBinding? = null
    private val binding get() = _binding!!
    private lateinit var subjectId: String

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"
        private const val ARG_SUBJECT_NAME = "subject_name"

        fun newInstance(subjectId: String, subjectName: String): ContentTabsFragment {
            return ContentTabsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBJECT_ID, subjectId)
                    putString(ARG_SUBJECT_NAME, subjectName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectId = it.getString(ARG_SUBJECT_ID) ?: run {
                Log.e("ContentTabs", "Missing subjectId!")
                requireActivity().onBackPressed()
                return
            }
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
        binding.toolbar.title = arguments?.getString(ARG_SUBJECT_NAME) ?: "Content"
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
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_notes)
                1 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_books)
                2 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_quizzes)
                3 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_video)
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}