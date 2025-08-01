package com.uhadawnbells.uha.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.adapters.BoardsAdapter
import com.uhadawnbells.uha.databinding.FragmentBoardsListBinding
import com.uhadawnbells.uha.deskmodels.Board
import com.uhadawnbells.uha.viewmodels.StudentDeskViewModel

class BoardsListFragment : Fragment() {

    private var _binding: FragmentBoardsListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentDeskViewModel by viewModels()
    private lateinit var boardsAdapter: BoardsAdapter

    companion object {
        private const val TAG = "BoardsListFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        observeViewModel()

        // Load boards from Firebase
        viewModel.loadBoards()
    }

    private fun setupRecyclerView() {
        boardsAdapter = BoardsAdapter { board ->
            navigateToClasses(board)
        }

        binding.boardsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = boardsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.boards.observe(viewLifecycleOwner) { boards ->
            Log.d(TAG, "Received ${boards.size} boards from Firebase")
            if (boards.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                boardsAdapter.submitList(boards)
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

    private fun navigateToClasses(board: Board) {
        Log.d("BoardsDebug", "Navigating with boardId: ${board.id}, name: ${board.name}")

        val fragment = ClassesListFragment().apply {
            arguments = Bundle().apply {
                // Make sure to use board.id, not board.name
                putString("boardId", board.id)  // THIS IS THE CRITICAL FIX
                putString("boardName", board.name)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.boardsRecyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.boardsRecyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}