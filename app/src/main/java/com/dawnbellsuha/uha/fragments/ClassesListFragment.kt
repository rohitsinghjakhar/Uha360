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
import com.dawnbellsuha.uha.adapters.ClassesAdapter
import com.dawnbellsuha.uha.databinding.FragmentClassesListBinding
import com.dawnbellsuha.uha.deskmodels.ClassModel
import com.dawnbellsuha.uha.viewmodels.StudentDeskViewModel

class ClassesListFragment : Fragment() {

    private var _binding: FragmentClassesListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentDeskViewModel by viewModels()
    private lateinit var classesAdapter: ClassesAdapter

    private var boardId: String = ""
    private var boardName: String = ""

    companion object {
        private const val TAG = "ClassesListFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            boardId = it.getString("boardId", "")
            boardName = it.getString("boardName", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        observeViewModel()

        // Load classes for the selected board
        if (boardId.isNotEmpty()) {
            viewModel.loadClasses(boardId)
        }
    }

    private fun setupRecyclerView() {
        classesAdapter = ClassesAdapter { classModel ->
            navigateToSubjects(classModel)
        }

        binding.classesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = classesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = boardName.ifEmpty { "Select Class" }
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.classes.observe(viewLifecycleOwner) { classes ->
            Log.d(TAG, "Received ${classes.size} classes from Firebase")
            if (classes.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                classesAdapter.submitList(classes)
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

    private fun navigateToSubjects(classModel: ClassModel) {
        val fragment = SubjectsListFragment().apply {
            arguments = Bundle().apply {
                putString("classId", classModel.id)
                putString("className", classModel.name)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.classesRecyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.classesRecyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}