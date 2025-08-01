package com.uhadawnbells.uha.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.uhadawnbells.uha.adapters.ContentAdapter
import com.uhadawnbells.uha.databinding.FragmentContentListBinding
import com.uhadawnbells.uha.deskmodels.Content
import com.uhadawnbells.uha.player.PdfViewerActivity
import com.uhadawnbells.uha.player.VideoPlayerActivity
import com.uhadawnbells.uha.viewmodels.StudentDeskViewModel

class ContentListFragment : Fragment() {
    private var _binding: FragmentContentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentDeskViewModel by viewModels()
    private lateinit var contentAdapter: ContentAdapter

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"
        private const val ARG_CONTENT_TYPE = "content_type"

        fun newInstance(subjectId: String, contentType: String): ContentListFragment {
            return ContentListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBJECT_ID, subjectId)
                    putString(ARG_CONTENT_TYPE, contentType)
                }
            }
        }
    }

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

        val subjectId = arguments?.getString(ARG_SUBJECT_ID) ?: run {
            showErrorAndGoBack("Missing subject ID")
            return
        }

        if (subjectId.isEmpty()) {
            showErrorAndGoBack("Invalid subject ID")
            return
        }

        val contentType = arguments?.getString(ARG_CONTENT_TYPE) ?: run {
            showErrorAndGoBack("Missing content type")
            return
        }

        Log.d("ContentDebug", "Loading content for subject: $subjectId, type: $contentType")

        setupRecyclerView()
        observeViewModel()
        viewModel.loadContent(subjectId, contentType)
    }

    private fun observeViewModel() {
        viewModel.content.observe(viewLifecycleOwner) { contentList ->
            if (contentList.isEmpty()) {
                showEmptyState(arguments?.getString(ARG_CONTENT_TYPE) ?: "content")
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

    private fun showErrorAndGoBack(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun showEmptyState(contentType: String) {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.contentRecyclerView.visibility = View.GONE
        binding.emptyStateText.text = "No $contentType available yet"
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.contentRecyclerView.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentAdapter(
            onContentClick = { content ->
                handleContentClick(content)
            },
            requireContext()
        )

        binding.contentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contentAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleContentClick(content: Content) {
        when (content.type) {
            "notes", "books" -> {
                val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
                    putExtra("pdf_url", content.url)
                    putExtra("title", content.title)
                }
                startActivity(intent)
            }
            "videos" -> {
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
                    putExtra("video_url", content.url)
                    putExtra("title", content.title)
                }
                startActivity(intent)
            }
            "quizzes" -> {
                Toast.makeText(requireContext(), "Opening Quiz: ${content.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}