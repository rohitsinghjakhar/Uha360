package com.dawnbellsuha.uha.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.adapters.SubjectsAdapter
import com.dawnbellsuha.uha.databinding.FragmentSubjectsListBinding
import com.dawnbellsuha.uha.deskmodels.Subject
import com.dawnbellsuha.uha.viewmodels.StudentDeskViewModel

class SubjectsListFragment : Fragment() {

    private var _binding: FragmentSubjectsListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentDeskViewModel by viewModels()
    private lateinit var subjectsAdapter: SubjectsAdapter

    private var classId: String = ""
    private var className: String = ""

    companion object {
        private const val TAG = "SubjectsListFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            classId = it.getString("classId", "")
            className = it.getString("className", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        observeViewModel()

        // Load subjects for the selected class
        if (classId.isNotEmpty()) {
            viewModel.loadSubjects(classId)
        }
    }

    private fun setupRecyclerView() {
        subjectsAdapter = SubjectsAdapter { subject ->
            navigateToContent(subject)
        }

        binding.subjectsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = subjectsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = className.ifEmpty { "Select Subject" }
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            Log.d(TAG, "Received ${subjects.size} subjects from Firebase")
            if (subjects.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                subjectsAdapter.submitList(subjects)
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

    private fun navigateToContent(subject: Subject) {
        val fragment = ContentTabsFragment().apply {
            arguments = Bundle().apply {
                putString("subjectId", subject.id)
                putString("subjectName", subject.name)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.subjectsRecyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.subjectsRecyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}