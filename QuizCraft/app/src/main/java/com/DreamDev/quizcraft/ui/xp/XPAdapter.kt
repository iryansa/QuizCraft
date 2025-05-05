package com.DreamDev.quizcraft.ui.xp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.DreamDev.quizcraft.R

class XPAdapter(private val xpList: List<XPRecord>) : RecyclerView.Adapter<XPAdapter.XPViewHolder>() {

    inner class XPViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val xpTitle: TextView = itemView.findViewById(R.id.xpTitle)
        val xpAmount: TextView = itemView.findViewById(R.id.xpAmount)
        val xpDate: TextView = itemView.findViewById(R.id.xpDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XPViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_xp_record, parent, false)
        return XPViewHolder(view)
    }

    override fun onBindViewHolder(holder: XPViewHolder, position: Int) {
        val record = xpList[position]
        holder.xpTitle.text = record.title
        holder.xpAmount.text = "${record.xpAmount} XP"
        holder.xpDate.text = record.dateEarned
    }

    override fun getItemCount(): Int {
        return xpList.size
    }
}
