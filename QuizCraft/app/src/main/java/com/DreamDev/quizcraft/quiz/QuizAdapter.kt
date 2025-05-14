package com.DreamDev.quizcraft.quiz

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R
import com.DreamDev.quizcraft.quiz.QuizItem
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QuizAdapter(private val quizList: List<QuizItem>) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quiz_item_layout, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        holder.quizName.text = quiz.name
        //logging all the values in the quizitem
        Log.d("QuizAdapter", "Quiz ID: ${quiz.id}, Name: ${quiz.name}, Class: ${quiz.className}, Is Public: ${quiz.isPublic}")

        // Inside onBindViewHolder()
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, QuizSolverActivity::class.java)
            intent.putExtra("quizId", quiz.id) // you need to store id too
            intent.putExtra("isPublicQuiz", quiz.isPublic)
            intent.putExtra("classId", quiz.className) // if needed
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            if (checkAndRequestStoragePermission(holder.itemView.context)) {
                fetchQuizDetailsAndGeneratePdf(holder.itemView.context, quiz.id, quiz.name)
            }
            true
        }


    }
    fun fetchQuizDetailsAndGeneratePdf(context: Context, quizId: String, quizName: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("publicQuizzes").child(quizId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val questionsSnapshot = snapshot.child("questions")
                val questionList = mutableListOf<Question>()

                for (qSnap in questionsSnapshot.children) {
                    val question = qSnap.getValue(Question::class.java)
                    question?.let { questionList.add(it) }
                }

                generatePdf(context, quizName, questionList)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load quiz", Toast.LENGTH_SHORT).show()
        }
    }
    fun generatePdf(context: Context, quizName: String, questions: List<Question>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.BLACK
        }

        var pageNumber = 1
        var y = 50f

        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawText("Quiz: $quizName", 40f, y, titlePaint)
        y += 40f

        for ((index, q) in questions.withIndex()) {
            if (y > 750f) {
                pdfDocument.finishPage(page)
                pageNumber++
                y = 50f
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
            }

            canvas.drawText("${index + 1}. ${q.question}", 40f, y, paint)
            y += 25f
            canvas.drawText("A. ${q.option1}", 60f, y, paint)
            y += 20f
            canvas.drawText("B. ${q.option2}", 60f, y, paint)
            y += 20f
            canvas.drawText("C. ${q.option3}", 60f, y, paint)
            y += 20f
            canvas.drawText("D. ${q.option4}", 60f, y, paint)
            y += 30f
            val correctOption = when (q.correctAnswer) {
                1 -> "A"
                2 -> "B"
                3 -> "C"
                4 -> "D"
                else -> q.correctAnswer
            }
            canvas.drawText("Correct Answer: $correctOption", 40f, y, paint)
            y += 30f

        }

        pdfDocument.finishPage(page)



        val dir = File(Environment.getExternalStorageDirectory(), "QuizCraft")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, "$quizName.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    override fun getItemCount(): Int {
        return quizList.size
    }

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quizName: TextView = itemView.findViewById(R.id.quizNameTextView)
    }
    private fun checkAndRequestStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                if (context is Activity) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Open app settings to allow file access", Toast.LENGTH_LONG).show()
                }
                return false
            }
            return true
        } else {
            val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                if (context is Activity) {
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
                } else {
                    Toast.makeText(context, "Storage permission needed", Toast.LENGTH_LONG).show()
                }
                return false
            }
            return true
        }
    }

}
