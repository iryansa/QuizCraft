package com.DreamDev.quizcraft.edit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.DreamDev.quizcraft.Network.NetworkUtil
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.ui.profile.UserDatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class EditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var dbHelper: UserDatabaseHelper

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveChangesBtn: TextView

    private lateinit var syncReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        dbHelper = UserDatabaseHelper(this)

        nameEditText = findViewById(R.id.editName)
        emailEditText = findViewById(R.id.editEmail)
        passwordEditText = findViewById(R.id.editPassword)
        saveChangesBtn = findViewById(R.id.saveChangesBtn)

        emailEditText.hint = currentUser.email
        fetchNameHint()

        saveChangesBtn.setOnClickListener {
            updateProfile()
        }

        syncReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (NetworkUtil.isConnected(this@EditProfile)) {
                    syncPendingChangesToFirebase()
                }
            }
        }
        registerReceiver(syncReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(syncReceiver)
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

        val currentName = dbHelper.getUser()?.name ?: ""
        val currentEmail = currentUser.email ?: ""
        val currentXp = dbHelper.getUser()?.xp ?: 0
        val currentStreak = dbHelper.getUser()?.streak ?: 0

        val isOnline = NetworkUtil.isConnected(this)
        var changesMade = false

        val updatedName = if (!TextUtils.isEmpty(newName)) newName else currentName
        val updatedEmail = if (!TextUtils.isEmpty(newEmail)) newEmail else currentEmail

        val pendingSync = !isOnline
        dbHelper.insertOrUpdateUser(updatedName, updatedEmail, currentXp, currentStreak, pendingSync = pendingSync)
        changesMade = true

        if (!TextUtils.isEmpty(newPassword)) {
            currentUser.updatePassword(newPassword)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Password update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        if (!TextUtils.isEmpty(newEmail) && newEmail != currentUser.email) {
            currentUser.updateEmail(newEmail)
                .addOnSuccessListener {
                    // Also update Firebase Realtime Database if online
                    if (isOnline) {
                        updateFirebase(updatedName, updatedEmail)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else if (isOnline) {
            updateFirebase(updatedName, updatedEmail)
        }

        if (changesMade) {
            Toast.makeText(this, "Changes saved locally", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun updateFirebase(name: String, email: String) {
        val uid = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userRef.child("name").setValue(name)
        userRef.child("email").setValue(email)

        dbHelper.clearPendingSync()
        Toast.makeText(this, "Changes synced to Firebase", Toast.LENGTH_SHORT).show()
    }

    private fun syncPendingChangesToFirebase() {
        val userData = dbHelper.getUser()
        if (userData != null && userData.pendingSync) {
            updateFirebase(userData.name, userData.email)
        }
    }
}




//package com.DreamDev.quizcraft.edit
//
//import android.os.Bundle
//import android.text.TextUtils
//import android.util.Log
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.EmailAuthProvider
//import com.google.firebase.auth.FirebaseAuth


//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.FirebaseDatabase
//import com.DreamDev.quizcraft.R
//
//class EditProfile : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var currentUser: FirebaseUser
//
//    private lateinit var nameEditText: EditText
//    private lateinit var emailEditText: EditText
//    private lateinit var passwordEditText: EditText
//    private lateinit var saveChangesBtn: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_edit_profile)
//
//        auth = FirebaseAuth.getInstance()
//        currentUser = auth.currentUser!!
//
//        nameEditText = findViewById(R.id.editName)
//        emailEditText = findViewById(R.id.editEmail)
//        passwordEditText = findViewById(R.id.editPassword)
//        saveChangesBtn = findViewById(R.id.saveChangesBtn)
//
//        // Set hints
//        emailEditText.hint = currentUser.email
//        fetchNameHint()
//
//        saveChangesBtn.setOnClickListener {
//            updateProfile()
//            //end this screen to shift to previous one
//            finish()
//
//        }
//    }
//
//    private fun fetchNameHint() {
//        val uid = currentUser.uid
//        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
//        userRef.child("name").get().addOnSuccessListener { snapshot ->
//            val name = snapshot.getValue(String::class.java)
//            nameEditText.hint = name ?: "Name"
//        }
//    }
//
//    private fun updateProfile() {
//        val newName = nameEditText.text.toString().trim()
//        val newEmail = emailEditText.text.toString().trim()
//        val newPassword = passwordEditText.text.toString().trim()
//
//        val uid = currentUser.uid
//        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
//
//        var changesMade = false
//
//        // Update name
//        if (!TextUtils.isEmpty(newName)) {
//            userRef.child("name").setValue(newName)
//            changesMade = true
//        }
//
//        // Update email
//        if (!TextUtils.isEmpty(newEmail) && newEmail != currentUser.email) {
//            currentUser.updateEmail(newEmail)
//                .addOnSuccessListener {
//                    userRef.child("email").setValue(newEmail)
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this, "Email update failed: ${it.message}", Toast.LENGTH_SHORT).show()
//                }
//            changesMade = true
//        }
//
//        // Update password
//        if (!TextUtils.isEmpty(newPassword)) {
//            currentUser.updatePassword(newPassword)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener {
//                    Log.e("EditProfile", "Password update failed: ${it.message}")
//                    Toast.makeText(this, "Password update failed: ${it.message}", Toast.LENGTH_SHORT).show()
//                }
//            changesMade = true
//        }
//
//        if (changesMade) {
//            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
//
//
////
////import android.os.Bundle
////import androidx.activity.enableEdgeToEdge
////import androidx.appcompat.app.AppCompatActivity
////import androidx.core.view.ViewCompat
////import androidx.core.view.WindowInsetsCompat
////import com.DreamDev.quizcraft.R
////
////class EditProfile : AppCompatActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContentView(R.layout.activity_edit_profile)
////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
////            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
////            insets
////        }
////    }
////}