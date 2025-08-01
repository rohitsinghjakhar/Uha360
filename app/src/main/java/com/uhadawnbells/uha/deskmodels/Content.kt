package com.uhadawnbells.uha.deskmodels

import java.util.Date

data class Content(
    val id: String = "",
    val subjectId: String = "",
    val type: String = "", // notes, books, quizzes, videos
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val createdAt: Date = Date()
)