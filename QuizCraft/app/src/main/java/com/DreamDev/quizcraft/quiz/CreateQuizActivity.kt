package com.DreamDev.quizcraft.quiz


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.DreamDev.quizcraft.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class CreateQuizActivity : AppCompatActivity() {

    private lateinit var totalQuestionsInput: EditText
    private lateinit var questionInput: EditText
    private lateinit var option1Input: EditText
    private lateinit var option2Input: EditText
    private lateinit var option3Input: EditText
    private lateinit var option4Input: EditText
    private lateinit var correctAnswerInput: EditText
    private lateinit var nextButton: Button
    private lateinit var submitButton: Button
    private lateinit var quizname: String
    private var totalQuestions = 0
    private var currentQuestion = 0

    private val questionsList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        totalQuestionsInput = findViewById(R.id.total_questions_input)
        questionInput = findViewById(R.id.question_input)
        option1Input = findViewById(R.id.option1_input)
        option2Input = findViewById(R.id.option2_input)
        option3Input = findViewById(R.id.option3_input)
        option4Input = findViewById(R.id.option4_input)
        correctAnswerInput = findViewById(R.id.correct_answer_input)
        nextButton = findViewById(R.id.next_question_button)
        submitButton = findViewById(R.id.submit_quiz_button)

        nextButton.setOnClickListener {
            if (currentQuestion == 0) {
                val totalInput = totalQuestionsInput.text.toString()
                if (totalInput.isEmpty()) {
                    totalQuestionsInput.error = "Required"
                    return@setOnClickListener
                }
                totalQuestions = totalInput.toInt()
                totalQuestionsInput.isEnabled = false
            }

            val questionData = mapOf(
                "question" to questionInput.text.toString(),
                "option1" to option1Input.text.toString(),
                "option2" to option2Input.text.toString(),
                "option3" to option3Input.text.toString(),
                "option4" to option4Input.text.toString(),
                "correctAnswer" to correctAnswerInput.text.toString().toInt()
            )
            questionsList.add(questionData)

            currentQuestion++
            clearFields()

            if (currentQuestion == totalQuestions) {
                nextButton.visibility = View.GONE
                submitButton.visibility = View.VISIBLE
            }
        }

        submitButton.setOnClickListener {
            showPublishOptions()
        }
    }

    private fun clearFields() {
        questionInput.text.clear()
        option1Input.text.clear()
        option2Input.text.clear()
        option3Input.text.clear()
        option4Input.text.clear()
        correctAnswerInput.text.clear()
    }

private fun showPublishOptions() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Publish Quiz")

    val quizNameInput = EditText(this)
    quizNameInput.hint = "Enter Quiz Name"
    builder.setView(quizNameInput)

    val options = arrayOf("Publish to Class", "Publish Publicly")
    builder.setItems(options) { _, which ->
        quizname = quizNameInput.text.toString()
        if (quizname.isEmpty()) {
            Toast.makeText(this, "Quiz name is required", Toast.LENGTH_SHORT).show()
            return@setItems
        }
        when (which) {
            0 -> selectClass()
            1 -> selectPublicCategory()
        }
    }
    builder.show()
}

    private fun selectClass() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("classes")

        database.get().addOnSuccessListener { snapshot ->
            val classList = mutableListOf<Pair<String, String>>() // Pair<classId, className>

            for (classSnapshot in snapshot.children) {
                val createdBy = classSnapshot.child("createdBy").getValue(String::class.java)
                val className = classSnapshot.child("className").getValue(String::class.java)

                if (createdBy == userId && className != null) {
                    val classId = classSnapshot.key ?: continue
                    classList.add(Pair(classId, className))
                }
            }

            if (classList.isEmpty()) {
                Toast.makeText(this, "No classes found", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val classNames = classList.map { it.second }.toTypedArray()

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Class")
            builder.setItems(classNames) { _, which ->
                val selectedClassId = classList[which].first
                uploadQuiz(className = selectedClassId, isPublic = false, category = null)
            }
            builder.show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load classes", Toast.LENGTH_SHORT).show()
        }
    }





    private fun selectPublicCategory() {
        val categories = arrayOf("Technology", "Physics", "Math", "Geography", "Chemistry")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Category")
        builder.setItems(categories) { _, which ->
            val selectedCategory = categories[which]
            uploadQuiz(className = null, isPublic = true, category = selectedCategory)
        }
        builder.show()
    }
    fun sendPushNotification(token: String, title: String, message: String, classId: String, context: Context) {
        val json = JSONObject().apply {
            put("to", token)
            put("notification", JSONObject().apply {
                put("title", title)
                put("body", message)
            })
            put("data", JSONObject().apply {
                put("click_action", "FLUTTER_NOTIFICATION_CLICK")
                put("classId", classId)
            })
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, "https://fcm.googleapis.com/fcm/send", json,
            Response.Listener { response ->
                Log.d("FCM", "Notification sent: $response")
            },
            Response.ErrorListener { error ->
                Log.e("FCM", "Notification failed: $error")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Authorization" to "key=293659426677",
                    "Content-Type" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun uploadQuiz(className: String?, isPublic: Boolean, category: String?) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val database = FirebaseDatabase.getInstance().reference

        val quizData = mapOf(
            "createdBy" to userId,
            "questions" to questionsList,
            "isPublic" to isPublic,
            "name" to quizname,
            "category" to (category ?: ""),
            "className" to (className ?: "")
        )

        val quizRef = if (isPublic) {
            database.child("publicQuizzes").push()
        } else {
            database.child("classes").child(className!!).child("quizzes").push()
        }

        quizRef.setValue(quizData).addOnSuccessListener {
            Toast.makeText(this, "Quiz Created Successfully", Toast.LENGTH_SHORT).show()

            // Only send notifications for class quizzes
            if (!isPublic && className != null) {
                val database = FirebaseDatabase.getInstance().reference
                val joinedUsersRef = database.child("classes").child(className).child("joinedUsers")

                joinedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnap in snapshot.children) {
                            val joinedUserId = userSnap.key ?: continue

                            // 1. Save to notifications node
                            val notification = mapOf(
                                "message" to "A new quiz has been added to your class!",
                                "classId" to className,
                                "timestamp" to System.currentTimeMillis()
                            )
                            database.child("notifications").child(joinedUserId).push().setValue(notification)

                            // 2. Get user token and send notification
                            database.child("users").child(joinedUserId).child("fcmToken")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(tokenSnap: DataSnapshot) {
                                        val token = tokenSnap.getValue(String::class.java)
                                        if (!token.isNullOrEmpty()) {
                                            sendPushNotification(
                                                token,
                                                "New Quiz Added",
                                                "A new quiz has been added to your class!",
                                                className,
                                                context = this@CreateQuizActivity // Make sure you're in an Activity/Fragment
                                            )
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FCM", "Error reading joined users", error.toException())
                    }
                })
            }


            finish() // close the activity
        }
    }

}