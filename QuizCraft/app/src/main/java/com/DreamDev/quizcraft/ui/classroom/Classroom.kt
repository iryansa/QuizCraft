package com.DreamDev.quizcraft.ui.classroom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.models.ClassItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import android.content.res.Resources
import com.DreamDev.quizcraft.quiz.QuizList
import java.util.*

class Classroom : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var classesContainer: LinearLayout
    private lateinit var joinClassButton: Button
    private lateinit var createClassButton: Button
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_classroom, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("classes")
        classesContainer = view.findViewById(R.id.classesContainer)
        joinClassButton = view.findViewById(R.id.joinClassButton)
        createClassButton = view.findViewById(R.id.createClassButton)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return  // Prevent crash

                classesContainer.removeAllViews()

                for (classSnapshot in snapshot.children) {
val classItem = classSnapshot.getValue(ClassItem::class.java)?.apply { classId = classSnapshot.key ?: "" }
                    val joinedUsers = classSnapshot.child("joinedUsers").children.map { it.key }

                    classItem?.let {
                        if (it.createdBy == currentUserId || joinedUsers.contains(currentUserId)) {
                            addClassCard(it, classesContainer)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error loading classes: ${error.message}")
            }
        })


        setupJoinClassButton()
        setupCreateClassButton()

        return view
    }

    private fun setupJoinClassButton() {
        joinClassButton.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Join Class")

            val input = EditText(requireContext())
            input.hint = "Enter Class Code"
            builder.setView(input)

            builder.setPositiveButton("Join") { _, _ ->
                val classCode = input.text.toString().trim()
                if (classCode.isNotEmpty()) {
                    joinClass(classCode)
                }
            }
            builder.setNegativeButton("Cancel", null)

            builder.show()
        }
    }

    private fun setupCreateClassButton() {
        createClassButton.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Create Class")

            val layout = LinearLayout(requireContext())
            layout.orientation = LinearLayout.VERTICAL

            val classNameInput = EditText(requireContext())
            classNameInput.hint = "Class Name"
            layout.addView(classNameInput)

            val categorySpinner = Spinner(requireContext())
            val categories = arrayOf("Technology", "Math", "Physics", "Chemistry", "Biology", "Geography")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
            categorySpinner.adapter = adapter
            layout.addView(categorySpinner)

            builder.setView(layout)

            builder.setPositiveButton("Create") { _, _ ->
                val className = classNameInput.text.toString().trim()
                val selectedCategory = categorySpinner.selectedItem.toString()

                if (className.isNotEmpty()) {
                    createClass(className, selectedCategory)
                }
            }
            builder.setNegativeButton("Cancel", null)

            builder.show()
        }
    }

    private fun joinClass(classCode: String) {
        databaseReference.orderByChild("classCode").equalTo(classCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return  // Prevent crash if fragment not attached

                    if (snapshot.exists()) {
                        for (classSnap in snapshot.children) {
                            val createdBy = classSnap.child("createdBy").getValue(String::class.java)
                            if (createdBy == currentUserId) {
                                Toast.makeText(requireContext(), "You are the creator of this class.", Toast.LENGTH_SHORT).show()
                                return
                            }

                            classSnap.ref.child("joinedUsers").child(currentUserId).setValue(true)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Successfully joined the class!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to join class.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Class code not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Failed to join class.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun createClass(className: String, category: String) {
        generateUniqueClassCode { uniqueCode ->
            val classId = databaseReference.push().key ?: return@generateUniqueClassCode
            val newClass = ClassItem(
                classId = classId,
                className = className,
                imageName = category.lowercase(),
                createdBy = currentUserId,
                classCode = uniqueCode
            )

            databaseReference.child(classId).setValue(newClass)
                .addOnSuccessListener {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    builder.setTitle("Class Created!")
                    builder.setMessage("Share this Class Code with others: \n\n$uniqueCode")
                    builder.setPositiveButton("OK", null)
                    builder.show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to create class.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addClassCard(classItem: ClassItem, classesContainer: LinearLayout) {
        val context = requireContext()

        val cardLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(165.dpToPx(), 250.dpToPx()).apply {
                marginEnd = 12.dpToPx()
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = if (classItem.createdBy == currentUserId) {
                // Use a solid orange background for created classes
                ContextCompat.getDrawable(context, R.drawable.orange_card_background)
            } else {
                // Default background
                ContextCompat.getDrawable(context, R.drawable.cards_bg1)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                Toast.makeText(context, "Clicked on ${classItem.className}", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, QuizList::class.java)
                intent.putExtra("categoryName" , "")
                intent.putExtra("classId", classItem.classId)  // <-- send the Firebase PUSH id, not classCode
                startActivity(intent)
            }
        }

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(70.dpToPx(), 70.dpToPx())
            val imageResId = context.resources.getIdentifier(classItem.imageName, "drawable", context.packageName)
            if (imageResId != 0) {
                setImageResource(imageResId)
            } else {
                setImageResource(R.drawable.geography)
            }
        }

        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8.dpToPx()
            }
            text = classItem.className
            setTextColor(Color.WHITE)
            textSize = 24f
            typeface = ResourcesCompat.getFont(context, R.font.poppinsbold)
        }

        cardLayout.addView(imageView)
        cardLayout.addView(textView)
        classesContainer.addView(cardLayout)
    }


    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun generateUniqueClassCode(callback: (String) -> Unit) {
        val code = generateClassCode()
        databaseReference.orderByChild("classCode").equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Code already exists, generate a new one
                        generateUniqueClassCode(callback)
                    } else {
                        // Code is unique
                        callback(code)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error generating class code.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun generateClassCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
