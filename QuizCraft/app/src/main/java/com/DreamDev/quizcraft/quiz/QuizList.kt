package com.DreamDev.quizcraft.quiz

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.quiz.QuizItem
import com.google.firebase.database.*

class QuizList : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var quizRecyclerView: RecyclerView
    private lateinit var quizList: ArrayList<QuizItem>
    private lateinit var quizAdapter: QuizAdapter

    private var categoryName: String? = null
    private var classId: String? = null // will be Firebase ID if coming from class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_list)

        quizRecyclerView = findViewById(R.id.quizRecyclerView)
        quizRecyclerView.layoutManager = LinearLayoutManager(this)
        quizList = ArrayList()
        quizAdapter = QuizAdapter(quizList)
        quizRecyclerView.adapter = quizAdapter

        // Receiving category or class from previous screen
        categoryName = intent.getStringExtra("categoryName")
        classId = intent.getStringExtra("classId")
        Log.d("QuizList", "Category Name: $categoryName, Class ID: $classId")
        if (classId != null) {
            loadClassQuizzes(classId!!)
        } else if (categoryName != null) {
            loadPublicQuizzes(categoryName!!)
        } else {
            Toast.makeText(this, "No category or class specified.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadClassQuizzes(classId: String) {
        databaseReference = FirebaseDatabase.getInstance()
            .getReference("classes")
            .child(classId)
            .child("quizzes")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                quizList.clear()
                for (quizSnap in snapshot.children) {
                    val quizItem = quizSnap.getValue(QuizItem::class.java)
if (quizItem != null) {
    val quizWithId = quizItem.copy(id = quizSnap.key ?: "", isPublic = false)
    quizList.add(quizWithId)
}
                }
                quizAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuizList, "Failed to load quizzes.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPublicQuizzes(categoryName: String) {
        databaseReference = FirebaseDatabase.getInstance()
            .getReference("publicQuizzes")

        databaseReference.orderByChild("category").equalTo(categoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    quizList.clear()
                    for (quizSnap in snapshot.children) {
                        val quizItem = quizSnap.getValue(QuizItem::class.java)
                        if (quizItem != null) {
                            val quizWithId = quizItem.copy(id = quizSnap.key ?: "")
                            quizList.add(quizWithId)
                        }
                    }
                    quizAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@QuizList, "Failed to load public quizzes.", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
