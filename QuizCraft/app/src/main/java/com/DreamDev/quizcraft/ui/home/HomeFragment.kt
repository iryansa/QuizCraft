package com.DreamDev.quizcraft.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.Search.SearchQuizActivity
import com.DreamDev.quizcraft.quiz.CreateQuizActivity
import com.DreamDev.quizcraft.quiz.QuizList
import com.DreamDev.quizcraft.quiz.QuizSolverActivity

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can remove this method if not used
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val shareLayout = rootView.findViewById<View>(R.id.share_layout)
        shareLayout.setOnClickListener {
            val intent = Intent(requireContext(), CreateQuizActivity::class.java)
            startActivity(intent)
        }

        val searchquiz = rootView.findViewById<ImageView>(R.id.account_picture)
        searchquiz.setOnClickListener {
            //open search quiz activity
            val intent = Intent(requireContext(), SearchQuizActivity::class.java)
            startActivity(intent)
        }


val technologyLayout = rootView.findViewById<View>(R.id.technology_layout)
technologyLayout.setOnClickListener {
    val intent = Intent(requireContext(), QuizList::class.java)
    intent.putExtra("categoryName", "Technology")
    intent.putExtra("classId", null as String?)
    startActivity(intent)
}

val mathLayout = rootView.findViewById<View>(R.id.math_layout)
mathLayout.setOnClickListener {
    val intent = Intent(requireContext(), QuizList::class.java)
    intent.putExtra("categoryName", "Math")
    intent.putExtra("classId", null as String?)
    startActivity(intent)
}

val physicsLayout = rootView.findViewById<View>(R.id.physics_layout)
physicsLayout.setOnClickListener {
    val intent = Intent(requireContext(), QuizList::class.java)
    intent.putExtra("categoryName", "Physics")
    intent.putExtra("classId", null as String?)
    startActivity(intent)
}

val chemistryLayout = rootView.findViewById<View>(R.id.chemistry_layout)
chemistryLayout.setOnClickListener {
    val intent = Intent(requireContext(), QuizList::class.java)
    intent.putExtra("categoryName", "Chemistry")
    intent.putExtra("classId", null as String?)
    startActivity(intent)
}

val geographyLayout = rootView.findViewById<View>(R.id.geography_layout)
geographyLayout.setOnClickListener {
    val intent = Intent(requireContext(), QuizList::class.java)
    intent.putExtra("categoryName", "Geography")
    intent.putExtra("classId", null as String?)
    startActivity(intent)
}

        val PlayQuizv = rootView.findViewById<View>(R.id.play_quiz_button)
        PlayQuizv.setOnClickListener {
            val intent = Intent(requireContext(), QuizSolverActivity::class.java)
            val quizid1 : String = "-OQ5N9L4LuXX_a1hjdKY"
            val publicQuiz : Boolean = true

            intent.putExtra("quizId", quizid1) // you need to store id too
            intent.putExtra("isPublicQuiz", publicQuiz)
            intent.putExtra("classId", "") // if needed
            startActivity(intent)
        }

        return rootView
    }
}
