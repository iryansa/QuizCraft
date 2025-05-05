package com.DreamDev.quizcraft.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.edit.EditProfile
import com.DreamDev.quizcraft.login.Login

class Profile : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find your edit_profile ImageView
        val editProfileBtn = view.findViewById<ImageView>(R.id.edit_profile)

        // Set onClickListener to open EditProfile Activity
        editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }

        // Find your logout_button
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

// Set onClickListener to navigate to LoginActivity and clear navigation stack
        logoutButton.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}
