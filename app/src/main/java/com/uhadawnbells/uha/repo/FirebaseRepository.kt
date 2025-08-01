package com.uhadawnbells.uha.repo

import android.util.Log
import com.uhadawnbells.uha.deskmodels.Board
import com.uhadawnbells.uha.deskmodels.ClassModel
import com.uhadawnbells.uha.deskmodels.Content
import com.uhadawnbells.uha.deskmodels.Subject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseRepository"

    suspend fun getBoards(): List<Board> {
        return try {
            Log.d(TAG, "Fetching boards...")
            val result = firestore.collection("boards")
                .orderBy("order")
                .get()
                .await()

            result.documents.map { document ->
                Board(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    thumbnail = document.getString("thumbnail") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0
                ).also {
                    Log.d(TAG, "Loaded board: ${it.id} - ${it.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting boards", e)
            emptyList()
        }
    }

    suspend fun getClasses(boardId: String): List<ClassModel> {
        return try {
            Log.d(TAG, "Fetching classes for board: $boardId")
            val result = firestore.collection("classes")
                .whereEqualTo("boardId", boardId)
                .orderBy("grade")
                .get()
                .await()

            result.documents.map { document ->
                ClassModel(
                    id = document.id,
                    boardId = document.getString("boardId") ?: "",
                    grade = document.getLong("grade")?.toInt() ?: 0,
                    name = document.getString("name") ?: ""
                ).also {
                    Log.d(TAG, "Loaded class: ${it.id} - ${it.name} for board ${it.boardId}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting classes for board $boardId", e)
            emptyList()
        }
    }


    suspend fun getSubjects(classId: String): List<Subject> {
        return try {
            Log.d("FirebaseDebug", "Querying subjects for classId: $classId")
            val snapshot = firestore.collection("subjects")
                .whereEqualTo("classId", classId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    Subject(
                        id = document.id, // This is critical - using the document ID
                        classId = document.getString("classId") ?: "",
                        name = document.getString("name") ?: "",
                        icon = document.getString("icon") ?: ""
                    ).also {
                        Log.d("FirebaseDebug", "Mapped subject: ${it.id} - ${it.name}")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDebug", "Error mapping subject: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error getting subjects: ${e.message}")
            emptyList()
        }
    }

    suspend fun getContent(subjectId: String, type: String): List<Content> {
        return try {
            Log.d("FirebaseDebug", "Querying content for subject: $subjectId, type: $type")

            val query = firestore.collection("content")
                .whereEqualTo("subjectId", subjectId)
                .whereEqualTo("type", type)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .orderBy("title")

            val snapshot = query.get().await()

            Log.d("FirebaseDebug", "Found ${snapshot.size()} items")
            snapshot.documents.mapNotNull { doc ->
                try {
                    Content(
                        id = doc.id,
                        subjectId = doc.getString("subjectId") ?: "",
                        type = doc.getString("type") ?: "",
                        title = doc.getString("title") ?: "",
                        url = doc.getString("url") ?: "",
                        description = doc.getString("description") ?: "",
                        thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                        createdAt = doc.getDate("createdAt") ?: Date()
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseDebug", "Error mapping content: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error getting content: ${e.message}")
            emptyList()
        }
    }
}