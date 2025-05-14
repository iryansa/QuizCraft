package com.DreamDev.quizcraft.ui.xp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Xps : Fragment() {
    private lateinit var xpRecyclerView: RecyclerView
    private lateinit var xpAdapter: XPAdapter
    private val xpList = mutableListOf<XPRecord>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_xps, container, false)

        xpRecyclerView = view.findViewById(R.id.xpRecyclerView)
        xpRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val namet = view.findViewById<TextView>(R.id.userName)
        val points = view.findViewById<TextView>(R.id.points_text2)



// Fetch data from the database
val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return view

        //update the name and the points
        val userRef1 = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef1.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java)
                namet.text = name
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
        userRef1.child("xp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pointsValue = snapshot.getValue(Int::class.java)
                points.text = pointsValue.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("xpHistory")
userRef.addListenerForSingleValueEvent(object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
        xpList.clear()
        for (xpSnap in snapshot.children) {
            val xpRecord = xpSnap.getValue(XPRecord::class.java)
            xpRecord?.let { xpList.add(it) }
        }
        xpAdapter.notifyDataSetChanged()
    }

    override fun onCancelled(error: DatabaseError) {
        // Handle database error
    }
})

        xpAdapter = XPAdapter(xpList)
        xpRecyclerView.adapter = xpAdapter

        return view
    }
}

