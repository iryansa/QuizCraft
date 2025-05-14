package com.DreamDev.quizcraft.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class Profile : Fragment() {
    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var quizCompleted: TextView
    private lateinit var totalXp: TextView
    private lateinit var quizStreak: TextView
    private lateinit var ranking: TextView

    private lateinit var profileImageView: ImageView
    private var imageUri: Uri? = null
    private val client = OkHttpClient()

    // Replace with your XAMPP/PHP endpoint
    private val imageLoadUrl = "http://192.168.166.32/quizcraft/get_profile_image.php?uid="
    private val imageUploadUrl = "http://192.168.166.32/quizcraft/upload_profile_image.php"

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let {
                profileImageView.setImageURI(it)
                uploadImageToServer(it)
            }
        }
    }


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

        // Add this line
        profileImageView = view.findViewById(R.id.profile_image)

        profileImageView.setOnLongClickListener {
            pickImageFromGallery()
            true
        }

        loadProfileImage()

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

        dbHelper = UserDatabaseHelper(requireContext())
        loadLocalProfile()
        loadUserProfile() // Firebase


        return view
    }
    private fun loadLocalProfile() {
        val localUser = dbHelper.getUser()
        localUser?.let {
            profileName.text = it.name
            profileEmail.text = it.email
            totalXp.text = "Total XP: ${it.xp}"
            quizStreak.text = "Quiz Streak: ${it.streak}"
            quizCompleted.text = "Quizzes Completed: Coming Soon"
            ranking.text = "Ranking: Coming Soon"
        }
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
                        dbHelper.insertOrUpdateUser(name, email, xp, longestStreak)

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

    private fun loadProfileImage() {
        val url = imageLoadUrl + '1'

        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.account_placeholder)
            .error(R.drawable.account_placeholder)
            .into(profileImageView)
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun uploadImageToServer(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val formBody = FormBody.Builder()
            .add("uid", uid)
            .add("image", encodedImage)
            .build()

        val request = Request.Builder()
            .url(imageUploadUrl)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Upload failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Image updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}
