package com.uhadawnbells.uha.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uhadawnbells.uha.deskmodels.Board
import com.uhadawnbells.uha.deskmodels.ClassModel
import com.uhadawnbells.uha.deskmodels.Content
import com.uhadawnbells.uha.deskmodels.Subject
import com.uhadawnbells.uha.repo.FirebaseRepository
import kotlinx.coroutines.launch

class StudentDeskViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _boards = MutableLiveData<List<Board>>()
    val boards: LiveData<List<Board>> = _boards

    private val _classes = MutableLiveData<List<ClassModel>>()
    val classes: LiveData<List<ClassModel>> = _classes

    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    private val _content = MutableLiveData<List<Content>>()
    val content: LiveData<List<Content>> = _content

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    companion object {
        private const val TAG = "StudentDeskViewModel"
    }

    fun loadBoards() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val boardsList = repository.getBoards()
                Log.d(TAG, "Loaded ${boardsList.size} boards from Firebase")
                _boards.value = boardsList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading boards", e)
                _error.value = "Failed to load boards: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadClasses(boardId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val classesList = repository.getClasses(boardId)
                Log.d(TAG, "Loaded ${classesList.size} classes for board: $boardId")
                _classes.value = classesList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading classes", e)
                _error.value = "Failed to load classes: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSubjects(classId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val subjectsList = repository.getSubjects(classId)
                Log.d(TAG, "Loaded ${subjectsList.size} subjects for class: $classId")
                _subjects.value = subjectsList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading subjects", e)
                _error.value = "Failed to load subjects: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadContent(subjectId: String, type: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val contentList = repository.getContent(subjectId, type)
                Log.d(TAG, "Loaded ${contentList.size} $type items for subject: $subjectId")
                _content.value = contentList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading content", e)
                _error.value = "Failed to load content: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    
}