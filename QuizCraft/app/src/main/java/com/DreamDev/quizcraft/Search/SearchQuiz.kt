package com.DreamDev.quizcraft.Search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.quiz.QuizAdapter
import com.DreamDev.quizcraft.quiz.QuizItem
import com.google.firebase.database.*

class SearchQuizActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var quizList: ArrayList<QuizItem>
    private lateinit var filteredList: ArrayList<QuizItem>
    private lateinit var quizAdapter: QuizAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_quiz)

        searchEditText = findViewById(R.id.searchEditText)
        searchRecyclerView = findViewById(R.id.searchQuizRecyclerView)

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        quizList = ArrayList()
        filteredList = ArrayList()
        quizAdapter = QuizAdapter(filteredList)
        searchRecyclerView.adapter = quizAdapter

        loadAllPublicQuizzes()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterQuizzes(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadAllPublicQuizzes() {
        databaseReference = FirebaseDatabase.getInstance().getReference("publicQuizzes")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                quizList.clear()
                for (quizSnap in snapshot.children) {
                    val quizItem = quizSnap.getValue(QuizItem::class.java)
                    if (quizItem != null) {
                        val quizWithId = quizItem.copy(id = quizSnap.key ?: "")
                        quizList.add(quizWithId)
                    }
                }
                filteredList.clear()
                filteredList.addAll(quizList)
                quizAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchQuizActivity, "Failed to load quizzes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterQuizzes(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredList.clear()
        for (quiz in quizList) {
            if (quiz.name.lowercase().contains(lowerCaseQuery)) {
                filteredList.add(quiz)
            }
        }
        quizAdapter.notifyDataSetChanged()
    }
}
