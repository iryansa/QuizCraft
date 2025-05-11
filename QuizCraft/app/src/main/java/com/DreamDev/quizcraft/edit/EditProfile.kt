package com.DreamDev.quizcraft.edit

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.DreamDev.quizcraft.R

class EditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveChangesBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        nameEditText = findViewById(R.id.editName)
        emailEditText = findViewById(R.id.editEmail)
        passwordEditText = findViewById(R.id.editPassword)
        saveChangesBtn = findViewById(R.id.saveChangesBtn)

        // Set hints
        emailEditText.hint = currentUser.email
        fetchNameHint()

        saveChangesBtn.setOnClickListener {
            updateProfile()
            //end this screen to shift to previous one
            finish()

        }
    }

    private fun fetchNameHint() {
        val uid = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java)
            nameEditText.hint = name ?: "Name"
        }
    }

    private fun updateProfile() {
        val newName = nameEditText.text.toString().trim()
        val newEmail = emailEditText.text.toString().trim()
        val newPassword = passwordEditText.text.toString().trim()

        val uid = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        var changesMade = false

        // Update name
        if (!TextUtils.isEmpty(newName)) {
            userRef.child("name").setValue(newName)
            changesMade = true
        }

        // Update email
        if (!TextUtils.isEmpty(newEmail) && newEmail != currentUser.email) {
            currentUser.updateEmail(newEmail)
                .addOnSuccessListener {
                    userRef.child("email").setValue(newEmail)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            changesMade = true
        }

        // Update password
        if (!TextUtils.isEmpty(newPassword)) {
            currentUser.updatePassword(newPassword)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("EditProfile", "Password update failed: ${it.message}")
                    Toast.makeText(this, "Password update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            changesMade = true
        }

        if (changesMade) {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
        }
    }
}


//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.DreamDev.quizcraft.R
//
//class EditProfile : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_edit_profile)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}