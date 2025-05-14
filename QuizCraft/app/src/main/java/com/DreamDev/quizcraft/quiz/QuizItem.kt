package com.DreamDev.quizcraft.quiz

data class QuizItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val className: String = "",
    val createdBy: String = "",
    var isPublic: Boolean = true,
)

data class Question(
    val question: String = "",
    val option1: String = "",
    val option2: String = "",
    val option3: String = "",
    val option4: String = "",
    val correctAnswer: Int = 1
)


data class QuizItem2(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val className: String = "",
    val createdBy: String = "",
    var isPublic: Boolean = true,
    val questions: List<Question> = emptyList() // Add this
)
