package com.uhadawnbells.uha.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val contentRef = storage.reference.child("content")

    // Define content types
    enum class ContentType {
        NOTES, BOOKS, QUIZZES, VIDEOS
    }

    /**
     * Upload file to Firebase Storage
     * @param fileUri Local file URI
     * @param subjectId Subject ID this content belongs to
     * @param contentType Type of content (notes, books, etc.)
     * @param title Title of the content
     * @param description Description of the content
     * @param context Android context
     * @return Download URL of the uploaded file
     */
    suspend fun uploadContent(
        fileUri: Uri,
        subjectId: String,
        contentType: ContentType,
        context: Context,
        title: String,
        description: String = ""
    ): String {
        // Generate unique filename
        val fileExtension = getFileExtension(fileUri, context)
        val fileName = "${UUID.randomUUID()}.$fileExtension"

        // Determine storage path based on content type
        val storagePath = when (contentType) {
            ContentType.NOTES -> "notes/$subjectId/$fileName"
            ContentType.BOOKS -> "books/$subjectId/$fileName"
            ContentType.QUIZZES -> "quizzes/$subjectId/$fileName"
            ContentType.VIDEOS -> "videos/$subjectId/$fileName"
        }

        val fileRef = contentRef.child(storagePath)

        // Upload file
        val uploadTask = fileRef.putFile(fileUri).await()

        // Get download URL
        return fileRef.downloadUrl.await().toString()
    }

    /**
     * Get all content URLs for a specific subject and content type
     */
    suspend fun getContentUrls(
        subjectId: String,
        contentType: ContentType
    ): List<String> {
        val path = when (contentType) {
            ContentType.NOTES -> "notes/$subjectId"
            ContentType.BOOKS -> "books/$subjectId"
            ContentType.QUIZZES -> "quizzes/$subjectId"
            ContentType.VIDEOS -> "videos/$subjectId"
        }

        val listRef = contentRef.child(path)
        val result = listRef.listAll().await()

        return result.items.map { it.downloadUrl.await().toString() }
    }

    private fun getFileExtension(uri: Uri, context: Context): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"
    }
}