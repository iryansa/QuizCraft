package com.DreamDev.quizcraft.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.edit.EditProfile
import com.DreamDev.quizcraft.login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Profile : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var quizCompleted: TextView
    private lateinit var totalXp: TextView
    private lateinit var quizStreak: TextView
    private lateinit var ranking: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profileName = view.findViewById(R.id.profile_name)
        profileEmail = view.findViewById(R.id.profile_email)
        quizCompleted = view.findViewById(R.id.quiz_completed)
        totalXp = view.findViewById(R.id.total_xp)
        quizStreak = view.findViewById(R.id.quiz_streak)
        ranking = view.findViewById(R.id.ranking)

        val editProfileBtn = view.findViewById<ImageView>(R.id.edit_profile)
        editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }

        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                        val email = snapshot.child("email").getValue(String::class.java) ?: "N/A"
                        val xp = snapshot.child("xp").getValue(Int::class.java) ?: 0
                        val longestStreak = snapshot.child("longestStreak").getValue(Int::class.java) ?: 0

                        // Set fetched data to the views
                        profileName.text = name
                        profileEmail.text = email
                        totalXp.text = "Total XP: $xp"
                        quizStreak.text = "Quiz Streak: $longestStreak"
                        quizCompleted.text = "Quizzes Completed: Coming Soon"
                        ranking.text = "Ranking: Coming Soon"
                    } else {
                        Toast.makeText(requireContext(), "User data not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
