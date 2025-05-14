package com.DreamDev.quizcraft.models

data class ClassItem(
    var classId: String = "",
    val className: String = "",
    val imageName: String = "",
    val createdBy: String = "",
    val classCode: String = "" // <-- Add this line
)
