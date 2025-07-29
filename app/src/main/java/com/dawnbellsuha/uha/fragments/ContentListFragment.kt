package com.dawnbellsuha.uha.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dawnbellsuha.uha.adapters.ContentAdapter
import com.dawnbellsuha.uha.databinding.FragmentContentListBinding
import com.dawnbellsuha.uha.deskmodels.Content
import com.dawnbellsuha.uha.viewmodels.StudentDeskViewModel

class ContentListFragment : Fragment() {

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"
        private const val ARG_CONTENT_TYPE = "content_type"
        private const val TAG = "ContentListFragment"

        fun newInstance(subjectId: String, contentType: String): ContentListFragment {
            return ContentListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBJECT_ID, subjectId)
                    putString(ARG_CONTENT_TYPE, contentType)
                }
            }
        }
    }

    private var _binding: FragmentContentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentDeskViewModel by viewModels()
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        val subjectId = arguments?.getString(ARG_SUBJECT_ID) ?: ""
        val contentType = arguments?.getString(ARG_CONTENT_TYPE) ?: ""

        Log.d(TAG, "Loading content for subject: $subjectId, type: $contentType")
        viewModel.loadContent(subjectId, contentType)
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentAdapter { content ->
            handleContentClick(content)
        }

        binding.contentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contentAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.content.observe(viewLifecycleOwner) { contentList ->
            Log.d(TAG, "Received ${contentList.size} content items from Firebase")
            if (contentList.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                contentAdapter.submitList(contentList)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleContentClick(content: Content) {
        when (content.type) {
            "quizzes" -> {
                // Navigate to quiz activity
                Toast.makeText(requireContext(), "Opening Quiz: ${content.title}", Toast.LENGTH_SHORT).show()
                // TODO: Implement quiz navigation
            }
            "videos" -> {
                // Open video in player or YouTube
                if (content.url.contains("youtube.com") || content.url.contains("youtu.be")) {
                    openYouTubeVideo(content.url)
                } else {
                    openInBrowser(content.url)
                }
            }
            else -> {
                // Open notes/books in browser or PDF viewer
                openInBrowser(content.url)
            }
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openYouTubeVideo(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.youtube")
            startActivity(intent)
        } catch (e: Exception) {
            // If YouTube app is not installed, open in browser
            openInBrowser(url)
        }
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.contentRecyclerView.visibility = View.GONE

        val contentType = arguments?.getString(ARG_CONTENT_TYPE) ?: "content"
        binding.emptyStateText.text = "No $contentType available yet"
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.contentRecyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}