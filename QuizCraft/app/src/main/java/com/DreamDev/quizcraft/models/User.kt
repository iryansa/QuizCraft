package com.DreamDev.quizcraft.models

data class User(val name: String = "",
                val email: String = "",
                val userId: String = "",
                var xp: Int = 0,
                var longestStreak: Int = 0)
