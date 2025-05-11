package com.DreamDev.quizcraft.models

data class User(val name: String = "",
                val email: String = "",
                val userId: String = "",
                val xp: Int = 0,
                val longestStreak: Int = 0)
