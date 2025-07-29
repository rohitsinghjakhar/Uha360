package com.dawnbellsuha.uha.repo

import com.dawnbellsuha.uha.deskmodels.Board
import com.dawnbellsuha.uha.deskmodels.ClassModel
import com.dawnbellsuha.uha.deskmodels.Content
import com.dawnbellsuha.uha.deskmodels.Subject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getBoards(): List<Board> {
        return try {
            firestore.collection("boards")
                .orderBy("order")
                .get()
                .await()
                .toObjects(Board::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getClasses(boardId: String): List<ClassModel> {
        return try {
            firestore.collection("classes")
                .whereEqualTo("boardId", boardId)
                .orderBy("grade")
                .get()
                .await()
                .toObjects(ClassModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSubjects(classId: String): List<Subject> {
        return try {
            firestore.collection("subjects")
                .whereEqualTo("classId", classId)
                .get()
                .await()
                .toObjects(Subject::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getContent(subjectId: String, type: String): List<Content> {
        return try {
            firestore.collection("content")
                .whereEqualTo("subjectId", subjectId)
                .whereEqualTo("type", type)
                .get()
                .await()
                .toObjects(Content::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}