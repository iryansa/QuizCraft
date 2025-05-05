package com.DreamDev.quizcraft.ui.xp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R


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

        // Dummy data (you can load real data later)
        xpList.add(XPRecord("Quiz 1", 50, "2025-05-01"))
        xpList.add(XPRecord("Quiz 2", 30, "2025-05-02"))
        xpList.add(XPRecord("Bonus XP", 20, "2025-05-03"))

        xpAdapter = XPAdapter(xpList)
        xpRecyclerView.adapter = xpAdapter

        return view
    }
}

