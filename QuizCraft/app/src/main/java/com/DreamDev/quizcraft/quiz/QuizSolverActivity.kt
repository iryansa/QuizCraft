package com.DreamDev.quizcraft.quiz

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.models.User
import com.DreamDev.quizcraft.ui.xp.XPRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class QuizSolverActivity : AppCompatActivity() {

    private lateinit var option1: TextView
    private lateinit var option2: TextView
    private lateinit var option3: TextView
    private lateinit var option4: TextView
    private lateinit var nextButton: TextView
    private lateinit var questionText: TextView

    private var selectedOption: TextView? = null
    private var currentQuestionIndex = 0
    private var score = 0

    private lateinit var questions: List<QuestionItem>
    private var correctAnswer: Int = -1

    private lateinit var databaseReference: DatabaseReference

    private var quizId: String? = null
    private var isPublicQuiz: Boolean = false
    private var classId: String? = null // for class quizzes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_solver)

        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        nextButton = findViewById(R.id.nextButton)
        questionText = findViewById(R.id.questionText)

        // Retrieve data from intent
        quizId = intent.getStringExtra("quizId")
        isPublicQuiz = intent.getBooleanExtra("isPublicQuiz", false)
        classId = intent.getStringExtra("classId")
        Log.d("QuizSolverActivity", "Quiz ID: $quizId, Public: $isPublicQuiz, Class ID: $classId")
        val options = listOf(option1, option2, option3, option4)
        for (option in options) {
            option.setOnClickListener { selectOption(option) }
        }

        nextButton.setOnClickListener {
            checkAnswerAndProceed()
        }

        loadQuestions()
    }

    private fun loadQuestions() {
        val path = if (isPublicQuiz) {
            "publicQuizzes/$quizId/questions"

        } else {
            "classes/$classId/quizzes/$quizId/questions"
        }

        databaseReference = FirebaseDatabase.getInstance().getReference(path)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionList = mutableListOf<QuestionItem>()
                for (questionSnap in snapshot.children) {
                    val question = questionSnap.getValue(QuestionItem::class.java)
                    question?.let { questionList.add(it) }
                }
                if (questionList.isNotEmpty()) {
                    questions = questionList
                    loadQuestion()
                } else {
                    Toast.makeText(this@QuizSolverActivity, "No questions found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuizSolverActivity, "Failed to load questions.", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]
        questionText.text = question.question

        option1.text = question.option1
        option2.text = question.option2
        option3.text = question.option3
        option4.text = question.option4

        correctAnswer = question.correctAnswer

        selectedOption = null
        resetOptionStyles()
    }
    private fun resetOptionStyles(){
        option1.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext_bg)
        option2.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext_bg)
        option3.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext_bg)
        option4.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext_bg)

        option1.setTextColor(Color.parseColor("#DFDFDF"))
        option2.setTextColor(Color.parseColor("#DFDFDF"))
        option3.setTextColor(Color.parseColor("#DFDFDF"))
        option4.setTextColor(Color.parseColor("#DFDFDF"))
    }
    private fun selectOption(option: TextView) {
        selectedOption?.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext_bg)
        selectedOption?.setTextColor(Color.parseColor("#DFDFDF"))

        option.background = ContextCompat.getDrawable(this, R.drawable.selected_option_bg)
        option.setTextColor(Color.WHITE)

        selectedOption = option
    }

    private fun checkAnswerAndProceed() {
        if (selectedOption == null) {
            Toast.makeText(this, "Please select an option.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = when (selectedOption) {
            option1 -> 1
            option2 -> 2
            option3 -> 3
            option4 -> 4
            else -> -1
        }

        if (selectedIndex == correctAnswer) {
            score += 10 // You can adjust scoring
        }

        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            loadQuestion()
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        updateUserXP()
        Toast.makeText(this, "Quiz finished! Score: $score", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun updateUserXP() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Update only the XP field using updateChildren (no overwrite)
        userRef.child("xp").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentXp = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentXp + score
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Toast.makeText(this@QuizSolverActivity, "Failed to update XP.", Toast.LENGTH_SHORT).show()
                    return
                }

                // ✅ Now safely add to xpHistory
                val xpRecord = XPRecord(
                    title = "Quiz", // better than static "Quiz"
                    xpAmount = score,
                    dateEarned = getCurrentDate()
                )

                val historyRef = userRef.child("xpHistory").push()
                historyRef.setValue(xpRecord)
            }
        })
    }
    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    }

