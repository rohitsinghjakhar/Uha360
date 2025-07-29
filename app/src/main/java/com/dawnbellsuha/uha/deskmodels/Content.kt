package com.dawnbellsuha.uha.deskmodels

data class Content(
    val id: String = "",
    val subjectId: String = "",
    val type: String = "", // notes, books, quizzes, videos
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val thumbnailUrl: String = ""
)